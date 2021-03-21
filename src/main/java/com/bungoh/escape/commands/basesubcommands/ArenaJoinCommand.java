package com.bungoh.escape.commands.basesubcommands;

import com.bungoh.escape.commands.SubCommand;
import com.bungoh.escape.files.ConfigFile;
import com.bungoh.escape.game.Arena;
import com.bungoh.escape.game.Manager;
import com.bungoh.escape.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ArenaJoinCommand extends SubCommand {

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Join an Escape game";
    }

    @Override
    public String getSyntax() {
        return "/escape join [arena name]";
    }

    @Override
    public void perform(Player player, String[] args) {

        if (args.length == 2) {
            String arenaName = args[1];
            Arena arena = Manager.getArena(arenaName);
            if (arena != null) {
                if (Manager.isRecruiting(arenaName)) {
                    arena.addPlayer(player);
                } else {
                    player.sendMessage(ConfigFile.getMessage(Messages.ARENA_NOT_RECRUITING.getPath()));
                }
            } else {
                player.sendMessage(ConfigFile.getMessage(Messages.ARENA_DOES_NOT_EXIST.getPath()));
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid usage! Use " + getSyntax());
        }

    }
}
