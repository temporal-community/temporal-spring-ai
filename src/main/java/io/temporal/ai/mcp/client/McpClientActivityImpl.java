package io.temporal.ai.mcp.client;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class McpClientActivityImpl implements McpClientActivity {
    private final McpSyncClient mcpSyncClient;

    public McpClientActivityImpl(List<McpSyncClient> mcpSyncClients) {
        // TODO: Handle multiple clients if necessary
        this.mcpSyncClient = mcpSyncClients.get(0);
    }

    @Override
    public McpSchema.ServerCapabilities getServerCapabilities() {
        return mcpSyncClient.getServerCapabilities();
    }

    @Override
    public McpSchema.Implementation getClientInfo() {
        return mcpSyncClient.getClientInfo();
    }

    @Override
    public McpSchema.CallToolResult callTool(McpSchema.CallToolRequest callToolRequest) {
        return mcpSyncClient.callTool(callToolRequest);
    }

    @Override
    public McpSchema.ListToolsResult listTools() {
        return mcpSyncClient.listTools();
    }
}
