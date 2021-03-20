package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import com.bungoh.escape.files.ConfigFile;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class Countdown extends BukkitRunnable {

    private Arena arena;
    private int seconds;

    public Countdown(Arena arena) {
        this.arena = arena;
        this.seconds = ConfigFile.getCountdownSeconds();
    }

    public void begin() {
        arena.setState(GameState.COUNTDOWN);
        this.runTaskTimer(Escape.getPlugin(), 0L, 20L);
    }

    @Override
    public void run() {
        if (seconds == 0) {
            cancel();
            arena.start();
            return;
        }

        if (seconds <= 10 || seconds % 5 == 0) {
            arena.sendMessage(ChatColor.AQUA + "Game will start in " + seconds + " seconds.");
        }

        if (arena.getPlayers().size() < ConfigFile.getRequiredPlayers()) {
            cancel();
            arena.setState(GameState.RECRUITING);
            arena.sendMessage(ChatColor.RED + "There are too few players. Countdown stopped.");
            return;
        }

        seconds--;
    }
}
