package com.bungoh.escape.game;

import org.bukkit.entity.Player;

public class Spectator extends GameParticipant {

    public Spectator(Game game, Player player) {
        super(game, player);
    }

    @Override
    public void cleanup() {

    }

}
