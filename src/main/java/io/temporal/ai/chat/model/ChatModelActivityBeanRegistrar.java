package io.temporal.ai.chat.model;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ChatModelActivityBeanRegistrar implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        // Register a dynamic bean with a dependency
        DefaultListableBeanFactory listableRegistry = (DefaultListableBeanFactory) registry;
        List<String> chatModelBeanNames = List.of(listableRegistry.getBeanNamesForType(ChatModel.class));
        for (String chatModelBeanName : chatModelBeanNames) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ChatModelActivityImpl.class)
                    .addConstructorArgReference(chatModelBeanName);
            String activityBeanName = String.format("%s%s", chatModelBeanName, "Activity");
            registry.registerBeanDefinition(activityBeanName, builder.getBeanDefinition());
            // Register an alias for the activity bean if there's only one chat model or if it's primary
            // TODO: Maybe add a property to control the bean selection logic
            if (chatModelBeanNames.size() == 1 || listableRegistry.getBeanDefinition(chatModelBeanName).isPrimary()) {
                registry.registerAlias(activityBeanName, "chatModelActivity");
            }
        }
    }
}