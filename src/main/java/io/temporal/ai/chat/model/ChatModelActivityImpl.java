package io.temporal.ai.chat.model;

import io.temporal.failure.ApplicationErrorCategory;
import io.temporal.failure.ApplicationFailure;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.retry.NonTransientAiException;

import java.util.List;
import java.util.stream.Collectors;

public class ChatModelActivityImpl implements ChatModelActivity {
    ChatModel chatModel;

    ChatModelActivityImpl(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ChatModelTypes.ChatModelActivityOutput callChatModel(ChatModelTypes.ChatModelActivityInput input) {
        try {
            ChatResponse response = chatModel.call(ActivityChatModel.createPrompt(input));
            List<ChatModelTypes.ChatModelActivityOutput.Generation> generations = response.getResults().stream()
                    .map(generation -> new ChatModelTypes.ChatModelActivityOutput.Generation(
                            ActivityChatModel.fromAssistantMessage(generation.getOutput())

                    ))
                    .collect(Collectors.toList());

            ChatModelTypes.ChatModelActivityOutput.ChatResponseMetadata metadata = new ChatModelTypes.ChatModelActivityOutput.ChatResponseMetadata(
                    response.getMetadata().getModel(),
                    new ChatModelTypes.ChatModelActivityOutput.ChatResponseMetadata.RateLimit(
                            response.getMetadata().getRateLimit().getRequestsLimit(),
                            response.getMetadata().getRateLimit().getRequestsRemaining(),
                            response.getMetadata().getRateLimit().getRequestsReset(),
                            response.getMetadata().getRateLimit().getTokensLimit(),
                            response.getMetadata().getRateLimit().getTokensRemaining(),
                            response.getMetadata().getRateLimit().getTokensReset()
                    ),
                    new ChatModelTypes.ChatModelActivityOutput.ChatResponseMetadata.Usage(
                            response.getMetadata().getUsage().getPromptTokens(),
                            response.getMetadata().getUsage().getCompletionTokens(),
                            response.getMetadata().getUsage().getTotalTokens()
                    )
            );
            return new ChatModelTypes.ChatModelActivityOutput(
                    generations,
                    metadata
            );
        } catch (NonTransientAiException e) {
            throw ApplicationFailure.newBuilder()
                    .setType(e.getClass().getTypeName())
                    .setMessage(e.getMessage())
                    .setCause(e.getCause())
                    .setCategory(ApplicationErrorCategory.UNSPECIFIED)
                    .build();
        }
    }
}
