package io.temporal.ai.chat.model;


import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * ChatModelActivity is an interface for Temporal activities that interact with a chat model.
 */
@ActivityInterface
public interface ChatModelActivity {

    @ActivityMethod
    ChatModelTypes.ChatModelActivityOutput callChatModel(ChatModelTypes.ChatModelActivityInput input);
}
