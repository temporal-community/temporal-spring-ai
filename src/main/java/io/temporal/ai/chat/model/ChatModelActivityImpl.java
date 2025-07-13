package io.temporal.ai.chat.model;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

// TODO: Make this conditional on the presence of a ChatModel bean
public class ChatModelActivityImpl implements ChatModelActivity {
    ChatModel chatModel;

    ChatModelActivityImpl(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ChatModelTypes.ChatModelActivityOutput callChatModel(ChatModelTypes.ChatModelActivityInput input) {
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
    }
}
