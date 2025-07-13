package io.temporal.ai.chat.model;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

class RemoteToolCallback implements ToolCallback {
    private final ToolDefinition toolDefinition;

    public RemoteToolCallback(ToolDefinition toolDefinition) {
        this.toolDefinition = toolDefinition;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return toolDefinition;
    }

    @Override
    public String call(String toolInput) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
