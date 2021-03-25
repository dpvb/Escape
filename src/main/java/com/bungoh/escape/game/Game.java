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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;

public class Game {

    private Arena arena;
    private Player killer;
    private Set<Player> runners;
    private Set<Generator> completedGenerators;
    private Particle.DustOptions doorParticleOptions;
    private BukkitTask particleTask;
    private BukkitTask killerHeartbeat;
    private boolean escapable;
    private boolean killerRevealCooldown;
    private HashMap<Player, Boolean> runnerInvisCooldown;
    private int runnerInvisCooldownLen;
    private int killerRevealCooldownLen;
    private Team team;
    private Sound heartbeatSound = Sound.BLOCK_NOTE_BLOCK_BASEDRUM;

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

        if (killerHeartbeat != null) {
            killerHeartbeat.cancel();
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

        //Create Killer Sword
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
        killer.getInventory().setHeldItemSlot(0);

        //Set Killer Reveal Cooldowns
        killerRevealCooldown = false;
        killerRevealCooldownLen = 20 * 20;

        //Remove the Killer's Nametag
        team.addEntry(killer.getName());

        //Teleport the killer
        killer.teleport(arena.getKillerSpawn());

        //Setup Runners
        runners = new HashSet<>();
        runnerInvisCooldown = new HashMap<>();
        for (int i = 0; i < numPlayers; i++) {
            if (i != rand) {
                //Add runner and teleport the runner to arena
                Player p = Bukkit.getPlayer(arena.getPlayers().get(i));
                runners.add(p);
                p.teleport(arena.getRunnerSpawn());

                //Create invis ability
                ItemStack inkSac = new ItemStack(Material.INK_SAC);
                ItemMeta sacMeta = inkSac.getItemMeta();
                sacMeta.setDisplayName(ChatColor.GRAY + "INVIS");
                List<String> sacLore = new ArrayList<>();
                lore.add(ChatColor.RED + "Right Click to get " + ChatColor.DARK_RED + "invisibility");
                sacMeta.setLore(sacLore);
                inkSac.setItemMeta(sacMeta);
                p.getInventory().setItem(0, inkSac);

                //Create invis cooldown
                runnerInvisCooldown.put(p, false);

                //Remove runner nametag
                team.addEntry(p.getName());
            }
        }

        //Set cooldown length for the invis abiliity
        runnerInvisCooldownLen = 30 * 20;

        //Setup Killer Heartbeat
        killerHeartbeat = new BukkitRunnable() {
            @Override
            public void run() {
                runners.forEach(p -> {
                    long d = (long) killer.getLocation().distance(p.getLocation());
                    if (d <= 20) {
                        long ticks = 8 - (6 * (20 - d) / 20);
                        p.playSound(p.getLocation(), heartbeatSound, SoundCategory.PLAYERS, 1, 1);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                p.playSound(p.getLocation(), heartbeatSound, SoundCategory.PLAYERS, 1, 1);
                            }
                        }.runTaskLaterAsynchronously(Escape.getPlugin(), ticks);
                    }
                });
            }
        }.runTaskTimer(Escape.getPlugin(), 0L, 20L);
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
            killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
            killer.removePotionEffect(PotionEffectType.BLINDNESS);

            for (Player r : getRunners()) {
                r.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1));
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

    private void prepareScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        team = scoreboard.registerNewTeam(arena.getName());
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        team.setCanSeeFriendlyInvisibles(false);
    }

    public void runnerInvis(Player player) {
        if (!runnerInvisCooldown.get(player)) {
            player.sendMessage(ChatColor.RED + "You went invis for 3 seconds!");
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 3));
            player.getInventory().setHeldItemSlot(1);

            runnerInvisCooldown.replace(player, true);
            new BukkitRunnable(){
                @Override
                public void run() {
                    if (runners.contains(player)) {
                        runnerInvisCooldown.replace(player, false);
                        player.sendMessage(ChatColor.GREEN + "Your invis ability is back up!");
                    }
                }
            }.runTaskLater(Escape.getPlugin(), runnerInvisCooldownLen);
        } else {
            player.sendMessage(ChatColor.RED + "Invis is on cooldown!");
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

    public Team getTeam() {
        return team;
    }
}
