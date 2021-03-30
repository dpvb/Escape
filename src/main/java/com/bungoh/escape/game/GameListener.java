package com.bungoh.escape.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public class GameListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        if (Manager.isPlaying(player)) {
            Manager.getArena(player).removePlayer(player);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (Manager.isPlaying(player)) {
            Arena arena = Manager.getArena(player);
            if (arena.getState() == GameState.LIVE) {
                Game game = arena.getGame();
                if (game.isRunner(player)) {
                    if (game.isEscapable() && player.getLocation().getBlock().equals(arena.getEscapeLocation().getBlock())) {
                        game.runnerEscaped(player);
                    }
                }
            }
        }
    }

    //Disable Extraneous Damage
    @EventHandler
    public void playerOtherDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }

        Player player = (Player) e.getEntity();
        if (Manager.isPlaying(player)) {
            e.setCancelled(true);
        }
    }

    //Disable PVP
    @EventHandler
    public void playerHitPlayer(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player v = (Player) e.getEntity();
            Player d = (Player) e.getDamager();

            if (!Manager.isPlaying(v) || !Manager.isPlaying(d)) {
                return;
            }

            if (!Manager.getArena(v).equals(Manager.getArena(d))) {
                return;
            }

            if (Manager.getArena(v).getState() != GameState.LIVE) {
                e.setCancelled(true);
            }
        }
    }


    //No Hunger Depletion while in an Arena
    @EventHandler
    public void noHungerInGame(FoodLevelChangeEvent e) {
        Player player = (Player) e.getEntity();

        if (Manager.isPlaying(player)) {
            e.setCancelled(true);
        }
    }

    //Disable Regular Regen
    @EventHandler
    public void noRunnerRegen(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getEntity();

        if (!Manager.isPlaying(p)) {
            return;
        }

        if (Manager.getArena(p).getState() != GameState.LIVE) {
            return;
        }

        if (!Manager.getArena(p).getGame().isRunner(p)) {
            return;
        }

        if (e.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED || e.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
            e.setCancelled(true);
        }
    }

    //Killer Swap Inventory Spot
    @EventHandler
    public void killerNoItemSwap(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        if (Manager.isPlaying(player)) {
            if (Manager.getArena(player).getState() == GameState.LIVE && Manager.getArena(player).getGame().getKiller().player.equals(player)) {
                e.getPlayer().getInventory().setHeldItemSlot(0);
            }
        }
    }

    //Killer Swap to Offhand
    @EventHandler
    public void killerNoOffhand(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        if (Manager.isPlaying(player)) {
            if (Manager.getArena(player).getState() == GameState.LIVE && Manager.getArena(player).getGame().getKiller().player.equals(player)) {
                e.setCancelled(true);
            }
        }
    }

    //Killer Cant Move Items in Inventory
    @EventHandler
    public void killerNoMoveInventory(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        if (Manager.isPlaying(player)) {
            if (Manager.getArena(player).getState() == GameState.LIVE) {
                e.setCancelled(true);
            }
        }
    }

    //Runner Killed Event
    @EventHandler
    public void runnerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        if (Manager.isPlaying(p) && Manager.getArena(p).getState() == GameState.LIVE) {
            if (Manager.getArena(p).getGame().isRunner(p)) {
                Manager.getArena(p).getGame().runnerKilled(p);
                e.getDrops().clear();
                e.setDeathMessage("");
            }
        }
    }

    //No Item Drop in Game
    @EventHandler
    public void noItemDrop(PlayerDropItemEvent e) {
        if (Manager.isPlaying(e.getPlayer())) {
            e.setCancelled(true);
        }
    }
}
