package com.github.thiskarolgajda.globalhelper.debug;

import org.bukkit.plugin.Plugin;

public class PluginDebugger {
    private final Plugin plugin;

    public PluginDebugger(Plugin plugin) {
        this.plugin = plugin;
    }

    public void debug(String message) {
        // todo check if debug is activated
        //plugin.getLogger().info("[DEBUG] " + message);
    }
}
