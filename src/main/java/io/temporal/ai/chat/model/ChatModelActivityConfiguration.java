package io.temporal.ai.chat.model;

import org.checkerframework.checker.units.qual.C;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;

import java.util.*;
import java.util.stream.Collectors;

public class ChatModelActivityConfiguration {
    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Bean
    Collection<ChatModelActivity> chatModelActivities(Map<String, ChatModel> chatModels) {
        List<ChatModelActivity> result = new ArrayList<>();
        for (String name : chatModels.keySet()) {
            ChatModel chatModel = chatModels.get(name);
            ChatModelActivity r = chatModelActivity(chatModel);
            String beanName = String.format("%s-%s", "chatModelActivity", name);
            Object b = this.beanFactory.initializeBean(r, beanName);
            this.beanFactory.autowireBean(b);
            this.beanFactory.registerSingleton(beanName, r);
            result.add(r);
        }
        return result;
    }

    ChatModelActivity chatModelActivity(ChatModel chatModel) {
        return new ChatModelActivityImpl(chatModel);
    }
}
