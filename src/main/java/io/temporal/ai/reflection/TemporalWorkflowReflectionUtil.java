package io.temporal.ai.reflection;

import io.temporal.activity.ActivityInterface;

import java.lang.reflect.Proxy;
import java.util.stream.Stream;

public class TemporalWorkflowReflectionUtil {

    public static boolean isActivityStub(Object object) {
        return object != null && Proxy.isProxyClass(object.getClass()) && Proxy.getInvocationHandler(object)
                .getClass().getName().contains("ActivityInvocationHandler");
    }

    public static boolean isLocalActivityStub(Object object) {
        return object != null && Proxy.isProxyClass(object.getClass()) && Proxy.getInvocationHandler(object)
                .getClass().getName().contains("LocalActivityInvocationHandler");
    }

    public static boolean isChildWorkflowStub(Object object) {
        return object != null && Proxy.isProxyClass(object.getClass()) && Proxy.getInvocationHandler(object)
                .getClass().getName().contains("ChildWorkflowInvocationHandler");
    }

    public static boolean isNexusServiceStub(Object object) {
        return object != null && Proxy.isProxyClass(object.getClass()) && Proxy.getInvocationHandler(object)
                .getClass().getName().contains("NexusServiceInvocationHandler");
        }

    public static Class<?> extractActivityClass(Object activityStub) {
        if (activityStub == null) {
            throw new IllegalArgumentException("Activity stub cannot be null");
        }
        if (!isActivityStub(activityStub)) {
            throw new IllegalArgumentException("Provided object is not an activity stub");
        }
        return Stream.of(activityStub.getClass().getInterfaces()).filter(i ->
                i.isAnnotationPresent(ActivityInterface.class)
        ).findFirst().orElse(null);
    }
}
