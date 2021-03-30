package com.bungoh.escape.commands;

import com.bungoh.escape.commands.basesubcommands.ArenaJoinCommand;
import com.bungoh.escape.commands.basesubcommands.ArenaLeaveCommand;
import com.bungoh.escape.commands.basesubcommands.ArenaListCommand;
import com.bungoh.escape.files.ConfigFile;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class BaseCommandManager implements CommandExecutor {

    private ArrayList<SubCommand> subcommands;

    public BaseCommandManager() {
        subcommands = new ArrayList<>();
        subcommands.add(new ArenaJoinCommand());
        subcommands.add(new ArenaLeaveCommand());
        subcommands.add(new ArenaListCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (args.length > 0) {
                boolean isCommand = false;
                for (SubCommand subcommand : subcommands) {
                    if (args[0].equalsIgnoreCase(subcommand.getName())) {
                        isCommand = true;
                        subcommand.perform(p, args);
                    }
                }

                if (!isCommand) {
                    p.sendMessage(ConfigFile.getPrefix() + " " + ChatColor.RED + "That was not a valid command argument!");
                }
            } else {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l========================"));
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lEscape Commands:"));
                for (SubCommand subcommand : subcommands) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&c" + subcommand.getSyntax() + "&7 - " + subcommand.getDescription()));
                }
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l========================"));
            }
        }

        return false;
    }

}
