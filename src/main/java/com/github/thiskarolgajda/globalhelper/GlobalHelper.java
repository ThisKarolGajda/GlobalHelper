package com.github.thiskarolgajda.globalhelper;

import com.github.thiskarolgajda.globalhelper.debug.PluginDebugger;
import com.github.thiskarolgajda.globalhelper.injection.DependencyInjection;
import com.github.thiskarolgajda.globalhelper.injection.config.ConfigInjectionManager;
import com.github.thiskarolgajda.globalhelper.injection.config.ConfigManager;
import com.github.thiskarolgajda.globalhelper.injection.messages.MessagesInjectionManager;
import com.github.thiskarolgajda.globalhelper.injection.messages.MessagesManager;
import com.github.thiskarolgajda.globalhelper.util.ClassFinder;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public abstract class GlobalHelper extends JavaPlugin {

    @Override
    public void onEnable() {
        enable();

        // Dependency injection initialization
        DependencyInjection.register(new PluginDebugger(this));
        DependencyInjection.registerInject(new ClassFinder(this));
        DependencyInjection.autoInject(PluginDebugger.class);

        // Plugin initialization
        DependencyInjection.registerInject(this);

        // Files initialization
        DependencyInjection.registerInject(new MessagesManager(this));
        DependencyInjection.registerInject(new MessagesInjectionManager());
        DependencyInjection.registerInject(new ConfigManager(this));
        DependencyInjection.registerInject(new ConfigInjectionManager());
    }

    @Override
    public void onDisable() {
        disable();
        DependencyInjection.dispose();
    }

    @Override
    public void onLoad() {
        load();
    }

    abstract void load();

    abstract void enable();

    abstract void disable();

    public static GlobalHelper getInstance() {
        return DependencyInjection.get(GlobalHelper.class);
    }
}
