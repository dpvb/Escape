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

public class RunnerCloak extends RunnerAbility {

    private int timer;
    private BukkitTask task;

    public RunnerCloak(Runner runner) {
        //Call Superclass
        super("Invisibility", ConfigFile.getRunnerInvisCooldown(), runner);

        //Cooldown Timer
        timer = -1;

        //Create the Item
        generateItem();
    }

    @Override
    public ItemStack generateItem() {
        item = new ItemStack(Material.GHAST_TEAR);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GRAY + name);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "Right Click to get " + ChatColor.DARK_RED + "invisibility and speed.");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

    @Override
    public void use() {
        if (timer == -1) {
            Player p = runner.player;

            p.sendMessage(ConfigFile.getPrefix() + " " + ChatColor.RED + "You went invis for 3 seconds!");
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 3));
            p.getInventory().setHeldItemSlot(1);

            timer = cooldown;

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (timer == 0) {
                        p.sendMessage(ConfigFile.getPrefix() + " " + ChatColor.GREEN + "Your invis ability is back up!");
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

        } else {
            runner.player.sendMessage(ConfigFile.getPrefix() + " " + ChatColor.RED + "Invis is on cooldown!");
        }
    }

    @Override
    public void cleanup() {
        if (task != null) {
            task.cancel();
        }
    }
}
