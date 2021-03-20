package com.bungoh.escape.commands.adminsubcommands;

import com.bungoh.escape.commands.SubCommand;
import com.bungoh.escape.files.DataFile;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ArenaCreateCommand extends SubCommand {

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Create an Arena";
    }

    @Override
    public String getSyntax() {
        return "/escapeadmin create [name]";
    }

    @Override
    public void perform(Player player, String[] args) {

        if (args.length == 2) {
            String arenaName = args[1];

            if (DataFile.initArena(arenaName, player.getWorld())) {
                player.sendMessage(ChatColor.GREEN + "The arena has been successfully created!");
            } else {
                player.sendMessage(ChatColor.RED + "An arena with that name already exists!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid Usage! Use " + getSyntax());
        }

    }
}