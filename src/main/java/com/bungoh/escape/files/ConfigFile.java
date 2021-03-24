package com.bungoh.escape.files;

import com.bungoh.escape.Escape;
import org.bukkit.ChatColor;

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

    public static String getGeneratorBlock() { return plugin.getConfig().getString("generator-block"); }

    public static int getGeneratorsRequired() { return plugin.getConfig().getInt("generator-amount"); }

    public static int getGeneratorWinRequirement() { return plugin.getConfig().getInt("generator-win-requirement"); }

    public static String getMessage(String path) {
        String msg = plugin.getConfig().getString(path);
        if (msg != null) {
            return ChatColor.translateAlternateColorCodes('&', msg);
        }
        return "";
    }

}
