package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Runner extends GameParticipant {

    protected RunnerAbility runnerAbility;
    protected BukkitTask killerHeartbeat;
    protected RunnerListener listener;

    public Runner(Game game, Player player) {
        super(game, player);
        init();
    }

    private void init() {
        //Select Runner Invis Ability
        runnerAbility = new RunnerInvis(this);
        player.getInventory().setItem(0, runnerAbility.item);

        //Setup Killer Heartbeat
        killerHeartbeat = new BukkitRunnable() {
            @Override
            public void run() {
                long d = (long) game.getKiller().getLocation().distance(getLocation());
                if (d <= 20) {
                    long ticks = 8 - (6 * (20 - d) / 20);
                    player.playSound(player.getLocation(), game.heartbeatSound, SoundCategory.PLAYERS, 1, 1);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.playSound(getLocation(), game.heartbeatSound, SoundCategory.PLAYERS, 1, 1);
                        }
                    }.runTaskLaterAsynchronously(Escape.getPlugin(), ticks);
                }
            }
        }.runTaskTimer(Escape.getPlugin(), 0L, 20L);

        //Register Event
        listener = new RunnerListener();
        Bukkit.getPluginManager().registerEvents(listener, Escape.getPlugin());
    }

    public void cleanup() {
        if (killerHeartbeat != null) {
            killerHeartbeat.cancel();
        }

        if (runnerAbility != null) {
            runnerAbility.cleanup();
        }

        HandlerList.unregisterAll(listener);
    }

    class RunnerListener implements Listener {

        @EventHandler
        public void onInteract(PlayerInteractEvent e) {
            if (!e.getPlayer().equals(player)) {
                return;
            }

            if (e.getItem() == null) {
                return;
            }

            if (e.getItem().equals(runnerAbility.item) && e.getHand() == EquipmentSlot.HAND && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                runnerAbility.use();
            }
        }

    }

}
