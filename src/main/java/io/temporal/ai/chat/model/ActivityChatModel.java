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
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivityChatModel implements ChatModel {
    private final ChatModelActivity chatModelActivity;
    private final ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();
    private final ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate = new DefaultToolExecutionEligibilityPredicate();


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


    public static Prompt createPrompt(ChatModelTypes.ChatModelActivityInput input) {
        List<org.springframework.ai.chat.messages.Message> messages = input.messages().stream().map(message -> {
            if (message.role() == ChatModelTypes.Message.Role.USER || message.role() == ChatModelTypes.Message.Role.SYSTEM) {
                if (message.rawContent() instanceof String textContent) {
                    // If the content is a String, we can directly use it as the message text.
                    return new UserMessage(textContent);
                }
                else if (message.rawContent() instanceof ChatModelTypes.MediaContent mediaContent) {
                    throw new UnsupportedOperationException("Media content is not supported in this implementation");
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
                        List<ChatModelTypes.MediaContent> contentList = new ArrayList<>(List.of(new ChatModelTypes.MediaContent(message.getText())));

                        contentList.addAll(userMessage.getMedia().stream().map(this::mapToMediaContent).toList());

                        content = contentList;
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

                toolMessage.getResponses()
                        .forEach(response -> Assert.isTrue(response.id() != null, "ToolResponseMessage must have an id"));
                return toolMessage.getResponses()
                        .stream()
                        .map(tr -> new ChatModelTypes.Message(tr.responseData(), ChatModelTypes.Message.Role.TOOL, tr.name(),
                                tr.id(), null, null, null, null))
                        .toList();
            }
            else {
                throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
            }
        }).flatMap(List::stream).toList();

        // OpenAiChatOptions requestOptions = (OpenAiChatOptions) prompt.getOptions();
        List<ChatModelTypes.FunctionTool> tools = List.of();
        // Add the tool definitions to the request's tools parameter.
        if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
            List<ToolDefinition> toolDefinitions = this.toolCallingManager.resolveToolDefinitions(toolCallingChatOptions);
            if (!CollectionUtils.isEmpty(toolDefinitions)) {
                tools = this.getFunctionTools(toolDefinitions);
            }
        }

        ChatModelTypes.ChatModelActivityInput.ModelOptions modelOptions = null;
        if (prompt.getOptions() != null) {
             new ChatModelTypes.ChatModelActivityInput.ModelOptions(
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
                        ).toList() : List.of());

    }

    public static ChatModelTypes.Message fromAssistantMessage(AssistantMessage assistantMessage) {
        List<ChatModelTypes.Message.ToolCall> toolCalls = null;
        if (!CollectionUtils.isEmpty(assistantMessage.getToolCalls())) {
            toolCalls = assistantMessage.getToolCalls().stream().map(toolCall -> {
                var function = new ChatModelTypes.Message.ChatCompletionFunction(toolCall.name(), toolCall.arguments());
                return new ChatModelTypes.Message.ToolCall(toolCall.id(), toolCall.type(), function);
            }).toList();
        }
        ChatModelTypes.Message.AudioOutput audioOutput = null;
        if (!CollectionUtils.isEmpty(assistantMessage.getMedia())) {
            Assert.isTrue(assistantMessage.getMedia().size() == 1,
                    "Only one media content is supported for assistant messages");
            audioOutput = new ChatModelTypes.Message.AudioOutput(assistantMessage.getMedia().get(0).getId(), null, null, null);
        }
        return new ChatModelTypes.Message(assistantMessage.getText(),
                ChatModelTypes.Message.Role.ASSISTANT, null, null, toolCalls, null, audioOutput, null);
    }

    private ChatModelTypes.MediaContent mapToMediaContent(Media media) {
        var mimeType = media.getMimeType();
        if (MimeTypeUtils.parseMimeType("audio/mp3").equals(mimeType)) {
            return new ChatModelTypes.MediaContent(
                    new ChatModelTypes.MediaContent.InputAudio(fromAudioData(media.getData()), ChatModelTypes.MediaContent.InputAudio.Format.MP3));
        }
        if (MimeTypeUtils.parseMimeType("audio/wav").equals(mimeType)) {
            return new ChatModelTypes.MediaContent(
                    new ChatModelTypes.MediaContent.InputAudio(fromAudioData(media.getData()), ChatModelTypes.MediaContent.InputAudio.Format.WAV));
        }
        else {
            return new ChatModelTypes.MediaContent(
                    new ChatModelTypes.MediaContent.ImageUrl(this.fromMediaData(media.getMimeType(), media.getData())));
        }
    }

    private String fromAudioData(Object audioData) {
        if (audioData instanceof byte[] bytes) {
            return Base64.getEncoder().encodeToString(bytes);
        }
        throw new IllegalArgumentException("Unsupported audio data type: " + audioData.getClass().getSimpleName());
    }

    private String fromMediaData(MimeType mimeType, Object mediaContentData) {
        if (mediaContentData instanceof byte[] bytes) {
            // Assume the bytes are an image. So, convert the bytes to a base64 encoded
            // following the prefix pattern.
            return String.format("data:%s;base64,%s", mimeType.toString(), Base64.getEncoder().encodeToString(bytes));
        }
        else if (mediaContentData instanceof String text) {
            // Assume the text is a URLs or a base64 encoded image prefixed by the user.
            return text;
        }
        else {
            throw new IllegalArgumentException(
                    "Unsupported media data type: " + mediaContentData.getClass().getSimpleName());
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
