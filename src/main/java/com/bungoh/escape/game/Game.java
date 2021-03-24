package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import com.bungoh.escape.files.ConfigFile;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game {

    private Arena arena;
    private Player killer;
    private Set<Player> runners;
    private Set<Generator> completedGenerators;
    private Particle.DustOptions doorParticleOptions;
    private BukkitTask particleTask;
    private boolean escapable;
    private boolean killerRevealCooldown;
    private int killerRevealCooldownLen;

    public Game(Arena arena) {
        this.arena = arena;
    }

    public void start() {
        //Set Game State
        arena.setState(GameState.LIVE);

        //Initialize all Players into Respective Roles
        setupKillerAndRunners();

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
        if (particleTask != null) {
            particleTask.cancel();
        }
    }

    public void generatorCompleted(Generator generator) {
        completedGenerators.add(generator);
        arena.sendMessage(ChatColor.GREEN + "A generator has been completed! (" + completedGenerators.size() + "/" + ConfigFile.getGeneratorWinRequirement() + ")");
        if (completedGenerators.size() == ConfigFile.getGeneratorWinRequirement()) {
            openEscapeDoor();
        }
    }

    private void setupKillerAndRunners() {
        //Setup Killers and Runners
        int numPlayers = arena.getPlayers().size();
        int rand = (int) (Math.random() * numPlayers);

        //Setup Killer
        killer = Bukkit.getPlayer(arena.getPlayers().get(rand));

        ItemStack killerSword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta swordMeta = killerSword.getItemMeta();
        swordMeta.setUnbreakable(true);
        swordMeta.setDisplayName(ChatColor.DARK_RED + "KILLER SWORD");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "Right Click to use " + ChatColor.DARK_RED + "Reveal");
        swordMeta.setLore(lore);
        killerSword.setItemMeta(swordMeta);
        killerSword.addEnchantment(Enchantment.KNOCKBACK, 2);
        killer.getInventory().setItem(0, killerSword);

        killer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0));
        killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));

        killerRevealCooldown = false;
        killerRevealCooldownLen = 10 * 20;

        //Setup Runners
        runners = new HashSet<>();
        for (int i = 0; i < numPlayers; i++) {
            if (i != rand) {
                Player p = Bukkit.getPlayer(arena.getPlayers().get(i));
                runners.add(p);
                p.teleport(arena.getRunnerSpawn());
            }
        }

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
        arena.sendMessage(ChatColor.GREEN + "The exit has been opened! Find a black door with white particles!");
        doorParticleOptions = new Particle.DustOptions(Color.WHITE, 1);
    }

    public void runnerEscaped(Player player) {
        arena.sendMessage(player.getName() + ChatColor.GREEN + " has escaped!");
        arena.removePlayer(player, RemovalTypes.ESCAPED);
    }

    public void runnerKilled(Player player) {
        arena.sendMessage(player.getName() + ChatColor.RED + " has been killed!");
        arena.removePlayer(player, RemovalTypes.KILLED);
    }

    public void revealRunners() {
        if (!killerRevealCooldown) {
            killer.sendMessage(ChatColor.RED + "Runners have been revealed!");

            for (Player r : getRunners()) {
                r.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 1));
            }

            killerRevealCooldown = true;
            new BukkitRunnable(){
                @Override
                public void run() {
                    killerRevealCooldown = false;
                }
            }.runTaskLater(Escape.getPlugin(), killerRevealCooldownLen);
        } else {
            killer.sendMessage(ChatColor.RED + "Reveal is on cooldown!");
        }
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
