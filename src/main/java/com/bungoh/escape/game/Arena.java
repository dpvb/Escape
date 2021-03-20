package com.bungoh.escape.game;

import com.bungoh.escape.files.ConfigFile;
import com.bungoh.escape.files.DataFile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class Arena {

    private String name;
    private ArrayList<UUID> players;

    private Location lobbyLocation;
    private Location runnerSpawn;
    private Location killerSpawn;
    private Location escapeLocation;

    private GameState state;
    private Countdown countdown;
    private Game game;

    private boolean ready;

    public Arena(String name) {
        this.name = name;
        players = new ArrayList<>();
        ready = DataFile.checkArenaReady(name);
        if (ready) {
            setup();
        }
    }

    public void start() {
        game.start();
    }

    public void setup() {
        ready = true;
        lobbyLocation = DataFile.getArenaLobbySpawn(name);
        runnerSpawn = DataFile.getArenaRunnerSpawn(name);
        killerSpawn = DataFile.getArenaKillerSpawn(name);
        escapeLocation = DataFile.getArenaEscapeLocation(name);
        state = GameState.RECRUITING;
        countdown = new Countdown(this);
        game = new Game(this);
    }

    public void reset() {
        for (UUID uuid : players) {
            Bukkit.getPlayer(uuid).teleport(lobbyLocation);
        }

        state = GameState.RECRUITING;
        players.clear();
        countdown = new Countdown(this);
        game = new Game(this);
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        player.teleport(lobbyLocation);
        sendMessage(player.getName() + ChatColor.GREEN + " has joined.");

        if (players.size() >= ConfigFile.getRequiredPlayers()) {
            countdown.begin();
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        player.teleport(lobbyLocation);

        if (players.size() <= ConfigFile.getRequiredPlayers() && state == GameState.COUNTDOWN) {
            reset();
        }

        if (players.size() == 0 && state == GameState.LIVE) {
            reset();
        }
    }

    public void sendMessage(String message) {
        for (UUID uuid : players) {
            Bukkit.getPlayer(uuid).sendMessage(message);
        }
    }

    public ArrayList<UUID> getPlayers() {
        return players;
    }

    public String getName() {
        return name;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public GameState getState() {
        return state;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
