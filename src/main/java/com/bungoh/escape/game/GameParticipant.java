package com.bungoh.escape.game;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class GameParticipant {

    protected Player player;
    protected Game game;

    public GameParticipant(Game game, Player player) {
        this.player = player;
        this.game = game;
    }

    public void teleport(GameParticipant participant) {
        player.teleport(participant.player);
    }

    public void teleport(Location location) {
        player.teleport(location);
    }

    public Location getLocation() {
        return player.getLocation();
    }

    public String getName() {
        return player.getName();
    }

    public abstract void cleanup();
}
