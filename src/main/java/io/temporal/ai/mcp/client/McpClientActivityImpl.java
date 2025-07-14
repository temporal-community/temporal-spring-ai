package io.temporal.ai.mcp.client;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.temporal.failure.ApplicationFailure;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * McpClientActivityImpl implements the McpClientActivity interface, providing
 * access to MCP clients registered in the worker.
 */
@Component
public class McpClientActivityImpl implements McpClientActivity {
    private final Map<String, McpSyncClient> mcpSyncClients;

    public McpClientActivityImpl(List<McpSyncClient> mcpSyncClients) {
        this.mcpSyncClients = mcpSyncClients.stream().collect(Collectors.toMap(
                c -> c.getClientInfo().name(),
                c -> c
        ));
    }

    @Override
    public Map<String, McpSchema.ServerCapabilities> getServerCapabilities() {
        return mcpSyncClients.keySet().stream().collect(Collectors.toMap(
                clientName -> clientName,
                clientName -> mcpSyncClients.get(clientName).getServerCapabilities()
        ));
    }

    @Override
    public Map<String, McpSchema.Implementation> getClientInfo() {
        return mcpSyncClients.keySet().stream().collect(Collectors.toMap(
                clientName -> clientName,
                clientName -> mcpSyncClients.get(clientName).getClientInfo()
        ));
    }

    @Override
    public McpSchema.CallToolResult callTool(String clientName, McpSchema.CallToolRequest callToolRequest) {
        if (!mcpSyncClients.containsKey(clientName)) {
            throw ApplicationFailure.newBuilder()
                    .setType("ClientNotFound")
                    .setMessage("MCP Client " + clientName + " not found")
                    .setNonRetryable(true)
                    .build();
        }
        return mcpSyncClients.get(clientName).callTool(callToolRequest);
    }

    @Override
    public Map<String, McpSchema.ListToolsResult> listTools() {
        return mcpSyncClients.keySet().stream().collect(Collectors.toMap(
                clientName -> clientName,
                clientName -> mcpSyncClients.get(clientName).listTools()
        ));
    }
}
