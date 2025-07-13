package io.temporal.ai.workflows;


import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.temporal.activity.ActivityOptions;
import io.temporal.ai.chattools.DateTimeTools;
import io.temporal.ai.chat.client.TemporalChatClient;
import io.temporal.ai.chattools.AlarmTool;
import io.temporal.ai.mcp.McpToolCallback;
import io.temporal.ai.mcp.client.ActivityMcpClient;
import io.temporal.ai.mcp.client.McpClientActivity;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInit;
import io.temporal.ai.chat.model.ActivityChatModel;
import io.temporal.ai.chat.model.ChatModelActivity;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallback;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.MimeTypeUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ChatWorkflowImpl is a Temporal workflow implementation that uses the TemporalChatClient to interact with a chat model.
 */
public class ChatWorkflowImpl implements ChatWorkflow {
    private final McpClientActivity mcpClientActivity = Workflow.newActivityStub(McpClientActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3)
                            .build())
                    .build());
    private final ActivityMcpClient mcpClient = new ActivityMcpClient(mcpClientActivity);
    private final ChatClient chatClient;

    @WorkflowInit
    public ChatWorkflowImpl(String prompt) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        DateTimeTools dateTimeTools = Workflow.newActivityStub(DateTimeTools.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofSeconds(10))
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setMaximumAttempts(3)
                                .build())
                        .build());
        ChatModelActivity chatModelActivity = Workflow.newActivityStub(ChatModelActivity.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofMinutes(1))
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setMaximumAttempts(3)
                                .build())
                        .build());
        ActivityChatModel activityChatModel = new ActivityChatModel(chatModelActivity);
        this.chatClient = TemporalChatClient.builder(activityChatModel)
                .defaultTools(dateTimeTools, new AlarmTool())
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem(prompt)
                .build();
    }

    @Override
    public String startChat(String prompt) {
        // Just sleep a while so we can run some updates in the meantime
        Workflow.sleep(Duration.ofMinutes(5));
        return chatClient.prompt("How did the conversation go?").call().content();
    }

    @Override
    public String ask(String input) {
        List<ToolCallback> tc = mcpClient.listTools()
                .tools()
                .stream()
                .map(t -> new McpToolCallback(mcpClient, t))
                .map(mcpTc -> (ToolCallback) mcpTc)
                .toList();
        return chatClient
                .prompt()
                .user(input)
                .toolCallbacks(tc)
                .call()
                .content();
    }
}
