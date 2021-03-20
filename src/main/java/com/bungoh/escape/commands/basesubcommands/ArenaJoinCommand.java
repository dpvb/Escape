package com.bungoh.escape.commands.basesubcommands;

import com.bungoh.escape.commands.SubCommand;
import com.bungoh.escape.game.Arena;
import com.bungoh.escape.game.Manager;
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
                    player.sendMessage(ChatColor.RED + "That game is not recruiting! You can not join.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "That arena does not exist!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid usage! Use " + getSyntax());
        }

    }
}
