package com.bungoh.escape.game;

import org.bukkit.ChatColor;

public class Game {

    private Arena arena;

    public Game(Arena arena) {
        this.arena = arena;
    }

    public void start() {
        //Set Game State
        arena.setState(GameState.LIVE);

        //Send message
        arena.sendMessage(ChatColor.GREEN + "The game has started!");
    }

}
