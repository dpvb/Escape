package com.bungoh.escape.commands.adminsubcommands;

import com.bungoh.escape.commands.SubCommand;
import com.bungoh.escape.files.DataFile;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ArenaReadyCommand extends SubCommand {

    @Override
    public String getName() {
        return "ready";
    }

    @Override
    public String getDescription() {
        return "Set an Arena to the ready state";
    }

    @Override
    public String getSyntax() {
        return "/escapeadmin ready [arena name]";
    }

    @Override
    public void perform(Player player, String[] args) {

        if (args.length == 2) {
            switch (DataFile.arenaToggleReady(args[1])) {
                case 0:
                    player.sendMessage(ChatColor.RED + "That arena does not exist!");
                    break;
                case 1:
                    player.sendMessage(ChatColor.RED + "That arena is not completely setup yet.");
                    break;
                case 2:
                    //Manager.getArena(args[1]).setup();
                    player.sendMessage(ChatColor.GREEN + "The arena was set to " + ChatColor.YELLOW + "ready.");
                    break;
                case 3:
                    //Manager.getArena(args[1]).setReady(false);
                    player.sendMessage(ChatColor.GREEN + "The arena was set to not " + ChatColor.YELLOW + "ready.");
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "An unknown error occurred with the ready command.");
                    break;
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid usage! Use " + getSyntax());
        }

    }
}
