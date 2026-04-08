package com.macecontrol;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Command: /mace amount set <number>
 *
 * Sub-commands:
 *   /mace amount set <n>         - set the global server-wide mace cap
 *   /mace amount set-player <n>  - set the per-player inventory cap
 *   /mace status                 - show current limits and live count
 */
public class MaceCommand implements CommandExecutor, TabCompleter {

    private final MaceControl plugin;

    public MaceCommand(MaceControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("macecontrol.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        // /mace status
        if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            int live = plugin.getTracker().countGlobal();
            sender.sendMessage("§6§lMaceControl Status");
            sender.sendMessage("§7Global limit: §e" + plugin.getMaxGlobal());
            sender.sendMessage("§7Per-player limit: §e" + plugin.getMaxPerPlayer());
            sender.sendMessage("§7Maces currently alive: §e" + live);
            return true;
        }

        // /mace amount set <n>
        // /mace amount set-player <n>
        if (args.length == 3 && args[0].equalsIgnoreCase("amount")) {

            String subCmd = args[1].toLowerCase();
            int value;

            try {
                value = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c'" + args[2] + "' is not a valid number.");
                return true;
            }

            if (value < 0) {
                sender.sendMessage("§cThe limit cannot be negative.");
                return true;
            }

            switch (subCmd) {
                case "set" -> {
                    plugin.setMaxGlobal(value);
                    sender.sendMessage("§aGlobal mace limit set to §e" + value + "§a.");
                    plugin.getServer().broadcastMessage(
                            "§6[MaceControl] §fThe server-wide mace limit has been updated to §e" + value + "§f.");
                }
                case "set-player" -> {
                    plugin.setMaxPerPlayer(value);
                    sender.sendMessage("§aPer-player mace limit set to §e" + value + "§a.");
                }
                default -> sendUsage(sender);
            }
            return true;
        }

        sendUsage(sender);
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§6§lMaceControl Commands");
        sender.sendMessage("§e/mace amount set §7<number>        §f- set global mace cap");
        sender.sendMessage("§e/mace amount set-player §7<number> §f- set per-player cap");
        sender.sendMessage("§e/mace status                        §f- show current limits");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("macecontrol.admin")) return Collections.emptyList();

        if (args.length == 1) return filterStart(args[0], "amount", "status");
        if (args.length == 2 && args[0].equalsIgnoreCase("amount"))
            return filterStart(args[1], "set", "set-player");
        if (args.length == 3 && args[0].equalsIgnoreCase("amount"))
            return filterStart(args[2], "1", "2", "3", "5", "10");
        return Collections.emptyList();
    }

    private List<String> filterStart(String input, String... options) {
        return Arrays.stream(options)
                .filter(o -> o.startsWith(input.toLowerCase()))
                .toList();
    }
}
