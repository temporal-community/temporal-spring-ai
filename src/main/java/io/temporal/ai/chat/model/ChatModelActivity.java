package io.temporal.ai.chat.model;


import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ChatModelActivity {

    @ActivityMethod
    ChatModelTypes.ChatModelActivityOutput callChatModel(ChatModelTypes.ChatModelActivityInput input);
}
