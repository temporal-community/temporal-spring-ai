package io.temporal.ai.chattools;

import io.temporal.ai.tool.DeterministicTool;
import io.temporal.workflow.Workflow;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@DeterministicTool
public class AlarmTool {
    @Tool(description = "Set a user alarm for the given time")
    void setAlarm(@ToolParam(description = "Time in ISO-8601 format") String time) {
        // Call in a side effect so it is deterministic
        Workflow.sideEffect(Void.class, () -> {
            LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
            System.out.println("Alarm set for " + alarmTime);
            return null;
        });
    }

}