package com.github.thiskarolgajda.globalhelper.injection.messages;

import com.github.thiskarolgajda.globalhelper.injection.file.FileManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class MessagesManager {
    private final FileManager fileManager;

    public MessagesManager(@NotNull Plugin plugin) {
        this.fileManager = new FileManager(plugin, "messages.yml");
    }

    public String getMessage(String key) {
        return fileManager.getValue(key);
    }

    public void setMessage(String key, String message) {
        fileManager.setValue(key, message);
    }

    public void addComment(String key, @NotNull String comment) {
        fileManager.addComment(key, comment);
    }

    @SuppressWarnings("unused")
    public void reload() {
        fileManager.reload();
    }
}