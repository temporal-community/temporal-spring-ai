package io.temporal.ai.tool;

import io.temporal.activity.LocalActivityOptions;
import io.temporal.workflow.Workflow;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LocalActivityToolCallbackWrapper implements ToolCallback {
    public static final Map<String, ToolCallback> toolCallbackMap = new ConcurrentHashMap<>();
    private final ToolCallback toolCallback;
    private final ExecuteToolLocalActivity stub;

    public LocalActivityToolCallbackWrapper(ToolCallback toolCallback) {
        // Generate a random UUID
        this.toolCallback = toolCallback;
        this.stub = Workflow.newLocalActivityStub(
                ExecuteToolLocalActivity.class,
                LocalActivityOptions.newBuilder()
                        .setDoNotIncludeArgumentsIntoMarker(true)
                        .setStartToCloseTimeout(Duration.ofSeconds(10))
                        .build()
        );
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
        UUID toolCallbackId = UUID.randomUUID();
        try {
            toolCallbackMap.put(toolCallbackId.toString(), toolCallback);
            return stub.call(toolCallbackId.toString(), toolInput);
        } finally {
            toolCallbackMap.remove(toolCallbackId.toString());
        }

    }
}
