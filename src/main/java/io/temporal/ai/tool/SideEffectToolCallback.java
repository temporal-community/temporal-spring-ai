package io.temporal.ai.tool;

import io.temporal.workflow.Workflow;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

public class SideEffectToolCallback  implements ToolCallback {
    private final ToolCallback toolCallback;

    public SideEffectToolCallback(ToolCallback toolCallback) {
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
        return Workflow.sideEffect(String.class, () -> toolCallback.call(toolInput));
    }

    @Override
    public String call(String toolInput, ToolContext tooContext) {
        return Workflow.sideEffect(String.class, () -> toolCallback.call(toolInput, tooContext));
    }
}
