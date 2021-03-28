package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import com.bungoh.escape.files.ConfigFile;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;

public class Game {

    private Arena arena;
    private Killer killer;
    private Set<Runner> runners;
    private Set<Spectator> spectators;
    private Set<Generator> completedGenerators;
    private Particle.DustOptions doorParticleOptions;
    private BukkitTask particleTask;
    private boolean escapable;
    private Team team;
    protected Sound heartbeatSound = Sound.BLOCK_NOTE_BLOCK_BASEDRUM;


    public Game(Arena arena) {
        this.arena = arena;
    }

    public void start() {
        //Set Game State
        arena.setState(GameState.LIVE);

        //Setup no nametags
        prepareScoreboard();

        //Initialize all Players into Respective Roles
        setupKillerAndRunners();

        //Create Empty Spectator Set
        spectators = new HashSet<>();

        //Setup Generators
        for (Generator g : arena.getGenerators()) {
            g.init();
        }
        completedGenerators = new HashSet<>();

        //Create Escape Door
        createEscapeDoor();

        //Send Message
        arena.sendMessage(ChatColor.GREEN + "The game has started!");
        arena.sendMessage(killer.getName() + ChatColor.GREEN + " is the " + ChatColor.RED + "killer!");
    }

    public void cleanup() {
        if (team != null) {
            team.getEntries().forEach(s -> team.removeEntry(s));
            team.unregister();
        }

        if (particleTask != null) {
            particleTask.cancel();
        }
    }

    public void generatorCompleted(Generator generator) {
        completedGenerators.add(generator);
        arena.sendMessage(ChatColor.GREEN + "A generator has been completed! (" + completedGenerators.size() + "/" + ConfigFile.getGeneratorWinRequirement() + ")");
        for (UUID u : arena.getPlayers()) {
            Player p = Bukkit.getPlayer(u);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }
        if (completedGenerators.size() == ConfigFile.getGeneratorWinRequirement()) {
            openEscapeDoor();
            for (UUID u : arena.getPlayers()) {
                Player p = Bukkit.getPlayer(u);
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 1);
            }
        }
    }

    private void setupKillerAndRunners() {
        //Setup Killers and Runners
        int numPlayers = arena.getPlayers().size();
        int rand = (int) (Math.random() * numPlayers);

        //Setup Killer
        killer = new Killer(this, Bukkit.getPlayer(arena.getPlayers().get(rand)));
        team.addEntry(killer.player.getName());
        killer.teleport(arena.getKillerSpawn());

        //Setup Runners
        runners = new HashSet<>();
        for (int i = 0; i < numPlayers; i++) {
            if (i != rand) {
                Runner r = new Runner(this, Bukkit.getPlayer(arena.getPlayers().get(i)));
                runners.add(r);
                team.addEntry(r.player.getName());
                r.teleport(arena.getRunnerSpawn());
            }
        }
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
        arena.sendMessage(ChatColor.GREEN + "The exit has been opened! Find a black door with white particles!");
        doorParticleOptions = new Particle.DustOptions(Color.WHITE, 1);
    }

    public void runnerEscaped(Player player) {
        arena.sendMessage(player.getName() + ChatColor.GREEN + " has escaped!");
        //spectate(player);
        arena.removePlayer(player, RemovalTypes.ESCAPED);
    }

    public void runnerKilled(Player player) {
        arena.sendMessage(player.getName() + ChatColor.RED + " has been killed!");
        //spectate(player);
        arena.removePlayer(player, RemovalTypes.KILLED);
    }

    private void prepareScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        team = scoreboard.registerNewTeam(arena.getName());
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        team.setCanSeeFriendlyInvisibles(false);
    }

    /*
    public void spectate(Player player) {
        //Add to Spectator Set
        spectators.add(player);
        //Let Player Fly
        player.setFlying(true);
        //Hide Player from Everyone Else in the Arena
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(Escape.getPlugin(), player);
        }
        //Teleport Player to the Killer
        player.teleport(killer.getLocation());
    }

    public void resetSpectator(Player player) {
        if (spectators.contains(player)) {
            player.setFlying(false);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(Escape.getPlugin(), player);
            }
            spectators.remove(player);
        }
    }

     */

    public Killer getKiller() {
        return killer;
    }

    public boolean isRunner(Player player) {
        if (runners == null) {
            return false;
        }

        for (Runner r : runners) {
            Player p = r.player;
            if (p.equals(player)) {
                return true;
            }
        }
        return false;
    }

    public void removeRunner(Player player) {
        if (runners != null) {
            runners.removeIf(r -> r.player.equals(player));
        }
    }

    public Runner getRunner(Player player) {
        return runners.stream().filter(r -> r.player.equals(player)).findAny().orElse(null);
    }

    public boolean isEscapable() {
        return escapable;
    }

    public Set<Runner> getRunners() {
        return runners;
    }

    public Team getTeam() {
        return team;
    }

}
