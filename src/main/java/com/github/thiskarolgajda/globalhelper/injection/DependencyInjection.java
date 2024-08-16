package com.github.thiskarolgajda.globalhelper.injection;

import com.github.thiskarolgajda.globalhelper.debug.PluginDebugger;
import com.github.thiskarolgajda.globalhelper.util.ClassFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyInjection {
    private static final Map<Class<?>, Object> container = new ConcurrentHashMap<>();

    public static <C> void register(C instance) {
        container.put(instance.getClass(), instance);
    }

    public static <C> void registerInject(C instance) {
        container.put(instance.getClass(), instance);
        autoInject(instance.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <C> C get(Class<C> clazz) {
        return (C) container.get(clazz);
    }

    static void inject(@NotNull Class<?> clazz, @Nullable Class<?> injecting) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (injecting != null && !field.getType().equals(injecting)) {
                continue;
            }

            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                try {
                    Object dependency = get(field.getType());
                    if (dependency != null) {
                        field.set(null, dependency);
                    } else {
                        DependencyInjection.get(PluginDebugger.class).debug("No dependency found for: " + field.getType().getName());
                    }
                } catch (IllegalAccessException e) {
                    DependencyInjection.get(PluginDebugger.class).debug("Failed to inject dependency into: " + field.getName());
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public static void autoInject() {
        Set<Class<?>> classes = get(ClassFinder.class).findAllClassesUsingClassLoader();
        for (Class<?> clazz : classes) {
            inject(clazz, null);
        }
    }

    public static void autoInject(Class<?> injecting) {
        Set<Class<?>> classes = get(ClassFinder.class).findAllClassesUsingClassLoader();
        for (Class<?> clazz : classes) {
            inject(clazz, injecting);
        }
    }

    public static void dispose() {
        container.clear();
    }
}