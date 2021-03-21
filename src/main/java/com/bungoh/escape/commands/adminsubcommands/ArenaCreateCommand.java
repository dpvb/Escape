package com.bungoh.escape.commands.adminsubcommands;

import com.bungoh.escape.commands.SubCommand;
import com.bungoh.escape.files.ConfigFile;
import com.bungoh.escape.files.DataFile;
import com.bungoh.escape.game.Manager;
import com.bungoh.escape.utils.Messages;
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
            Messages message = DataFile.initArena(arenaName, player.getWorld());
            switch (message) {
                case ARENA_CREATED:
                    player.sendMessage(ConfigFile.getMessage(message.getPath()));
                    Manager.addArena(arenaName);
                    break;
                case ARENA_ALREADY_EXISTS:
                    player.sendMessage(ConfigFile.getMessage(message.getPath()));
                    break;
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid Usage! Use " + getSyntax());
        }

    }
}