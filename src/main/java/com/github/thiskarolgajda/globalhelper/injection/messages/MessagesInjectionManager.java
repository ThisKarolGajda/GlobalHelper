package com.github.thiskarolgajda.globalhelper.injection.messages;

import com.github.thiskarolgajda.globalhelper.debug.PluginDebugger;
import com.github.thiskarolgajda.globalhelper.injection.DependencyInjection;
import com.github.thiskarolgajda.globalhelper.injection.Inject;
import com.github.thiskarolgajda.globalhelper.util.ClassFinder;

import java.lang.reflect.Field;
import java.util.Set;

public class MessagesInjectionManager {
    @Inject
    private static MessagesManager messagesManager;

    public MessagesInjectionManager() {
        autoInject();
    }

    public void autoInject() {
        Set<Class<?>> set = DependencyInjection.get(ClassFinder.class).findAllClassesUsingClassLoader();
        PluginDebugger debugger = DependencyInjection.get(PluginDebugger.class);

        for (Class<?> clazz : set) {
            debugger.debug("Found Class: " + clazz.getName());
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(MessageInjection.class)) {
                    MessageInjection messageInjection = field.getAnnotation(MessageInjection.class);
                    field.setAccessible(true);
                    try {
                        Object defaultTranslation = field.get(null);
                        if (defaultTranslation instanceof String string) {
                            debugger.debug("Found default translation String: " + string);
                            String key = field.getName();
                            String loadedMessage = messagesManager.getMessage(key);
                            if (loadedMessage == null) {
                                loadedMessage = string;
                            }

                            messagesManager.setMessage(key, string);
                            debugger.debug("Set default message in messages.yml for key: " + key);


                            messagesManager.addComment(key, "Default: " + string + ((!messageInjection.comment().isBlank()) ? "\n" + messageInjection.comment() : ""));
                            field.set(null, loadedMessage);
                            debugger.debug("Injected value into field: " + field.getName() + " of class: " + clazz.getName());
                        }
                    } catch (IllegalAccessException ignored) {
                        debugger.debug("Failed to inject value into field: " + field.getName() + " of class: " + clazz.getName());
                    }
                } else {
                    debugger.debug("Field " + field.getName() + " is not annotated with @MessageInjection.");
                }
            }
        }
    }
}