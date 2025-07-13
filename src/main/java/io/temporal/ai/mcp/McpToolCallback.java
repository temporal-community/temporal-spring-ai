package io.temporal.ai.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.temporal.ai.mcp.client.ActivityMcpClient;
import io.temporal.ai.mcp.client.McpClientActivity;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.Map;

public class McpToolCallback implements ToolCallback {
    private final ActivityMcpClient client;

    private final McpSchema.Tool tool;

    public McpToolCallback(ActivityMcpClient client, McpSchema.Tool tool) {
        this.client = client;
        this.tool = tool;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return DefaultToolDefinition.builder()
                .name(McpToolUtils.prefixedToolName(this.client.getClientInfo().name(), this.tool.name()))
                .description(this.tool.description())
                .inputSchema(ModelOptionsUtils.toJsonString(this.tool.inputSchema()))
                .build();
    }

    @Override
    public String call(String functionInput) {
        Map<String, Object> arguments = ModelOptionsUtils.jsonToMap(functionInput);
        // Note that we use the original tool name here, not the adapted one from
        // getToolDefinition
        McpSchema.CallToolResult response = this.client.callTool(new McpSchema.CallToolRequest(this.tool.name(), arguments));
        if (response.isError() != null && response.isError()) {
            throw new IllegalStateException("Error calling tool: " + response.content());
        }
        return ModelOptionsUtils.toJsonString(response.content());
    }

    @Override
    public String call(String toolArguments, ToolContext toolContext) {
        // ToolContext is not supported by the MCP tools
        return this.call(toolArguments);
    }
}
