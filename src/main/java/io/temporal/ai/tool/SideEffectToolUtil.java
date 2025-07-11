package io.temporal.ai.tool;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

import java.util.Arrays;

public class SideEffectToolUtil {

    public static ToolCallback[] from(Object... toolObjects) {
        return Arrays.stream(ToolCallbacks.from(toolObjects)).map(SideEffectToolCallback::new).toArray(ToolCallback[]::new);
    }
}
