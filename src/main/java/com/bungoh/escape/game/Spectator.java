package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Spectator extends GameParticipant {

    public Spectator(Game game, Player player) {
        super(game, player);
        init();
    }

    private void init() {
        player.setAllowFlight(true);
        player.setFlying(true);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(Escape.getPlugin(), player);
        }
    }

    @Override
    public void cleanup() {
        player.setAllowFlight(false);
        player.setFlying(false);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(Escape.getPlugin(), player);
        }
    }

}
