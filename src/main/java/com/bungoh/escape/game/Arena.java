package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import com.bungoh.escape.files.ConfigFile;
import com.bungoh.escape.files.DataFile;
import com.bungoh.escape.utils.InsufficientGeneratorAmount;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class Arena {

    private String name;
    private ArrayList<UUID> players;
    private Location lobbyLocation;
    private Location runnerSpawn;
    private Location killerSpawn;
    private Location escapeLocation;
    private Location corner1;
    private Location corner2;
    private World world;
    private List<Generator> generators;

    private GameState state;
    private Countdown countdown;
    private Game game;

    private boolean ready;

    public Arena(String name) {
        this.name = name;
        players = new ArrayList<>();
        ready = DataFile.checkArenaReady(name);
        world = DataFile.getWorld(name);
        if (ready) {
            try {
                setup();
            } catch (InsufficientGeneratorAmount e) {
                ready = false;
            }
        }
    }

    public void start() {
        game.start();
    }

    public void setup() throws InsufficientGeneratorAmount {
        // Set all fixed locations.
        lobbyLocation = DataFile.getArenaLobbySpawn(name);
        runnerSpawn = DataFile.getArenaRunnerSpawn(name);
        killerSpawn = DataFile.getArenaKillerSpawn(name);
        escapeLocation = DataFile.getArenaEscapeLocation(name);
        corner1 = DataFile.getArenaCornerOne(name);
        corner2 = DataFile.getArenaCornerTwo(name);
        
        //Attempt to locate all generators in area
        initGenerators();

        // Set Game State
        state = GameState.RECRUITING;
        // Instantiate Countdown and Game
        countdown = new Countdown(this);
        game = new Game(this);
        // Arena is now completely ready
        ready = true;
        System.out.println("Arena " + name + " successfully setup.");
    }

    //Completely reset the Arena
    public void reset() {
        //Reset Players and Clean them Up
        for (int i = 0; i < players.size(); i++) {
            Player player = Bukkit.getPlayer(players.get(i));
            resetPlayer(player);
            player.teleport(lobbyLocation);
        }

        //Reset Generators
        if (generators != null) {
            for (Generator g : generators) {
                g.cleanUp();
            }
        }

        //Reset Arena State
        state = GameState.RECRUITING;
        players.clear();
        countdown = new Countdown(this);
        if (game != null) {
            game.cleanup();
        }
        game = new Game(this);

        System.out.println("Arena " + name + " successfully reset.");
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        player.teleport(lobbyLocation);
        sendMessage(ConfigFile.getPrefix() + " " + player.getName() + ChatColor.GREEN + " has joined.");

        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);

        if (players.size() >= ConfigFile.getRequiredPlayers()) {
            countdown.begin();
        }
    }

    //This method should be used to completely remove the player from the Arena.
    public void removePlayer(Player player) {
        resetPlayer(player);
        player.teleport(getLobbyLocation());
        players.remove(player.getUniqueId());

        if (state == GameState.LIVE) {
            game.removePlayer(player);
            game.checkGameConditions();
        }

        if (state == GameState.COUNTDOWN && players.size() < ConfigFile.getRequiredPlayers()) {
            reset();
        }
    }

    public void resetPlayer(Player player) {
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setLevel(0);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(Escape.getPlugin(), player);
        }

        if (state == GameState.LIVE && game.getTeam() != null) {
            game.getTeam().removeEntry(player.getName());
        }
    }

    public void sendMessage(String message) {
        for (UUID uuid : players) {
            Bukkit.getPlayer(uuid).sendMessage(message);
        }
    }

    private void initGenerators() throws InsufficientGeneratorAmount {
        generators = new ArrayList<>();
        int xMin = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int xMax = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int yMin = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int yMax = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int zMin = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int zMax = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType() == Manager.getGeneratorMaterial()) {
                        generators.add(new Generator(b, this));
                    }
                }
            }
        }

        // Check if the amount of generators is correct
        if (generators.size() != ConfigFile.getGeneratorsRequired()) {
            throw new InsufficientGeneratorAmount("Insufficient amount of Generators in the Arena");
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

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public Location getRunnerSpawn() {
        return runnerSpawn;
    }

    public Location getKillerSpawn() {
        return killerSpawn;
    }

    public Location getEscapeLocation() {
        return escapeLocation;
    }

    public Location getCorner1() {
        return corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public Game getGame() {
        return game;
    }

    public World getWorld() {
        return world;
    }

    public List<Generator> getGenerators() {
        return generators;
    }
}
