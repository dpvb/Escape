package com.bungoh.escape.commands;

import com.bungoh.escape.commands.adminsubcommands.*;
import com.bungoh.escape.files.ConfigFile;
import com.bungoh.escape.utils.Messages;
import org.bukkit.ChatColor;
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
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lEscape Admin Commands:"));
                    for (SubCommand subcommand : subcommands) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&c" + subcommand.getSyntax() + "&7 - " + subcommand.getDescription()));
                    }
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l========================"));
                }
            }
        }

        return false;
    }

}
