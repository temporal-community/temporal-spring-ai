package io.temporal.ai.mcp.client;


import io.modelcontextprotocol.spec.McpSchema;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface McpClientActivity {
    McpSchema.ServerCapabilities getServerCapabilities();

    McpSchema.Implementation getClientInfo();

    McpSchema.CallToolResult callTool(McpSchema.CallToolRequest callToolRequest);

    McpSchema.ListToolsResult listTools();
}
