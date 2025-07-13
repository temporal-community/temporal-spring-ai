package io.temporal.ai.vectorstore;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VectorStoreActivityBeanRegistrar implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        // Register a dynamic bean with a dependency
        DefaultListableBeanFactory listableRegistry = (DefaultListableBeanFactory) registry;
        List<String> vectorStoreBeanNames = List.of(listableRegistry.getBeanNamesForType(VectorStore.class));
        for (String vectorStoreBeanName : vectorStoreBeanNames) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(VectorStoreActivityImpl.class)
                    .addConstructorArgReference(vectorStoreBeanName);
            String activityBeanName = String.format("%s%s", vectorStoreBeanName, "Activity");
            registry.registerBeanDefinition(activityBeanName, builder.getBeanDefinition());
            // Register an alias for the activity bean if there's only one vector store or if it's primary
            if (vectorStoreBeanNames.size() == 1 || listableRegistry.getBeanDefinition(vectorStoreBeanName).isPrimary()) {
                registry.registerAlias(activityBeanName, "vectorStoreActivity");
            }
        }
    }
}