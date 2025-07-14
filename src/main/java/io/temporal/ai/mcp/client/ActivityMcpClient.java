package io.temporal.ai.mcp.client;

import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Map;

public class ActivityMcpClient {
    private final McpClientActivity mcpClientActivity;
    private Map<String, McpSchema.ServerCapabilities> serverCapabilities;
    private Map<String, McpSchema.Implementation> clientInfo;

    public ActivityMcpClient(McpClientActivity mcpClientActivity) {
        this.mcpClientActivity = mcpClientActivity;
    }

    public Map<String, McpSchema.ServerCapabilities> getServerCapabilities() {
        // TODO Technically even in a workflow this can kind of race still
        if (serverCapabilities == null) {
            serverCapabilities = mcpClientActivity.getServerCapabilities();
        }
        return serverCapabilities;
    }

    public Map<String, McpSchema.Implementation> getClientInfo() {
        // TODO Technically even in a workflow this can kind of race still
        if (clientInfo == null) {
            clientInfo = mcpClientActivity.getClientInfo();
        }
        return clientInfo;

    }

    public McpSchema.CallToolResult callTool(String clientName, McpSchema.CallToolRequest callToolRequest) {
        return mcpClientActivity.callTool(clientName, callToolRequest);
    }

    public Map<String, McpSchema.ListToolsResult> listTools() {
        return mcpClientActivity.listTools();
    }
}
