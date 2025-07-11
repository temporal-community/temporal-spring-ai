package io.temporal.ai.chat.client.advisor;

import io.temporal.ai.tool.ActivityToolCallback;
import io.temporal.ai.tool.LocalActivityToolCallbackWrapper;
import io.temporal.ai.tool.SideEffectToolCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;

public class SandboxingAdvisor implements CallAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiChatModel.class);

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        var prompt = chatClientRequest.prompt();
        if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
            var toolCallbacks = toolCallingChatOptions.getToolCallbacks();
            toolCallingChatOptions.setToolCallbacks(toolCallbacks.stream().map( tc -> {
                if (tc instanceof ActivityToolCallback || tc instanceof SideEffectToolCallback) {
                    return tc;
                } else {
                    // Log a warning or handle the case where the tool callback is not deterministic
                    logger.warn("Tool callbacks is not guaranteed to be deterministic: {}, trying to convert to local activity", tc.getClass().getName());
                    return new LocalActivityToolCallbackWrapper(tc);
                }
            }).toList());
        }
        return callAdvisorChain.nextCall(chatClientRequest);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER;
    }
}
