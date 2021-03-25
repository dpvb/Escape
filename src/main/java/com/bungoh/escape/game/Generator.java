package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.UUID;

public class Generator {

    private Block block;
    private Arena arena;
    private ArrayList<ArmorStand> holograms;
    private ArrayList<Vector> circlePoints;
    private int progress;
    private boolean finished;
    private Particle.DustOptions circleDust;
    private BukkitTask task;
    private int radius;

    public Generator(Block block, Arena arena) {
        this.block = block;
        this.arena = arena;
    }

    public void init() {
        progress = 0;
        finished = false;

        holograms = new ArrayList<>();
        setupHolograms();

        circleDust = new Particle.DustOptions(Color.fromRGB(0, 0, 0), 1);

        circlePoints = new ArrayList<>();
        Vector middlePoint = block.getLocation().toVector().add(new Vector(0.5, 0.25, 0.5));

        int numCircleParticles = 12;
        radius = 3;
        for (int i = 0; i < numCircleParticles; i++) {
            double x = middlePoint.getX() + radius * Math.cos(2 * Math.PI * i / numCircleParticles);
            double y = middlePoint.getY();
            double z = middlePoint.getZ() + radius * Math.sin(2 * Math.PI * i / numCircleParticles);
            circlePoints.add(new Vector(x, y, z));
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (progress == 100) {
                    finished = true;
                    arena.getGame().generatorCompleted(getGenerator());
                    cancel();
                    return;
                }

                try {
                    for (UUID u : arena.getPlayers()) {
                        Player p = Bukkit.getPlayer(u);
                        if (!arena.getGame().getKiller().equals(p)) {
                            Block target = p.getTargetBlockExact(radius);
                            if (target != null && target.equals(block)) {
                                progress += 4;
                                updateHoloProgress();
                                break;
                            }
                        }
                    }
                } catch (ConcurrentModificationException e) {
                    System.out.println("Concurrent Modification because players force quit. Generators still trying to update.");
                }


                circleDust = new Particle.DustOptions(Color.fromRGB(255 * progress / 100, 255 * progress / 100, 255 * progress / 100), 1);
                for (Vector v : circlePoints) {
                    arena.getWorld().spawnParticle(Particle.REDSTONE, v.toLocation(arena.getWorld()), 25, circleDust);
                }
            }
        }.runTaskTimerAsynchronously(Escape.getPlugin(), 0L, 20L);
    }

    public void setupHolograms() {
        holograms.add(produceArmorstand("&aGenerator", block.getLocation().add(0.5, .9, 0.5)));
        holograms.add(produceArmorstand("&c" + progress + "/100", block.getLocation().add(0.5, .6, 0.5)));
    }

    private ArmorStand produceArmorstand(String name, Location location) {
        ArmorStand stand = (ArmorStand) arena.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setSmall(true);
        stand.setCustomNameVisible(true);
        stand.setCustomName(ChatColor.translateAlternateColorCodes('&', name));
        return stand;
    }

    public void cleanUp() {
        deleteHolograms();
        if (task != null) {
            task.cancel();
        }
    }

    public void deleteHolograms() {
        if (holograms != null) {
            for (ArmorStand h : holograms) {
                h.remove();
            }
            holograms.clear();
        }
    }

    private void updateHoloProgress() {
        holograms.get(1).setCustomName(ChatColor.translateAlternateColorCodes('&', "&c" + progress + "/100"));
    }

    public Block getBlock() {
        return block;
    }

    public boolean isFinished() {
        return finished;
    }

    public Generator getGenerator() {
        return this;
    }

}
