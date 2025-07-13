package io.temporal.ai.chat.client;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClientBuilder;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.util.Assert;

import java.util.Map;

public class TemporalChatClientBuilder extends DefaultChatClientBuilder {
    private static final Logger logger = LoggerFactory.getLogger(TemporalChatClientBuilder.class);

    public TemporalChatClientBuilder(ChatModel chatModel, ObservationRegistry observationRegistry, ChatClientObservationConvention customObservationConvention) {
        super(chatModel, observationRegistry, customObservationConvention);
    }

    @Override
    public ChatClient.Builder defaultTools(Object... toolObjects) {
        Assert.notNull(toolObjects, "toolObjects cannot be null");
        Assert.noNullElements(toolObjects, "toolObjects cannot contain null elements");
        this.defaultRequest.toolCallbacks(TemporalToolUtil.convertTools(toolObjects));
        return this;
    }

    @Override
    public ChatClient.Builder defaultToolContext(Map<String, Object> toolContext) {
        throw new UnsupportedOperationException("defaultToolContext is not supported in TemporalChatClientBuilder");
    }
}
