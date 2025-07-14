package io.temporal.ai.mcp.client;


import io.modelcontextprotocol.spec.McpSchema;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.List;
import java.util.Map;

/**
 * McpClientActivity defines the activities for interacting with a set of MCP clients on the worker.
 */
@ActivityInterface(namePrefix = "MCP-Client-")
public interface McpClientActivity {
    Map<String, McpSchema.ServerCapabilities> getServerCapabilities();

    Map<String, McpSchema.Implementation> getClientInfo();

    McpSchema.CallToolResult callTool(String clientName, McpSchema.CallToolRequest callToolRequest);

    Map<String,McpSchema.ListToolsResult> listTools();
}
