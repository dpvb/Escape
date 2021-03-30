package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Spectator extends GameParticipant {

    private SpectatorListener listener;
    private ItemStack leaveItem;
    private ItemStack teleportItem;

    public Spectator(Game game, Player player) {
        super(game, player);
        init();
    }

    private void init() {
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setLevel(0);
        setupTeleportItem();
        setupLeaveItem();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(Escape.getPlugin(), player);
        }

        listener = new SpectatorListener();
        Bukkit.getPluginManager().registerEvents(listener, Escape.getPlugin());
    }

    private void setupTeleportItem() {
        teleportItem = new ItemStack(Material.COMPASS);
        ItemMeta meta = teleportItem.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "TELEPORT TO KILLER");
        teleportItem.setItemMeta(meta);

        player.getInventory().setItem(0, teleportItem);
    }

    private void setupLeaveItem() {
        leaveItem = new ItemStack(Material.RED_BED);
        ItemMeta meta = leaveItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "LEAVE GAME");
        leaveItem.setItemMeta(meta);;

        player.getInventory().setItem(8, leaveItem);
    }

    @Override
    public void cleanup() {
        player.setAllowFlight(false);
        player.setFlying(false);
        player.getInventory().clear();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(Escape.getPlugin(), player);
        }

        HandlerList.unregisterAll(listener);
    }

    class SpectatorListener implements Listener {

        @EventHandler
        public void spectatorRespawn(PlayerRespawnEvent e) {
            e.setRespawnLocation(game.getKiller().getLocation());
        }

        @EventHandler
        public void spectatorInteract(PlayerInteractEvent e) {
            if (e.getPlayer().equals(player)) {
                e.setCancelled(true);
            }
        }

        @EventHandler
        public void playerInteractEvent(PlayerInteractEvent e) {
            if (!e.getPlayer().equals(player)) {
                return;
            }

            if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getHand() == EquipmentSlot.HAND) {
                if (teleportItem.equals(e.getItem())) {
                    player.teleport(game.getKiller().getLocation());
                }

                if (leaveItem.equals(e.getItem())) {
                    Manager.getArena(player).removePlayer(player);
                }
            }
        }

    }

}
