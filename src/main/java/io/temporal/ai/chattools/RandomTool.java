package io.temporal.ai.chattools;

import org.springframework.ai.tool.annotation.Tool;

public class RandomTool {
    @Tool(description = "Generate a random number between 0 and 1")
    public String randomNumber() {
        return String.valueOf(Math.random());
    }
}
