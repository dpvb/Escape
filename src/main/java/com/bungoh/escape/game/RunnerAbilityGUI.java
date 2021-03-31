package com.bungoh.escape.game;

import com.bungoh.escape.Escape;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class RunnerAbilityGUI {

    protected String guiName;
    protected ItemStack invisItem;
    protected ItemStack blindItem;

    public RunnerAbilityGUI(Runner runner) {

        guiName = ChatColor.BLUE + "Ability Selection";

        Inventory gui = Bukkit.createInventory(null, 9, guiName);

        invisItem = new ItemStack(Material.GHAST_TEAR);
        ItemMeta invisMeta = invisItem.getItemMeta();
        invisMeta.setDisplayName(ChatColor.RED + "INVISIBILITY");
        invisItem.setItemMeta(invisMeta);
        gui.setItem(3, invisItem);

        blindItem = new ItemStack(Material.INK_SAC);
        ItemMeta blindMeta = blindItem.getItemMeta();
        blindMeta.setDisplayName(ChatColor.RED + "BLIND");
        blindItem.setItemMeta(blindMeta);
        gui.setItem(5, blindItem);

        new BukkitRunnable() {
            @Override
            public void run() {
                runner.player.openInventory(gui);
            }
        }.runTaskLater(Escape.getPlugin(), 2L);


    }

}
