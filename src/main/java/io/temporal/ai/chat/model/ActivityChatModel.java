package io.temporal.ai.chat.model;

import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.model.tool.*;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ActivityChatModel is a chat model implementation that uses a Temporal activity to call a chat model.
 * It supports tool calling and tool execution eligibility checks.
 */
public class ActivityChatModel implements ChatModel {
    private final ChatModelActivity chatModelActivity;
    private final ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();
    private final ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate = new DefaultToolExecutionEligibilityPredicate();


    /**
     * Constructor for ActivityChatModel.
     *
     * @param chatModelActivity The Temporal activity that implements the chat model.
     */
    public ActivityChatModel(ChatModelActivity chatModelActivity) {
        this.chatModelActivity = chatModelActivity;
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return ToolCallingChatOptions.builder().build();
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        ChatModelTypes.ChatModelActivityOutput output = chatModelActivity.callChatModel(createRequest(prompt));
        ChatResponseMetadata metadata = ChatResponseMetadata.builder()
                .model(output.metadata().model())
                .build();

        ChatResponse response = ChatResponse.builder().generations(
                output.generations().stream().map(generation -> new Generation(toAssistantMessage(generation.message()))).collect(Collectors.toList()))
                .metadata(metadata)
                .build();

        if (prompt.getOptions() != null && this.toolExecutionEligibilityPredicate.isToolExecutionRequired(prompt.getOptions(), response)) {
            var toolExecutionResult = this.toolCallingManager.executeToolCalls(prompt, response);
            if (toolExecutionResult.returnDirect()) {
                // Return tool execution result directly to the client.
                return ChatResponse.builder()
                        .from(response)
                        .generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
                        .build();
            }
            else {
                // Send the tool execution result back to the model.
                return this.call(new Prompt(toolExecutionResult.conversationHistory(), prompt.getOptions()));
            }
        }
        return response;
    }

    public static Media toMedia(ChatModelTypes.MediaContent mediaContent)  {
        if (mediaContent.uri() != null) {
            try {
                return new Media(MimeType.valueOf(mediaContent.mimeType()), new URI(mediaContent.uri()));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        } else if (mediaContent.data() != null) {
            return new Media(MimeType.valueOf(mediaContent.mimeType()), new ByteArrayResource(mediaContent.data()));
        } else {
            throw new IllegalArgumentException("Unsupported media content data type: " + mediaContent.data().getClass().getSimpleName());
        }
    }


    public static Prompt createPrompt(ChatModelTypes.ChatModelActivityInput input) {
        List<org.springframework.ai.chat.messages.Message> messages = input.messages().stream().map(message -> {
            if (message.role() == ChatModelTypes.Message.Role.USER || message.role() == ChatModelTypes.Message.Role.SYSTEM) {
                if (message.rawContent() instanceof String textContent) {
                    // If the content is a String, we can directly use it as the message text.
                    UserMessage.Builder userMessageBuilder = UserMessage.builder().text(textContent);
                    if (message.mediaContents() != null) {
                        List<Media> media = message.mediaContents().stream().map(ActivityChatModel::toMedia).toList();
                        userMessageBuilder.media(media);
                    }
                    return userMessageBuilder.build();
                } else {
                    throw new IllegalArgumentException("Unsupported raw content type: " + message.rawContent().getClass().getSimpleName());
                }
            }
            else if (message.role() == ChatModelTypes.Message.Role.ASSISTANT) {
                return toAssistantMessage(message);
            }
            else if (message.role() == ChatModelTypes.Message.Role.TOOL) {
                return new ToolResponseMessage(List.of(new ToolResponseMessage.ToolResponse(message.toolCallId(), message.name(), (String) message.rawContent())));
            }
            else {
                throw new IllegalArgumentException("Unsupported message role: " + message.role());
            }
        }).map(abstractMessage -> (Message) abstractMessage).toList();

        ToolCallingChatOptions.Builder toolCallingChatOptionsBuilder = ToolCallingChatOptions.builder();
        // Disable internal tool execution so the workflow can handle tool execution.
        toolCallingChatOptionsBuilder = toolCallingChatOptionsBuilder.internalToolExecutionEnabled(false);
        if (input.modelOptions() != null) {
            toolCallingChatOptionsBuilder.model(input.modelOptions().model());
            toolCallingChatOptionsBuilder.frequencyPenalty(input.modelOptions().frequencyPenalty());
            toolCallingChatOptionsBuilder.maxTokens(input.modelOptions().maxTokens());
            toolCallingChatOptionsBuilder.presencePenalty(input.modelOptions().presencePenalty());
            toolCallingChatOptionsBuilder.stopSequences(input.modelOptions().stopSequences());
            toolCallingChatOptionsBuilder.temperature(input.modelOptions().temperature());
            toolCallingChatOptionsBuilder.topK(input.modelOptions().topK());
            toolCallingChatOptionsBuilder.topP(input.modelOptions().topP());
        }

        ToolCallingChatOptions toolCallingChatOptions = toolCallingChatOptionsBuilder.build();
        if (input.tools() != null && !input.tools().isEmpty()) {
            toolCallingChatOptions.setToolCallbacks(input.tools().stream().map(ChatModelTypes.FunctionTool::getFunction)
                    .map(f ->
                            ToolDefinition.builder()
                                    .name(f.getName())
                                    .description(f.getDescription())
                                    .inputSchema(f.getJsonSchema())
                                    .build()
                    ).map(RemoteToolCallback::new).collect(Collectors.toList()));
        }

        return Prompt.builder()
                .messages(messages)
                .chatOptions(toolCallingChatOptions)
                .build();
    }

    ChatModelTypes.ChatModelActivityInput createRequest(Prompt prompt) {
        List<ChatModelTypes.Message> messages = prompt.getInstructions().stream().map(message -> {
            if (message.getMessageType() == MessageType.USER || message.getMessageType() == MessageType.SYSTEM) {
                Object content = message.getText();
                if (message instanceof UserMessage userMessage) {
                    if (!CollectionUtils.isEmpty(userMessage.getMedia())) {
                        List<ChatModelTypes.MediaContent> contentList = new ArrayList<>(userMessage.getMedia().stream().map(ActivityChatModel::mapToMediaContent).toList());
                        return List.of(new ChatModelTypes.Message(content, contentList, ChatModelTypes.Message.Role.valueOf(message.getMessageType().name())));
                    }
                }

                return List.of(new ChatModelTypes.Message(content,
                        ChatModelTypes.Message.Role.valueOf(message.getMessageType().name())));
            }
            else if (message.getMessageType() == MessageType.ASSISTANT) {
                var assistantMessage = (AssistantMessage) message;
                return List.of(fromAssistantMessage(assistantMessage));
            }
            else if (message.getMessageType() == MessageType.TOOL) {
                ToolResponseMessage toolMessage = (ToolResponseMessage) message;
                return toolMessage.getResponses()
                        .stream()
                        .map(tr -> new ChatModelTypes.Message(tr.responseData(), ChatModelTypes.Message.Role.TOOL, tr.name(),
                                tr.id(), null, null))
                        .toList();
            }
            else {
                throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
            }
        }).flatMap(List::stream).toList();

        List<ChatModelTypes.FunctionTool> tools = List.of();
        // Add the tool definitions to the request's tools parameter.
        if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
            List<ToolDefinition> toolDefinitions = this.toolCallingManager.resolveToolDefinitions(toolCallingChatOptions);
            if (!CollectionUtils.isEmpty(toolDefinitions)) {
                tools = this.getFunctionTools(toolDefinitions);
            }
        }

        ChatModelTypes.ModelOptions modelOptions = null;
        if (prompt.getOptions() != null) {
            modelOptions = new ChatModelTypes.ModelOptions(
                    prompt.getOptions().getModel(),
                    prompt.getOptions().getFrequencyPenalty(),
                    prompt.getOptions().getMaxTokens(),
                    prompt.getOptions().getPresencePenalty(),
                    prompt.getOptions().getStopSequences(),
                    prompt.getOptions().getTemperature(),
                    prompt.getOptions().getTopK(),
                    prompt.getOptions().getTopP()
            );
        }

        return new ChatModelTypes.ChatModelActivityInput(messages, modelOptions, tools);
    }

