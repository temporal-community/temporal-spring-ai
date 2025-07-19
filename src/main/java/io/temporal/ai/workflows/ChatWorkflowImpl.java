package io.temporal.ai.workflows;


import io.temporal.activity.ActivityOptions;
import io.temporal.ai.chattools.DateTimeTools;
import io.temporal.ai.chat.client.TemporalChatClient;
import io.temporal.ai.chattools.AlarmTool;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInit;
import io.temporal.ai.chat.model.ActivityChatModel;
import io.temporal.ai.chat.model.ChatModelActivity;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

import java.time.Duration;

/**
 * ChatWorkflowImpl is a Temporal workflow implementation that uses the TemporalChatClient to interact with a chat model.
 */
public class ChatWorkflowImpl implements ChatWorkflow {
    private boolean isChatEnded = false;
    private final ChatClient chatClient;

    @WorkflowInit
    public ChatWorkflowImpl(String prompt) {
        // Create a chat memory to store the conversation history so the conversation can be preserved across `ask` calls.
        ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        // Create an activity stub to be used as a tool to get the current date and time.
        DateTimeTools dateTimeTools = Workflow.newActivityStub(DateTimeTools.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofSeconds(10))
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setMaximumAttempts(3)
                                .build())
                        .build());
        // Create an activity stub to be used to call the chat model.
        ChatModelActivity chatModelActivity = Workflow.newActivityStub(ChatModelActivity.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofMinutes(1))
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setMaximumAttempts(3)
                                .build())
                        .build());
        ActivityChatModel activityChatModel = new ActivityChatModel(chatModelActivity);
        // Build the TemporalChatClient with the chat model, tools, and chat memory advisor.
        this.chatClient = TemporalChatClient.builder(activityChatModel)
                .defaultTools(dateTimeTools, new AlarmTool())
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem(prompt)
                .build();
    }

    @Override
    public String startChat(String prompt) {
        // Wait for the conversation to finish and all handlers to complete
        Workflow.await(() -> isChatEnded && Workflow.isEveryHandlerFinished());
        return chatClient.prompt("How did the conversation go?").call().content();
    }

    @Override
    public String ask(String input) {
        return chatClient
                .prompt()
                .user(input)
                .call()
                .content();
    }

    @Override
    public void endChat() {
        isChatEnded = true;
    }
}
