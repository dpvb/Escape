package com.bungoh.escape.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GameListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        if (Manager.isPlaying(player)) {
            Manager.getArena(player).removePlayer(player, RemovalTypes.DISCONNECTED);
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

    //Killer Damage Onto Runners
    @EventHandler
    public void playerHitEvent(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player victim = (Player) e.getEntity();
            Player damager = (Player) e.getDamager();

            //Are both of them in game
            if (!Manager.isPlaying(victim) || !Manager.isPlaying(damager)) {
                return;
            }

            //Are they in the same game
            if (!Manager.getArena(victim).equals(Manager.getArena(damager))) {
                return;
            }

            Arena arena = Manager.getArena(victim);
            //Cancel the event if the damager is not the killer
            if (!arena.getGame().getKiller().equals(damager)) {
                e.setCancelled(true);
            }

            //Deal appropriate Killer Damage
            e.setDamage(10);

            //Give Runner Movement Speed and Killer Slowness
            damager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2));
            if (victim.getHealth() > 10) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2));
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

        if (!Manager.getArena(p).getGame().getRunners().contains(p)) {
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
            if (Manager.getArena(player).getState() == GameState.LIVE && Manager.getArena(player).getGame().getKiller().equals(player)) {
                e.getPlayer().getInventory().setHeldItemSlot(0);
            }
        }
    }

    //Killer Swap to Offhand
    @EventHandler
    public void killerNoOffhand(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        if (Manager.isPlaying(player)) {
            if (Manager.getArena(player).getState() == GameState.LIVE && Manager.getArena(player).getGame().getKiller().equals(player)) {
                e.setCancelled(true);
            }
        }
    }

    //Killer Cant Move Items in Inventory
    @EventHandler
    public void killerNoMoveInventory(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        if (Manager.isPlaying(player)) {
            if (Manager.getArena(player).getState() == GameState.LIVE && Manager.getArena(player).getGame().getKiller().equals(player)) {
                e.setCancelled(true);
            }
        }
    }

    //Listen for Killer Reveal
    @EventHandler
    public void killerRightClickSword(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (Manager.isPlaying(player)) {
            if (Manager.getArena(player).getState() == GameState.LIVE && Manager.getArena(player).getGame().getKiller().equals(player)) {
                if (e.getHand() == EquipmentSlot.HAND && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    Manager.getArena(player).getGame().revealRunners();
                }
            }
        }
    }

    //Runner Killed Event
    @EventHandler
    public void runnerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        if (Manager.isPlaying(p) && Manager.getArena(p).getState() == GameState.LIVE) {
            if (Manager.getArena(p).getGame().getRunners().contains(p)) {
                Manager.getArena(p).getGame().runnerKilled(p);
                e.setDeathMessage("");
            }
        }
    }
}
