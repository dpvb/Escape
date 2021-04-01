package com.bungoh.escape.files;

import com.bungoh.escape.Escape;
import com.bungoh.escape.utils.Messages;
import net.bytebuddy.build.ToStringPlugin;
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

    public static int getKillerRevealCooldown() { return plugin.getConfig().getInt("killer-reveal-cooldown"); }

    public static int getRunnerInvisCooldown() { return plugin.getConfig().getInt("runner-invis-cooldown"); }

    public static int getRunnerBlindCooldown() { return plugin.getConfig().getInt("runner-blind-cooldown"); }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(Messages.PREFIX.getPath()) + "&r");
    }

    public static String getMessage(String path) {
        String msg = plugin.getConfig().getString(path);
        if (msg != null) {
            return ChatColor.translateAlternateColorCodes('&', getPrefix() + " " + msg);
        }
        return "";
    }

}
