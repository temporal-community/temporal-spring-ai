package io.temporal.ai.mcp.client;

import io.modelcontextprotocol.spec.McpSchema;

public class ActivityMcpClient {
    private final McpClientActivity mcpClientActivity;
    private McpSchema.ServerCapabilities serverCapabilities;
    private McpSchema.Implementation clientInfo;

    public ActivityMcpClient(McpClientActivity mcpClientActivity) {
        this.mcpClientActivity = mcpClientActivity;
    }

    public McpSchema.ServerCapabilities getServerCapabilities() {
        // TODO Technically even in a workflow this can kind of race still
        if (serverCapabilities == null) {
            serverCapabilities = mcpClientActivity.getServerCapabilities();
        }
        return serverCapabilities;
    }

    public McpSchema.Implementation getClientInfo() {
        // TODO Technically even in a workflow this can kind of race still
        if (clientInfo == null) {
            clientInfo = mcpClientActivity.getClientInfo();
        }
        return clientInfo;

    }

    public McpSchema.CallToolResult callTool(McpSchema.CallToolRequest callToolRequest) {
        return mcpClientActivity.callTool(callToolRequest);
    }

    public McpSchema.ListToolsResult listTools() {
        return mcpClientActivity.listTools();
    }
}
