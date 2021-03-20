package com.bungoh.escape.files;

import com.bungoh.escape.Escape;

public class ConfigFile {

    private static Escape plugin;

    public ConfigFile(Escape plugin) {
        ConfigFile.plugin = plugin;

        plugin.getConfig().options().copyDefaults();
        plugin.saveDefaultConfig();
    }

    public static int getRequiredPlayers() {
        return plugin.getConfig().getInt("required-players");
    }

    public static int getCountdownSeconds() {
        return plugin.getConfig().getInt("countdown-timer");
    }

}
