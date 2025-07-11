package io.temporal.ai.tool;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ExecuteToolLocalActivity {

    @ActivityMethod
    String call(String toolCallbackId, String toolInput);
}
