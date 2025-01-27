package com.github.thiskarolgajda.globalhelper.injection.config;

import com.github.thiskarolgajda.globalhelper.injection.file.FileManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ConfigManager {
    private final FileManager fileManager;

    public ConfigManager(@NotNull Plugin plugin) {
        this.fileManager = new FileManager(plugin, "config.yml");
    }

    public <C> C get(String key, C defaultObject) {
        return fileManager.getValue(key, defaultObject);
    }

    public void set(String key, Object object) {
        fileManager.setValue(key, object);
    }

    public void addComment(String key, @NotNull String comment) {
        fileManager.addComment(key, comment);
    }

    @SuppressWarnings("unused")
    public void reload() {
        fileManager.reload();
    }
}