package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Runner extends GameParticipant {

    protected RunnerAbility runnerAbility;
    protected BukkitTask killerHeartbeat;
    protected RunnerListener listener;
    private RunnerAbilityGUI selectGUI;

    public Runner(Game game, Player player) {
        super(game, player);
        init();
    }

    private void init() {
        //Register Listener
        listener = new RunnerListener();
        Bukkit.getPluginManager().registerEvents(listener, Escape.getPlugin());

        //Select Ability
        selectGUI = new RunnerAbilityGUI(this);

        //Setup Killer Heartbeat
        killerHeartbeat = new BukkitRunnable() {
            @Override
            public void run() {
                long d = (long) game.getKiller().getLocation().distance(getLocation());
                if (d <= 30) {
                    long ticks = 9 - (7 * (30 - d) / 30);
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
    }

    public void cleanup() {
        player.getInventory().clear();

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

        @EventHandler
        public void onAbilitySelect(InventoryClickEvent e) {
            if (!(e.getWhoClicked() instanceof Player)) {
                return;
            }

            Player p = (Player) e.getWhoClicked();

            if (!p.equals(player)) {
                return;
            }

            if (!e.getView().getTitle().equals(selectGUI.guiName)) {
                return;
            }

            if (e.getCurrentItem() == null) {
                return;
            }

            if (selectGUI.blindItem.equals(e.getCurrentItem())) {
                runnerAbility = new RunnerBlind(Runner.this);
                player.sendMessage(ChatColor.GREEN + "You selected the " + ChatColor.RED + runnerAbility.name + ChatColor.GREEN + " ability!");
                player.getInventory().setItem(0, runnerAbility.item);
                player.closeInventory();
            } else if (selectGUI.invisItem.equals(e.getCurrentItem())) {
                runnerAbility = new RunnerCloak(Runner.this);
                player.sendMessage(ChatColor.GREEN + "You selected the " + ChatColor.RED + runnerAbility.name + ChatColor.GREEN + " ability!");
                player.getInventory().setItem(0, runnerAbility.item);
                player.closeInventory();
            }
        }

        @EventHandler
        public void onAbilitySelectGUIClose(InventoryCloseEvent e) {
            if (!(e.getPlayer() instanceof Player)) {
                return;
            }

            Player p = (Player) e.getPlayer();

            if (!p.equals(player)) {
                return;
            }

            if (!e.getView().getTitle().equals(selectGUI.guiName)) {
                return;
            }

            if (runnerAbility == null) {
                runnerAbility = new RunnerCloak(Runner.this);
                player.sendMessage(ChatColor.GREEN + "You selected the " + ChatColor.RED + runnerAbility.name + ChatColor.GREEN + " ability!");
                player.getInventory().setItem(0, runnerAbility.item);
            }
        }

    }

}
