package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import com.bungoh.escape.files.ConfigFile;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class RunnerBlind extends RunnerAbility {

    private int timer;
    private BukkitTask task;

    public RunnerBlind(Runner runner) {
        //Call Superclass
        super("Blind", 50, runner);

        //Cooldown Timer
        timer = 0;

        //Generate the Item
        generateItem();
    }


    @Override
    public ItemStack generateItem() {
        item = new ItemStack(Material.INK_SAC);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + name);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "Right click to " + ChatColor.DARK_RED + " blind " + ChatColor.RED + " the Killer.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void use() {
        if (timer == -1) {
            Player p = runner.player;
            Killer killer = Manager.getArena(p).getGame().getKiller();
            p.sendMessage(ConfigFile.getPrefix() + " " + ChatColor.RED + "You blinded the Killer for 3 seconds!");
            killer.player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            p.getInventory().setHeldItemSlot(1);

            timer = cooldown;

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (timer == 0) {
                        p.sendMessage(ConfigFile.getPrefix() + " " + ChatColor.GREEN + "Your blind ability is back up!");
                        cancel();
                        timer = -1;
                    } else {
                        timer--;
                    }

                    if (timer != -1) {
                        p.setLevel(timer);
                    }
                }
            }.runTaskTimerAsynchronously(Escape.getPlugin(), 0L, 20L);
        }
    }

    @Override
    public void cleanup() {
        if (task != null) {
            task.cancel();
        }
    }
}
