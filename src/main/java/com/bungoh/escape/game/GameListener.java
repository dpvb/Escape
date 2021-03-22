package com.bungoh.escape.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        if (Manager.isPlaying(player)) {
            Manager.getArena(player).removePlayer(player);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (Manager.isPlaying(player)) {
            Arena arena = Manager.getArena(player);
            if (arena.getState() == GameState.LIVE) {
                Game game = arena.getGame();
                if (game.isRunner(player)) {
                    if (game.isEscapable() && player.getLocation().getBlock().equals(arena.getEscapeLocation().getBlock())) {
                        game.runnerEscaped(player);
                    }
                }
            }
        }
    }
}
