package io.temporal.ai.chat.client;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.DefaultChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.content.Media;
import org.springframework.ai.template.TemplateRenderer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

public class TemporalChatClient extends DefaultChatClient {
    private final DefaultChatClientRequestSpec defaultChatClientRequest;

    public TemporalChatClient(DefaultChatClientRequestSpec defaultChatClientRequest) {
        super(defaultChatClientRequest);
        this.defaultChatClientRequest = defaultChatClientRequest;
    }

    public static Builder builder(ChatModel chatModel) {
        return builder(chatModel, ObservationRegistry.NOOP, null);
    }

    static Builder builder(ChatModel chatModel, ObservationRegistry observationRegistry,
                           @Nullable ChatClientObservationConvention customObservationConvention) {
        Assert.notNull(chatModel, "chatModel cannot be null");
        Assert.notNull(observationRegistry, "observationRegistry cannot be null");
        return new TemporalChatClientBuilder(chatModel, observationRegistry, customObservationConvention);
    }

    public static class TemporalChatClientRequestSpec extends DefaultChatClient.DefaultChatClientRequestSpec {

        public TemporalChatClientRequestSpec(ChatModel chatModel, String userText, Map<String, Object> userParams, String systemText, Map<String, Object> systemParams, List<ToolCallback> toolCallbacks, List<Message> messages, List<String> toolNames, List<Media> media, ChatOptions chatOptions, List<Advisor> advisors, Map<String, Object> advisorParams, ObservationRegistry observationRegistry, ChatClientObservationConvention observationConvention, Map<String, Object> toolContext, TemplateRenderer templateRenderer) {
            super(chatModel, userText, userParams, systemText, systemParams, toolCallbacks, messages, toolNames, media, chatOptions, advisors, advisorParams, observationRegistry, observationConvention, toolContext, templateRenderer);
        }
    }
}
