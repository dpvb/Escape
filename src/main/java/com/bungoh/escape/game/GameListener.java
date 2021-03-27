package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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

            if (arena.getState() != GameState.LIVE) {
                e.setCancelled(true);
                return;
            }

            //Cancel the event if the damager is not the killer
            if (!arena.getGame().getKiller().equals(damager)) {
                e.setCancelled(true);
                return;
            }

            //Check if hit is in cooldown
            if (!arena.getGame().getHitCooldown()) {
                //Deal Damage
                e.setDamage(10);

                //Give Runner Movement Speed and Killer Slowness
                damager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 4));
                if (victim.getHealth() > 10) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 3));
                }

                arena.getGame().setHitCooldown(true);

                //Setup Task
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        arena.getGame().setHitCooldown(false);
                    }
                }.runTaskLaterAsynchronously(Escape.getPlugin(), arena.getGame().getHitCooldownLen());
            } else {
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
            if (Manager.getArena(player).getState() == GameState.LIVE) {
                e.setCancelled(true);
            }
        }
    }

    //Listen for Killer Reveal // Runner Invis
    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (Manager.isPlaying(player)) {
            Arena arena = Manager.getArena(player);
            if (arena.getState() == GameState.LIVE) {
                Game game = arena.getGame();

                if (e.getItem() == null) {
                    return;
                }

                if (game.getKiller().equals(player)) {

                    if (e.getItem().getType().equals(Material.DIAMOND_SWORD) && e.getHand() == EquipmentSlot.HAND && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                        game.revealRunners();
                    }
                } else {
                    if (e.getItem().getType().equals(Material.INK_SAC) && e.getHand() == EquipmentSlot.HAND && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                        game.runnerInvis(player);
                    }
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
