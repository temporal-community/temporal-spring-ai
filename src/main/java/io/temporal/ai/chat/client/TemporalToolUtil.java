package io.temporal.ai.chat.client;

import io.temporal.activity.ActivityOptions;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.ai.reflection.TemporalWorkflowReflectionUtil;
import io.temporal.ai.tool.ActivityToolUtil;
import io.temporal.ai.tool.DeterministicTool;
import io.temporal.ai.tool.NexusToolUtil;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TemporalToolUtil {

    static List<ToolCallback> convertTools(Object... toolObjects) {
        List<ToolCallback> toolCallbacks = new ArrayList<>();
        for (Object toolObject : toolObjects) {
            if (TemporalWorkflowReflectionUtil.isActivityStub(toolObject)) {
                Map<String, Tool> toolsMethod = ActivityToolUtil.getToolAnnotation(toolObject);
                Map<String, ActivityOptions> activityOptionsMap = new HashMap<>();
                toolsMethod.forEach((method, tool) -> {
                    activityOptionsMap.put(
                            method,
                            ActivityOptions.newBuilder()
                                    .setSummary(tool.description()).build()
                    );
                });
                ToolCallback[] activityToolCallbacks = ActivityToolUtil.fromActivityStub(toolObject);
                toolCallbacks.addAll(List.of(activityToolCallbacks));
            } else if (TemporalWorkflowReflectionUtil.isLocalActivityStub(toolObject)) {
                Map<String, Tool> toolsMethod = ActivityToolUtil.getToolAnnotation(toolObject);
                Map<String, LocalActivityOptions> activityOptionsMap = new HashMap<>();
                toolsMethod.forEach((method, tool) -> {
                    activityOptionsMap.put(
                            method,
                            LocalActivityOptions.newBuilder()
                                    .setSummary(tool.description()).build()
                    );
                });
                ToolCallback[] activityToolCallbacks = ActivityToolUtil.fromActivityStub(toolObject);
                toolCallbacks.addAll(List.of(activityToolCallbacks));
            } else if (TemporalWorkflowReflectionUtil.isChildWorkflowStub(toolObject)) {
                throw new UnsupportedOperationException("Child workflow stubs are not supported in TemporalChatClientBuilder");
            } else if (TemporalWorkflowReflectionUtil.isNexusServiceStub(toolObject)) {
                ToolCallback[] nexusServiceToolCallbacks = NexusToolUtil.fromNexusServiceStub(toolObject);
                toolCallbacks.addAll(List.of(nexusServiceToolCallbacks));
            } else if (toolObject.getClass().isAnnotationPresent(DeterministicTool.class)) {
                toolCallbacks.addAll(List.of(ToolCallbacks.from(toolObject)));
            } else {
                throw new IllegalArgumentException("Tool object is not deterministic");
            }
        }
        return toolCallbacks;
    }
}
