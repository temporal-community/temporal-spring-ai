package io.temporal.ai.tool;

import org.springframework.stereotype.Component;

@Component
public class ExecuteToolLocalActivityImpl implements ExecuteToolLocalActivity {
    @Override
    public String call(String toolCallbackId, String toolInput) {
        return LocalActivityToolCallbackWrapper.toolCallbackMap.get(toolCallbackId).call(toolInput);
    }
}
