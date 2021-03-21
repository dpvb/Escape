package com.bungoh.escape.commands.adminsubcommands;

import com.bungoh.escape.commands.SubCommand;
import com.bungoh.escape.files.ConfigFile;
import com.bungoh.escape.files.DataFile;
import com.bungoh.escape.game.Manager;
import com.bungoh.escape.utils.Messages;
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
            Messages message = DataFile.removeArena(arenaName);
            switch (message) {
                case ARENA_DOES_NOT_EXIST:
                case ARENA_NOT_EDITABLE:
                    player.sendMessage(ConfigFile.getMessage(message.getPath()));
                    break;
                case ARENA_REMOVED:
                    Manager.removeArena(Manager.getArena(args[1]));
                    player.sendMessage(ConfigFile.getMessage(message.getPath()));
                    break;
                default:
                    player.sendMessage(ConfigFile.getMessage(Messages.UNEXPECTED_ERROR.getPath()));
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid usage! Use " + getSyntax());
        }

    }

}
