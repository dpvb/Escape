package com.bungoh.escape.commands;

import com.bungoh.escape.commands.adminsubcommands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class AdminCommandManager implements CommandExecutor {

    private ArrayList<SubCommand> subcommands;

    public AdminCommandManager() {
        subcommands = new ArrayList<>();
        subcommands.add(new ArenaCreateCommand());
        subcommands.add(new ArenaSetCornerOneCommand());
        subcommands.add(new ArenaSetCornerTwoCommand());
        subcommands.add(new ArenaSetLobbySpawnCommand());
        subcommands.add(new ArenaSetRunnerSpawnCommand());
        subcommands.add(new ArenaSetKillerSpawnCommand());
        subcommands.add(new ArenaSetEscapeLocationCommand());
        subcommands.add(new ArenaRemoveCommand());
        subcommands.add(new ArenaReadyCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (p.hasPermission("escape.admin")) {
                if (args.length > 0) {
                    for (SubCommand subcommand : subcommands) {
                        if (args[0].equalsIgnoreCase(subcommand.getName())) {
                            subcommand.perform(p, args);
                        }
                    }
                } else {
                    p.sendMessage("----------------------");
                    p.sendMessage("Escape Admin Commands:");
                    for (SubCommand subcommand : subcommands) {
                        p.sendMessage(subcommand.getSyntax() + " - " + subcommand.getDescription());
                    }
                    p.sendMessage("----------------------");
                }
            }
        }

        return false;
    }

}
