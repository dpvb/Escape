package com.bungoh.escape.commands.adminsubcommands;

import com.bungoh.escape.commands.SubCommand;
import com.bungoh.escape.files.DataFile;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ArenaSetKillerSpawnCommand extends SubCommand {

    @Override
    public String getName() {
        return "setkillerspawn";
    }

    @Override
    public String getDescription() {
        return "Sets the killer spawn in the Arena";
    }

    @Override
    public String getSyntax() {
        return "/escapeadmin setkillerspawn [arena name]";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length == 2) {
            String arenaName = args[1];

            switch (DataFile.setArenaKillerSpawn(arenaName, player.getLocation())) {
                case 0:
                    player.sendMessage(ChatColor.RED + "That arena does not exist!");
                    break;
                case 1:
                    player.sendMessage(ChatColor.RED + "You can't do that. The arena is currently in the ready state.");
                    break;
                case 2:
                    player.sendMessage(ChatColor.GREEN + "The arena killer spawn location was successfully set!");
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "An unexpected error occurred when adding a spawn location.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid usage! Use " + getSyntax());
        }
    }
}
