package com.github.thiskarolgajda.globalhelper.injection.config;

import com.github.thiskarolgajda.globalhelper.debug.PluginDebugger;
import com.github.thiskarolgajda.globalhelper.injection.DependencyInjection;
import com.github.thiskarolgajda.globalhelper.injection.Inject;
import com.github.thiskarolgajda.globalhelper.util.ClassFinder;

import java.lang.reflect.Field;
import java.util.Set;

public class ConfigInjectionManager {
    @Inject
    private static ConfigManager configManager;

    public ConfigInjectionManager() {
        autoInject();
    }

    public void autoInject() {
        Set<Class<?>> set = DependencyInjection.get(ClassFinder.class).findAllClassesUsingClassLoader();
        PluginDebugger debugger = DependencyInjection.get(PluginDebugger.class);

        for (Class<?> clazz : set) {
            debugger.debug("Found Class: " + clazz.getName());
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(ConfigInjection.class)) {
                    field.setAccessible(true);
                    try {
                        Object object = field.get(null);
                        debugger.debug("Found default object: " + object);
                        String key = field.getName();
                        Object loadedObject = configManager.get(key, object);
                        System.out.println("Loaded object: " + loadedObject + " -- " + object);
                        if (loadedObject == null) {
                            loadedObject = object;
                        }

                        configManager.set(key, loadedObject);
                        debugger.debug("Set default message in messages.yml for key: " + key);

                        configManager.addComment(key, "Default: " + object);
                        field.set(null, loadedObject);
                        debugger.debug("Injected value into field: " + field.getName() + " of class: " + clazz.getName());
                    } catch (IllegalAccessException e) {
                        debugger.debug("Failed to inject value into field: " + field.getName() + " of class: " + clazz.getName());
                    }
                } else {
                    debugger.debug("Field " + field.getName() + " is not annotated with @MessageInjection.");
                }
            }
        }
    }
}