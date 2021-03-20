package com.bungoh.escape.commands.basesubcommands;

import com.bungoh.escape.commands.SubCommand;
import com.bungoh.escape.game.Arena;
import com.bungoh.escape.game.Manager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ArenaLeaveCommand extends SubCommand {

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Leave an Escape game";
    }

    @Override
    public String getSyntax() {
        return "/escape leave";
    }

    @Override
    public void perform(Player player, String[] args) {

        if (args.length == 1) {
            if (Manager.isPlaying(player)) {
                Arena arena = Manager.getArena(player);
                arena.removePlayer(player);
                arena.sendMessage(player.getName() + ChatColor.GREEN + " has left the game!");
                player.sendMessage(ChatColor.GREEN + "You left the game!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid usage! Use " + getSyntax());
        }

    }
}
