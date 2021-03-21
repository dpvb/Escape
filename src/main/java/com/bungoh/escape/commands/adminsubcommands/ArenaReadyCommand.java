package com.bungoh.escape.commands.adminsubcommands;

import com.bungoh.escape.commands.SubCommand;
import com.bungoh.escape.files.ConfigFile;
import com.bungoh.escape.files.DataFile;
import com.bungoh.escape.game.Manager;
import com.bungoh.escape.utils.InsufficientGeneratorAmount;
import com.bungoh.escape.utils.Messages;
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

            String arenaName = args[1];
            Messages message = DataFile.arenaToggleReady(arenaName);
            switch (message) {
                case ARENA_DOES_NOT_EXIST:
                case ARENA_NOT_SETUP:
                    player.sendMessage(ConfigFile.getMessage(message.getPath()));
                    break;
                case ARENA_READY:
                    try {
                        Manager.getArena(args[1]).setup();
                        player.sendMessage(ConfigFile.getMessage(message.getPath()));
                    } catch (InsufficientGeneratorAmount e) {
                        DataFile.arenaToggleReady(arenaName);
                        player.sendMessage(ConfigFile.getMessage(Messages.ARENA_INSUFFICIENT_GENS.getPath()));
                    }
                    break;
                case ARENA_NOT_READY:
                    Manager.getArena(args[1]).setReady(false);
                    player.sendMessage(ConfigFile.getMessage(message.getPath()));
                    break;
                default:
                    player.sendMessage(ConfigFile.getMessage(Messages.UNEXPECTED_ERROR.getPath()));
                    break;
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid usage! Use " + getSyntax());
        }

    }
}
