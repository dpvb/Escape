package com.bungoh.escape.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class BaseCommandManager implements CommandExecutor {

    private ArrayList<SubCommand> subcommands;

    public BaseCommandManager() {
        subcommands = new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (args.length > 0) {
                for (SubCommand subcommand : subcommands) {
                    if (args[0].equalsIgnoreCase(subcommand.getName())) {
                        subcommand.perform(p, args);
                    }
                }
            } else {
                p.sendMessage("----------------------");
                p.sendMessage("Escape Commands:");
                for (SubCommand subcommand : subcommands) {
                    p.sendMessage(subcommand.getSyntax() + " - " + subcommand.getDescription());
                }
                p.sendMessage("----------------------");
            }
        }

        return false;
    }

}
