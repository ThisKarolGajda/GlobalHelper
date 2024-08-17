package com.github.thiskarolgajda.globalhelper.injection.file;

import com.github.thiskarolgajda.globalhelper.util.Helper;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class FileManager extends Helper {
    private final Plugin plugin;
    private final File configFile;
    private final Map<String, String> values; // Cache for key-value pairs
    private final Map<String, List<String>> comments; // Cache for comments associated with keys
    private final List<String> lines; // Cached lines of the configuration file

    public FileManager(@NotNull Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), fileName);
        this.values = new LinkedHashMap<>(); // Maintain insertion order
        this.comments = new LinkedHashMap<>(); // Maintain insertion order for comments
        this.lines = new ArrayList<>();
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
                saveConfig(); // Save initial empty config
            } catch (IOException e) {
                debug("Could not create config file: " + configFile.getAbsolutePath());
            }
        }
    }

    private void loadConfig() {
        try {
            lines.clear(); // Clear existing lines
            lines.addAll(Files.readAllLines(configFile.toPath()));
            values.clear(); // Clear the cache for values
            comments.clear(); // Clear the cache for comments

            String currentKey = null;

            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) {
                    continue; // Skip empty lines
                }
                if (trimmedLine.startsWith("#")) {
                    // It's a comment; store it in the comments map
                    if (currentKey != null) {
                        comments.computeIfAbsent(currentKey, k -> new ArrayList<>()).add(trimmedLine);
                    }
                    continue; // Continue to the next line
                }
                if (trimmedLine.contains(":")) {
                    // It's a key-value pair
                    String[] parts = trimmedLine.split(":", 2);
                    currentKey = parts[0].trim(); // Update the current key
                    String value = parts[1].trim();
                    values.put(currentKey, value); // Store the value in the cache
                }
            }
        } catch (IOException e) {
            debug("Failed to load config file: " + configFile.getAbsolutePath());
        }
    }

    public <T> @NotNull T getValue(String key, T defaultType) {
        String value = values.get(key);
        if (value == null) {
            return defaultType;
        }

        Class<T> type = (Class<T>) defaultType.getClass();
        try {
            if (type == String.class) {
                return (T) value;
            } else if (type == Integer.class || type == int.class) {
                return (T) Integer.valueOf(value);
            } else if (type == Double.class || type == double.class) {
                return (T) Double.valueOf(value);
            } else if (type == Boolean.class || type == boolean.class) {
                return (T) Boolean.valueOf(value);
            }
        } catch (Exception ignored) {
        }

        return defaultType;
    }

    public <T> void setValue(String key, T value) {
        values.put(key, value.toString()); // Update the cache directly
        saveConfig(); // Save the updated configuration to the file
    }

    public void addComment(String key, @NotNull String comment) {
        String commentLine = "# " + comment.replace("\n", "\n# ");

        // Remove existing comments for the key
        comments.remove(key);

        // Add the new comment
        comments.computeIfAbsent(key, k -> new ArrayList<>()).add(commentLine);

        // Save the config to include the new comment
        saveConfig();
    }

    private void saveConfig() {
        try {
            List<String> outputLines = new ArrayList<>(); // Start with existing lines

            // Add key-value pairs from the cache, avoiding duplicates
            for (Map.Entry<String, String> entry : values.entrySet()) {
                String key = entry.getKey();
                String value = (entry.getValue() != null) ? entry.getValue() : "null"; // Handle null values
                String line = key + ": " + value;

                // Remove any existing line for the key to avoid duplication
                outputLines.removeIf(l -> l.startsWith(key + ":"));
                outputLines.add(line); // Add the new key-value pair

                // Add comments associated with the key
                if (comments.containsKey(key)) {
                    for (String comment : comments.get(key)) {
                        outputLines.add(outputLines.indexOf(line), comment); // Insert comment above the key-value line
                    }
                }
            }

            // Write the updated lines to the config file
            Files.write(configFile.toPath(), outputLines);
        } catch (IOException e) {
            debug("Could not save config file: " + configFile.getAbsolutePath());
        }
    }

    public void reload() {
        debug("Reloading " + configFile.getName());
        loadConfig(); // Reload the configuration and cache
    }

    public void dispose() {
        values.clear();
        lines.clear();
        comments.clear();
    }
}