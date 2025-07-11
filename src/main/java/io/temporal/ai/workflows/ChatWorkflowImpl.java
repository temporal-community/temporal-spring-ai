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

public class ChatWorkflowImpl implements ChatWorkflow {
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
        return chatClient.prompt(input).call().content();
    }
}
