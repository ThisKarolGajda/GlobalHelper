package com.github.thiskarolgajda.globalhelper.util;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

public class ClassFinder extends Helper {
    private static Set<Class<?>> cachedClasses = null;
    private final Plugin plugin;

    public ClassFinder(Plugin plugin) {
        this.plugin = plugin;
    }

    public Set<Class<?>> findAllClassesUsingClassLoader() {
        if (cachedClasses != null) {
            return cachedClasses;
        }

        String packageName = plugin.getClass().getPackage().getName().replace("/", ".");
        cachedClasses = new HashSet<>();
        findClassesRecursively(packageName, cachedClasses);
        return cachedClasses;
    }

    private void findClassesRecursively(@NotNull String packageName, Set<Class<?>> classes) {
        URL packageURL = plugin.getClass().getClassLoader().getResource(packageName.replace('.', '/'));

        if (packageURL == null) {
            debug("Package URL is null for package: " + packageName);
            return;
        }

        String protocol = packageURL.getProtocol();
        if ("file".equals(protocol)) {
            loadClassesFromDirectory(packageURL, packageName, classes);
        } else if ("jar".equals(protocol)) {
            loadClassesFromJar(packageURL, packageName, classes);
        } else {
            debug("Unsupported protocol: " + protocol);
        }
    }

    private void loadClassesFromDirectory(URL packageURL, String packageName, Set<Class<?>> classes) {
        try {
            File directory = new File(packageURL.toURI());
            if (!directory.exists()) {
                debug("Directory does not exist for package: " + packageName);
                return;
            }

            debug("Searching for classes in directory: " + directory.getAbsolutePath());
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        debug("Found directory: " + file.getName());
                        findClassesRecursively(packageName + "." + file.getName(), classes);
                    } else if (file.getName().endsWith(".class")) {
                        addClassToSet(packageName, file, classes);
                    }
                }
            } else {
                debug("No files found in directory: " + directory.getAbsolutePath());
            }
        } catch (URISyntaxException e) {
            debug("Error while accessing package: " + packageName);
        }
    }

    private void loadClassesFromJar(URL packageURL, String packageName, Set<Class<?>> classes) {
        String jarPath = packageURL.getPath().substring(5, packageURL.getPath().indexOf("!"));
        try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
            jarFile.stream()
                    .filter(entry -> entry.getName().startsWith(packageName.replace('.', '/') + "/") && entry.getName().endsWith(".class"))
                    .forEach(entry -> addClassToSet(entry.getName(), classes));
        } catch (IOException e) {
            debug("Failed to read JAR file: " + jarPath);
        }
    }

    private void addClassToSet(String packageName, File file, Set<Class<?>> classes) {
        String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
        try {
            classes.add(Class.forName(className));
            debug("Loaded class: " + className);
        } catch (ClassNotFoundException e) {
            debug("Class not found: " + className);
        }
    }

    private void addClassToSet(String entryName, Set<Class<?>> classes) {
        String className = entryName.replace("/", ".").substring(0, entryName.length() - 6);
        try {
            classes.add(Class.forName(className));
            debug("Loaded class from JAR: " + className);
        } catch (ClassNotFoundException e) {
            debug("Class not found: " + className);
        }
    }
}