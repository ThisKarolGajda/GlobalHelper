package com.github.thiskarolgajda.globalhelper.injection.file;

import com.github.thiskarolgajda.globalhelper.util.Helper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class FileManager extends Helper {
    private final Plugin plugin;
    private File configFile;
    private FileConfiguration config;

    public FileManager(@NotNull Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), fileName);
        this.config = YamlConfiguration.loadConfiguration(configFile);
        createConfigFile();
        loadConfig();
    }

    private void createConfigFile() {
        if (!configFile.exists()) {
            try {
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }

                configFile.createNewFile();
                config.save(configFile);
            } catch (IOException e) {
                debug("Could not create config file: " + configFile.getAbsolutePath());
            }
        }
    }

    private void loadConfig() {
        try {
            config.load(configFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            debug("Failed to load config file: " + configFile.getAbsolutePath());
        }
    }

    public <T> T getValue(String key, T defaultValue) {
        return (T) config.get(key, defaultValue);
    }

    public <T> T getValue(String key) {
        return (T) config.get(key);
    }

    public <T> void setValue(String key, T value) {
        config.set(key, value);
        saveConfig();
    }

    public void addComment(String key, @NotNull String comment) {
        try {
            List<String> lines = Files.readAllLines(configFile.toPath());
            String commentLine = "# " + comment.replace("\n", "\n# ");

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith(key)) {
                    int backIndex = i - 1;
                    List<String> comments = new ArrayList<>();

                    while (backIndex > 0)  {
                        String backCommentLine = lines.get(backIndex);
                        if (backCommentLine.startsWith("# ")) {
                            comments.add(backCommentLine);
                        } else {
                            break;
                        }
                        backIndex--;
                    }

                    comments = comments.reversed();
                    comments.replaceAll((string) -> string.replace("# ", ""));

                    if (!commentLine.equals(String.join("\n", comments))) {
                        if (backIndex != -1 && i > backIndex) {
                            lines.subList(backIndex, i).clear();
                        }
                        lines.add(backIndex == -1 ? i : backIndex, commentLine);
                    }

                    break;
                }
            }

            Files.write(configFile.toPath(), lines);
        } catch (IOException e) {
            debug("Failed to load config file: " + configFile.getAbsolutePath());
        }
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            debug("Could not save config file: " + configFile.getAbsolutePath());
        }
    }

    public void reload() {
        debug("Reloading " + configFile.getName());
        try {
            config.load(configFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            debug("Failed to reload " + configFile.getName());
        }
    }

    public void dispose() {
        config = null;
        configFile = null;
    }
}