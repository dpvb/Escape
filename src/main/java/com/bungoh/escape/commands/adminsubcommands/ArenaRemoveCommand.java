package com.bungoh.escape.commands.adminsubcommands;

import com.bungoh.escape.commands.SubCommand;
import com.bungoh.escape.files.DataFile;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ArenaRemoveCommand extends SubCommand {

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Removes the Arena";
    }

    @Override
    public String getSyntax() {
        return "/escapeadmin remove [arena name]";
    }

    @Override
    public void perform(Player player, String[] args) {

        if (args.length == 2) {
            String arenaName = args[1];

            switch (DataFile.removeArena(arenaName)) {
                case 0:
                    player.sendMessage(ChatColor.RED + "That arena does not exist!");
                    break;
                case 1:
                    player.sendMessage(ChatColor.RED + "You can't do that. The arena is currently in the ready state.");
                    break;
                case 2:
                    //Manager.removeArena(Manager.getArena(args[1]));
                    player.sendMessage(ChatColor.GREEN + "You successfully removed the " + arenaName + " Arena!");
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "An unexpected error occurred when removing the Arena.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid usage! Use " + getSyntax());
        }

    }

}
