package io.temporal.ai.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

public class ActivityToolCallback implements ToolCallback {
    private final ToolCallback toolCallback;

    public ActivityToolCallback(ToolCallback toolCallback) {
        this.toolCallback = toolCallback;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return toolCallback.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return toolCallback.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        return toolCallback.call(toolInput);
    }

    @Override
    public String call(String toolInput, ToolContext tooContext) {
        return toolCallback.call(toolInput, tooContext);
    }
}
