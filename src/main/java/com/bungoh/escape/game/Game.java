package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Game {

    private Arena arena;
    private Player killer;
    private Set<Player> runners;
    private Set<Generator> completedGenerators;
    private Particle.DustOptions doorParticleOptions;
    private BukkitTask particleTask;
    private boolean escapable;

    public Game(Arena arena) {
        this.arena = arena;
    }

    public void start() {
        //Set Game State
        arena.setState(GameState.LIVE);

        setupKillerAndRunners();

        //Setup Generators
        for (Generator g : arena.getGenerators()) {
            g.init();
        }
        completedGenerators = new HashSet<>();

        createEscapeDoor();

        //Send message
        arena.sendMessage(ChatColor.GREEN + "The game has started!");
        arena.sendMessage(killer.getName() + ChatColor.GREEN + " is the " + ChatColor.RED + "killer!");
    }

    public void cleanup() {
        if (particleTask != null) {
            particleTask.cancel();
        }
    }

    public void generatorCompleted(Generator generator) {
        completedGenerators.add(generator);
        if (completedGenerators.size() == arena.getGenerators().size()) {
            openEscapeDoor();
        }
    }

    private void setupKillerAndRunners() {
        //Setup Killers and Runners
        int numPlayers = arena.getPlayers().size();
        int rand = (int) (Math.random() * numPlayers);
        killer = Bukkit.getPlayer(arena.getPlayers().get(rand));
        runners = new HashSet<>();

        for (int i = 0; i < numPlayers; i++) {
            if (i != rand) {
                Player p = Bukkit.getPlayer(arena.getPlayers().get(i));
                runners.add(p);
                p.teleport(arena.getRunnerSpawn());
            }
        }
        /*
        APPLY STATUS EFFECTS / ETC
         */

        killer.teleport(arena.getKillerSpawn());
    }

    private void createEscapeDoor() {
        //Create Escape Location Particles
        doorParticleOptions = new Particle.DustOptions(Color.BLACK, 1);
        Block escapeBlock = arena.getEscapeLocation().getBlock();
        Vector doorLoc1 = new Vector(
                escapeBlock.getX(),
                escapeBlock.getY(),
                escapeBlock.getZ())
                .add(new Vector(0.5, 0.5, 0.5));
        Vector doorLoc2 = (new Vector(0, 1, 0)).add(doorLoc1);

        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                arena.getWorld().spawnParticle(Particle.REDSTONE, doorLoc1.toLocation(arena.getWorld()), 25, doorParticleOptions);
                arena.getWorld().spawnParticle(Particle.REDSTONE, doorLoc2.toLocation(arena.getWorld()), 25, doorParticleOptions);
            }
        }.runTaskTimerAsynchronously(Escape.getPlugin(), 0L, 20L);

        escapable = false;
    }

    private void openEscapeDoor() {
        escapable = true;
        doorParticleOptions = new Particle.DustOptions(Color.WHITE, 1);
    }

    public void runnerEscaped(Player player) {
        arena.sendMessage(player.getName() + ChatColor.GREEN + " has escaped!");
        arena.removePlayer(player);
    }

    public Player getKiller() {
        return killer;
    }

    public boolean isRunner(Player player) {
        return runners.contains(player);
    }

    public boolean isEscapable() {
        return escapable;
    }

    public Set<Player> getRunners() {
        return runners;
    }
}
