package io.temporal.ai.chattools;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.springframework.ai.tool.annotation.Tool;

@ActivityInterface
public interface DateTimeTools {
    @ActivityMethod
    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime();
}
