package com.bungoh.escape.game;

import org.bukkit.ChatColor;

import java.util.ArrayList;

public class Game {

    private Arena arena;

    public Game(Arena arena) {
        this.arena = arena;
    }

    public void start() {
        //Set Game State
        arena.setState(GameState.LIVE);

        //Setup Generators
        for (Generator g : arena.getGenerators()) {
            g.init();
        }

        //Send message
        arena.sendMessage(ChatColor.GREEN + "The game has started!");
    }

}
