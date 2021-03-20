package com.bungoh.escape.commands.basesubcommands;

import com.bungoh.escape.commands.SubCommand;
import com.bungoh.escape.game.Arena;
import com.bungoh.escape.game.Manager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ArenaListCommand extends SubCommand {

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "List all available arenas";
    }

    @Override
    public String getSyntax() {
        return "/escape list";
    }

    @Override
    public void perform(Player player, String[] args) {

        if (args.length == 1) {
            player.sendMessage(ChatColor.GREEN + "These are the available arenas:");
            for (Arena arena : Manager.getArenas()) {
                if (arena.isReady()) {
                    player.sendMessage(ChatColor.GREEN + " - " + arena.getName());
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid usage! Use " + getSyntax());
        }

    }
}
