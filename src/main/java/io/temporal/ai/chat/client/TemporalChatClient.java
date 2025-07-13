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
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TemporalChatClient extends DefaultChatClient {
    private final DefaultChatClientRequestSpec defaultChatClientRequest;

    public TemporalChatClient(DefaultChatClientRequestSpec defaultChatClientRequest) {
        super(defaultChatClientRequest);
        this.defaultChatClientRequest = defaultChatClientRequest;
    }

    public static Builder builder(ChatModel chatModel) {
        return builder(chatModel, ObservationRegistry.NOOP, null);
    }

    static Builder builder(
            ChatModel chatModel,
            ObservationRegistry observationRegistry,
                           @Nullable ChatClientObservationConvention customObservationConvention) {
        Assert.notNull(chatModel, "chatModel cannot be null");
        Assert.notNull(observationRegistry, "observationRegistry cannot be null");
        return new TemporalChatClientBuilder(chatModel, observationRegistry, customObservationConvention);
    }

    public static class TemporalChatClientRequestSpec implements ChatClientRequestSpec {

        private final DefaultChatClientRequestSpec defaultChatClientRequestSpec;

        public TemporalChatClientRequestSpec(ChatModel chatModel, String userText, Map<String, Object> userParams, String systemText, Map<String, Object> systemParams, List<ToolCallback> toolCallbacks, List<Message> messages, List<String> toolNames, List<Media> media, ChatOptions chatOptions, List<Advisor> advisors, Map<String, Object> advisorParams, ObservationRegistry observationRegistry, ChatClientObservationConvention observationConvention, Map<String, Object> toolContext, TemplateRenderer templateRenderer) {
            defaultChatClientRequestSpec = new DefaultChatClientRequestSpec(chatModel, userText, userParams, systemText, systemParams, toolCallbacks, messages, toolNames, media, chatOptions, advisors, advisorParams, observationRegistry, observationConvention, toolContext, templateRenderer);
        }

        public Builder mutate() {
            return defaultChatClientRequestSpec.mutate();
        }

        public ChatClientRequestSpec advisors(Consumer<AdvisorSpec> consumer) {
            return defaultChatClientRequestSpec.advisors(consumer);
        }

        public ChatClientRequestSpec advisors(Advisor... advisors) {
            return defaultChatClientRequestSpec.advisors(advisors);
        }

        public ChatClientRequestSpec advisors(List<Advisor> advisors) {
            return defaultChatClientRequestSpec.advisors(advisors);
        }

        public ChatClientRequestSpec messages(Message... messages) {
            return defaultChatClientRequestSpec.messages(messages);
        }

        public ChatClientRequestSpec messages(List<Message> messages) {
            return defaultChatClientRequestSpec.messages(messages);
        }

        public <T extends ChatOptions> ChatClientRequestSpec options(T options) {
            return defaultChatClientRequestSpec.options(options);
        }

        public ChatClientRequestSpec toolNames(String... toolNames) {
            return defaultChatClientRequestSpec.toolNames(toolNames);
        }

        public ChatClientRequestSpec toolCallbacks(ToolCallback... toolCallbacks) {
            return defaultChatClientRequestSpec.toolCallbacks(toolCallbacks);
        }

        public ChatClientRequestSpec toolCallbacks(List<ToolCallback> toolCallbacks) {
            return defaultChatClientRequestSpec.toolCallbacks(toolCallbacks);
        }

        public ChatClientRequestSpec tools(Object... toolObjects) {
            return defaultChatClientRequestSpec.tools(TemporalToolUtil.convertTools(toolObjects));
        }

        public ChatClientRequestSpec toolCallbacks(ToolCallbackProvider... toolCallbackProviders) {
            return defaultChatClientRequestSpec.toolCallbacks(toolCallbackProviders);
        }

        public ChatClientRequestSpec toolContext(Map<String, Object> toolContext) {
            throw new UnsupportedOperationException("Tool context is not supported in TemporalChatClientRequestSpec");
            // return defaultChatClientRequestSpec.toolContext(toolContext);
        }

        public ChatClientRequestSpec system(String text) {
            return defaultChatClientRequestSpec.system(text);
        }

        public ChatClientRequestSpec system(Resource text, Charset charset) {
            return defaultChatClientRequestSpec.system(text, charset);
        }

        public ChatClientRequestSpec system(Resource text) {
            return defaultChatClientRequestSpec.system(text);
        }

        public ChatClientRequestSpec system(Consumer<PromptSystemSpec> consumer) {
            return defaultChatClientRequestSpec.system(consumer);
        }

        public ChatClientRequestSpec user(String text) {
            return defaultChatClientRequestSpec.user(text);
        }

        public ChatClientRequestSpec user(Resource text, Charset charset) {
            return defaultChatClientRequestSpec.user(text, charset);
        }

        public ChatClientRequestSpec user(Resource text) {
            return defaultChatClientRequestSpec.user(text);
        }

        public ChatClientRequestSpec user(Consumer<PromptUserSpec> consumer) {
            return defaultChatClientRequestSpec.user(consumer);
        }

        public ChatClientRequestSpec templateRenderer(TemplateRenderer templateRenderer) {
            return defaultChatClientRequestSpec.templateRenderer(templateRenderer);
        }

        public CallResponseSpec call() {
            return defaultChatClientRequestSpec.call();
        }

        public StreamResponseSpec stream() {
            throw new UnsupportedOperationException("Streaming is not supported in TemporalChatClientRequestSpec");
        }
    }
}