    public static AssistantMessage toAssistantMessage(ChatModelTypes.Message message) {
        return new AssistantMessage(
                (String) message.rawContent(),
                Map.of(),
                message.toolCalls() != null ?
                        message.toolCalls().stream().map(
                                tc -> new AssistantMessage.ToolCall(tc.id(), tc.type(), tc.function().name(), tc.function().arguments())
                        ).toList() : List.of(),
                message.mediaContents() != null ?
                message.mediaContents().stream().map(ActivityChatModel::toMedia).toList() : List.of());

    }

    public static ChatModelTypes.Message fromAssistantMessage(AssistantMessage assistantMessage) {
        List<ChatModelTypes.Message.ToolCall> toolCalls = null;
        if (!CollectionUtils.isEmpty(assistantMessage.getToolCalls())) {
            toolCalls = assistantMessage.getToolCalls().stream().map(toolCall -> {
                var function = new ChatModelTypes.Message.ChatCompletionFunction(toolCall.name(), toolCall.arguments());
                return new ChatModelTypes.Message.ToolCall(toolCall.id(), toolCall.type(), function);
            }).toList();
        }
        List<ChatModelTypes.MediaContent> contentList = new ArrayList<>(assistantMessage.getMedia().stream().map(ActivityChatModel::mapToMediaContent).toList());
        return new ChatModelTypes.Message(assistantMessage.getText(),
                ChatModelTypes.Message.Role.ASSISTANT, null, null, toolCalls, contentList);
    }

    private static ChatModelTypes.MediaContent mapToMediaContent(Media media) {
        if (media.getData() instanceof String uri) {
            return new ChatModelTypes.MediaContent(media.getMimeType().toString(), uri);
        } else if (media.getData() instanceof byte[] data) {
            return new ChatModelTypes.MediaContent(media.getMimeType().toString(), data);
        } else {
            throw new IllegalArgumentException("Unsupported media content data type: " + media.getData().getClass().getSimpleName());
        }
    }

    private List<ChatModelTypes.FunctionTool> getFunctionTools(List<ToolDefinition> toolDefinitions) {
        return toolDefinitions.stream().map(toolDefinition -> {
            var function = new ChatModelTypes.FunctionTool.Function(toolDefinition.description(), toolDefinition.name(),
                    toolDefinition.inputSchema());
            return new ChatModelTypes.FunctionTool(function);
        }).toList();
    }
}
