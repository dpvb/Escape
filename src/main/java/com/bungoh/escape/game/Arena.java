package com.bungoh.escape.game;

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

    public void reset() {
        //Teleport Players
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            p.teleport(lobbyLocation);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.getInventory().clear();
            p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
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
        game.cleanup();
        game = new Game(this);

        System.out.println("Arena " + name + " successfully reset.");
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        player.teleport(lobbyLocation);
        sendMessage(player.getName() + ChatColor.GREEN + " has joined.");

        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();

        if (players.size() >= ConfigFile.getRequiredPlayers()) {
            countdown.begin();
        }
    }

    public void removePlayer(Player player, RemovalTypes remove) {
        players.remove(player.getUniqueId());
        player.teleport(lobbyLocation);

        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        player.getInventory().clear();
        player.setFoodLevel(20);

        switch (remove) {
            case COMMAND:
            case DISCONNECTED:
                player.setHealth(20);
                if (state == GameState.LIVE) {
                    if (players.size() == 0 || game.getKiller().equals(player)) {
                        sendMessage(ChatColor.GREEN + "The killer has disconnected. The game ended.");
                        reset();
                        return;
                    }

                    if (game.isRunner(player)) {
                        if (game.getRunners().size() == 1) {
                            //Killer Win
                            Bukkit.getServer().broadcastMessage(game.getKiller().getName() + ChatColor.GREEN + " won as the Killer in arena: " + ChatColor.WHITE + name);
                            reset();
                        } else {
                            game.getRunners().remove(player);
                        }
                    }
                }

                if (players.size() < ConfigFile.getRequiredPlayers() && state == GameState.COUNTDOWN) {
                    reset();
                }
                break;
            case KILLED:
                if (state == GameState.LIVE) {
                    if (game.isRunner(player)) {
                        if (game.getRunners().size() == 1) {
                            //Killer Win
                            Bukkit.getServer().broadcastMessage(game.getKiller().getName() + ChatColor.GREEN + " won as the Killer in arena: " + ChatColor.WHITE + name);
                            reset();
                        } else {
                            player.sendMessage(ChatColor.RED + "You were not able to escape!");
                            game.getRunners().remove(player);
                        }
                    }
                }
                break;
            case ESCAPED:
                player.setHealth(20);
                if (state == GameState.LIVE) {
                    if (players.size() == 0 || game.getKiller().equals(player)) {
                        reset();
                    }

                    if (game.isRunner(player)) {
                        if (game.getRunners().size() == 1) {
                            Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Everyone escaped in arena: " + ChatColor.WHITE + name);
                            reset();
                        } else {
                            player.sendMessage(ChatColor.GREEN + "You successfully escaped!");
                            game.getRunners().remove(player);
                        }
                    }
                }
                break;
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
