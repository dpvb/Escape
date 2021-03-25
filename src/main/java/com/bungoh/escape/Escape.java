package com.bungoh.escape;

import com.bungoh.escape.commands.AdminCommandManager;
import com.bungoh.escape.commands.BaseCommandManager;
import com.bungoh.escape.files.ConfigFile;
import com.bungoh.escape.files.DataFile;
import com.bungoh.escape.game.GameListener;
import com.bungoh.escape.game.Manager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Escape extends JavaPlugin {

    private static Escape plugin;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Bungoh - Escape Sounds").build());

    @Override
    public void onEnable() {
        //Init Global Plugin Reference
        Escape.plugin = this;

        //Create File Managers
        new ConfigFile(this);
        new DataFile(this);

        //Create Manager
        new Manager();

        //Register Commands
        getCommand("escape").setExecutor(new BaseCommandManager());
        getCommand("escapeadmin").setExecutor(new AdminCommandManager());

        //Register Events
        Bukkit.getPluginManager().registerEvents(new GameListener(), this);
    }

    @Override
    public void onDisable() {
        //Reset all Arenas
        Manager.resetAllArenas();
    }

    public static Escape getPlugin() {
        return plugin;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }
}
