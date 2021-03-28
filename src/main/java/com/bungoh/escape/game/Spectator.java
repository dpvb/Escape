package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class Spectator extends GameParticipant {

    private SpectatorListener listener;

    public Spectator(Game game, Player player) {
        super(game, player);
        init();
    }

    private void init() {
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setLevel(0);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(Escape.getPlugin(), player);
        }

        listener = new SpectatorListener();
        Bukkit.getPluginManager().registerEvents(listener, Escape.getPlugin());
    }

    @Override
    public void cleanup() {
        player.setAllowFlight(false);
        player.setFlying(false);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(Escape.getPlugin(), player);
        }

        HandlerList.unregisterAll(listener);
    }

    class SpectatorListener implements Listener {

        @EventHandler
        public void spectatorRespawn(PlayerRespawnEvent e) {
            e.setRespawnLocation(game.getKiller().getLocation());
        }

        @EventHandler
        public void spectatorInteract(PlayerInteractEvent e) {
            e.setCancelled(true);
        }

    }

}
