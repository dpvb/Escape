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
    private Set<Player> spectators;
    private Set<Generator> completedGenerators;
    private Particle.DustOptions doorParticleOptions;
    private BukkitTask particleTask;
    private BukkitTask killerHeartbeat;
    private BukkitTask revealCooldown;
    private HashMap<Player, BukkitTask> invisTasks;
    private HashMap<Player, Boolean> runnerInvisCooldown;
    private boolean escapable;
    private boolean killerRevealCooldown;
    private boolean hitCooldown;
    private int runnerInvisCooldownLen;
    private int killerRevealCooldownLen;
    private int hitCooldownLen;
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

        if (killerHeartbeat != null) {
            killerHeartbeat.cancel();
        }

        if (revealCooldown != null) {
            revealCooldown.cancel();
        }

        if (invisTasks != null) {
            for (Player p: invisTasks.keySet()) {
                invisTasks.get(p).cancel();
            }
        }

        for (UUID uuid : arena.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            //resetSpectator(p);
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

        //Setup Sword Cooldown
        hitCooldown = false;
        hitCooldownLen = 2 * 20;

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
        runnerInvisCooldownLen = 35 * 20;

        //Initialize Tasks List
        invisTasks = new HashMap<>();

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
        //spectate(player);
        arena.removePlayer(player, RemovalTypes.ESCAPED);
    }

    public void runnerKilled(Player player) {
        arena.sendMessage(player.getName() + ChatColor.RED + " has been killed!");
        //spectate(player);
        arena.removePlayer(player, RemovalTypes.KILLED);
    }

    public void revealRunners() {
        if (!killerRevealCooldown) {
            killer.sendMessage(ChatColor.RED + "Runners have been revealed!");
            killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));

            for (Player r : getRunners()) {
                r.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1));
            }

            killerRevealCooldown = true;

            revealCooldown = new BukkitRunnable(){
                int revealCount = killerRevealCooldownLen / 20;
                @Override
                public void run() {
                    if (revealCount == 0) {
                        killerRevealCooldown = false;
                        cancel();
                    } else {
                        revealCount--;
                    }

                    killer.setLevel(revealCount + 1);
                }
            }.runTaskTimerAsynchronously(Escape.getPlugin(), 0L, 20L);
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
            //HIDE SPRINTING PARTICLES FROM PLAYER
            //killer.hidePlayer(Escape.getPlugin(), player);
            //new BukkitRunnable() {
            //    @Override
            //    public void run() {
            //        killer.showPlayer(Escape.getPlugin(), player);
            //    }
            //}.runTaskLater(Escape.getPlugin(), 60);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 3));
            player.getInventory().setHeldItemSlot(1);

            runnerInvisCooldown.replace(player, true);

            invisTasks.put(player, (new BukkitRunnable(){
                int invisCounter = runnerInvisCooldownLen / 20;
                @Override
                public void run() {
                    if (invisCounter == 0 && runners.contains(player)) {
                        runnerInvisCooldown.replace(player, false);
                        player.sendMessage(ChatColor.GREEN + "Your invis ability is back up!");
                        cancel();
                        invisTasks.remove(player);
                    } else {
                        invisCounter--;
                    }

                    player.setLevel(invisCounter + 1);
                }
            }.runTaskTimerAsynchronously(Escape.getPlugin(), 0L, 20L)));

        } else {
            player.sendMessage(ChatColor.RED + "Invis is on cooldown!");
        }
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

    public void cancelInvisTask(Player player) {
        if (getInvisTasks().containsKey(player)) {
            getInvisTasks().get(player).cancel();
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

    public boolean getHitCooldown() { return hitCooldown; }

    public int getHitCooldownLen() {
        return hitCooldownLen;
    }

    public void setHitCooldown(boolean hitCooldown) {
        this.hitCooldown = hitCooldown;
    }

    public HashMap<Player, BukkitTask> getInvisTasks() {
        return invisTasks;
    }

}
