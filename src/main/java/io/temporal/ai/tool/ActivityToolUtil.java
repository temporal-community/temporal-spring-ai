package io.temporal.ai.tool;

import com.google.common.collect.Lists;
import io.temporal.activity.ActivityInterface;
import io.temporal.common.metadata.POJOActivityInterfaceMetadata;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActivityToolUtil {

    /**
     * Extracts tool annotations from the given activity stub object.
     *
     * @param activityStub the tool object to extract annotations from
     * @return a map of tool names to Tool annotations
     */
    public static Map<String, Tool> getToolAnnotation(Object activityStub) {
        return Stream.of(activityStub.getClass().getInterfaces())
                .filter(activityInterface -> activityInterface.isAnnotationPresent(ActivityInterface.class))
                .map(POJOActivityInterfaceMetadata::newInstance)
                .flatMap(m ->m.getMethodsMetadata().stream())
                .filter(toolMethod -> toolMethod.getMethod().isAnnotationPresent(Tool.class))
                .collect(Collectors.toMap(
                        method -> method.getActivityTypeName(),
                        method -> method.getMethod().getAnnotation(Tool.class)
                ));
    }

    public static ToolCallback[] fromActivityStub(Object... toolObjects) {
        return Lists.newArrayList(toolObjects).stream()
                .map(toolObject -> Stream.of(toolObject.getClass().getInterfaces())
                        .filter(activityInterface -> activityInterface.isAnnotationPresent(ActivityInterface.class))
                        .flatMap(i -> Stream.of(ReflectionUtils.getDeclaredMethods(i)))
                        .filter(toolMethod -> toolMethod.isAnnotationPresent(Tool.class))
                        .filter(toolMethod -> !isFunctionalType(toolMethod))
                        .map(toolMethod -> MethodToolCallback.builder()
                                .toolDefinition(ToolDefinitions.from(toolMethod))
                                .toolMetadata(ToolMetadata.from(toolMethod))
                                .toolMethod(toolMethod)
                                .toolObject(toolObject)
                                .toolCallResultConverter(ToolUtils.getToolCallResultConverter(toolMethod))
                                .build())
                        .toArray(ToolCallback[]::new))
                .flatMap(Stream::of)
                .map(ActivityToolCallback::new)
                .toArray(ToolCallback[]::new);
    }

    private static boolean isFunctionalType(Method toolMethod) {
        var isFunction = ClassUtils.isAssignable(toolMethod.getReturnType(), Function.class)
                || ClassUtils.isAssignable(toolMethod.getReturnType(), Supplier.class)
                || ClassUtils.isAssignable(toolMethod.getReturnType(), Consumer.class);

//        if (isFunction) {
//            logger.warn("Method {} is annotated with @Tool but returns a functional type. "
//                    + "This is not supported and the method will be ignored.", toolMethod.getName());
//        }

        return isFunction;
    }
}
