package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import com.bungoh.escape.files.ConfigFile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Killer extends GameParticipant {

    protected ItemStack sword;
    protected KillerReveal killerReveal;
    protected boolean canHit;
    protected long hitCooldown;
    protected KillerListener listener;
    protected BukkitTask hitTask;

    public Killer(Game game, Player player) {
        super(game, player);
        init();
    }

    private void init() {
        //Create Killer Sword
        sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta swordMeta = sword.getItemMeta();
        swordMeta.setUnbreakable(true);
        swordMeta.setDisplayName(ChatColor.DARK_RED + "KILLER SWORD");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "Right Click to use " + ChatColor.DARK_RED + "Reveal");
        swordMeta.setLore(lore);
        sword.setItemMeta(swordMeta);
        sword.addEnchantment(Enchantment.KNOCKBACK, 2);
        player.getInventory().setItem(0, sword);
        player.getInventory().setHeldItemSlot(0);

        //Add Ability
        killerReveal = new KillerReveal();

        //Hit Cooldowns
        canHit = true;
        hitCooldown = 2 * 20;

        //Register Listener
        listener = new KillerListener();
        Bukkit.getPluginManager().registerEvents(listener, Escape.getPlugin());
    }

    @Override
    public void cleanup() {
        if (hitTask != null) {
            hitTask.cancel();
        }
        killerReveal.cleanup();
        HandlerList.unregisterAll(listener);
    }

    class KillerReveal {

        protected int cooldown;
        protected int timer;
        protected BukkitTask task;

        public KillerReveal() {
            cooldown = 25;
            timer = -1;
        }

        public void use() {
            if (timer == -1) {
                player.sendMessage(ConfigFile.getPrefix() + " " + ChatColor.RED + "Runners have been revealed!");
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));

                for (Runner r : game.getRunners()) {
                    r.player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1));
                }

                timer = cooldown;

                task = new BukkitRunnable(){
                    @Override
                    public void run() {
                        if (timer == 0) {
                            player.sendMessage(ChatColor.GREEN + "Your Reveal is back up!");
                            cancel();
                            timer = -1;
                        } else {
                            timer--;
                        }

                        if (timer != -1) {
                            player.setLevel(timer);
                        }
                    }
                }.runTaskTimerAsynchronously(Escape.getPlugin(), 0L, 20L);

            } else {
                player.sendMessage(ConfigFile.getPrefix() + " " + ChatColor.RED + "Reveal is on cooldown!");
            }
        }

        public void cleanup() {
            if (task != null) {
                task.cancel();
            }
        }

    }

    class KillerListener implements Listener {

        @EventHandler
        public void onInteract(PlayerInteractEvent e) {
            if (!e.getPlayer().equals(player)) {
                return;
            }

            if (e.getItem() == null) {
                return;
            }

            if (e.getItem().equals(sword) && e.getHand() == EquipmentSlot.HAND && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                killerReveal.use();
            }
        }

        @EventHandler
        public void killerHitRunner(EntityDamageByEntityEvent e) {
            if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
                Player v = (Player) e.getEntity();
                Player d = (Player) e.getDamager();

                if (!player.equals(d)) {
                    e.setCancelled(true);
                    return;
                }

                if (canHit) {
                    //Deal Damage
                    e.setDamage(10);

                    //Give Runner Movement Speed and Killer Slowness
                    d.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 4));
                    if (v.getHealth() > 10) {
                        v.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
                    }

                    canHit = false;

                    //Setup Task
                    hitTask = new BukkitRunnable() {
                        @Override
                        public void run() {
                            canHit = true;
                        }
                    }.runTaskLater(Escape.getPlugin(), hitCooldown);
                } else {
                    e.setCancelled(true);
                }
            }
        }


    }

}
