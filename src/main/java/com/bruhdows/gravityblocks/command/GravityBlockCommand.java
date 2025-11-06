package com.bruhdows.gravityblocks.command;

import com.bruhdows.gravityblocks.GravityBlocksPlugin;
import com.bruhdows.gravityblocks.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record GravityBlockCommand(GravityBlocksPlugin plugin) implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendRichMessage("<red>Only players can use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "stick":
                giveStick(player);
                break;

            case "spawn":
                if (args.length < 2) {
                    TextUtil.sendMessage(player, "<red>Usage: /gravityblock spawn <material> [size]");
                    return true;
                }
                spawnBlock(player, args);
                break;

            case "cleanup":
                cleanup(player);
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void giveStick(Player player) {
        player.getInventory().addItem(plugin.getGravityStickManager().createGravityStick());
        TextUtil.sendMessage(player, "<green>You received a Gravity Stick!");
    }

    private void spawnBlock(Player player, String[] args) {
        String materialName = args[1].toUpperCase();
        Material material;

        try {
            material = Material.valueOf(materialName);
            if (!material.isBlock()) {
                TextUtil.sendMessage(player, "<red>" + materialName + " is not a valid block material!");
                return;
            }
        } catch (IllegalArgumentException e) {
            TextUtil.sendMessage(player, "<red>" + materialName + " is not a valid material!");
            return;
        }

        double size = 1.0;
        if (args.length >= 3) {
            try {
                size = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                TextUtil.sendMessage(player, "<red>Invalid size! Must be a number.");
                return;
            }
        }

        Location spawnLoc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(3));
        plugin.getGravityBlockManager().createGravityBlock(spawnLoc, material, size);

        TextUtil.sendMessage(player, "<green>Spawned a " + material.name().toLowerCase() + " gravity block with size " + size + "!");
    }

    private void cleanup(Player player) {
        int count = plugin.getGravityBlockManager().getBlockCount();
        plugin.getGravityBlockManager().cleanupAll();
        TextUtil.sendMessage(player, "<green>Cleaned up " + count + " gravity block(s)!");
    }

    private void sendHelp(Player player) {
        TextUtil.sendMessage(player, "<yellow>/gravityblock stick <gray>- Get a gravity stick<br>" +
                "<yellow>/gravityblock spawn <material> [size] <gray>- Spawn a gravity block<br>" +
                "<yellow>/gravityblock cleanup <gray>- Remove all gravity blocks");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("stick", "spawn", "cleanup"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            for (Material material : Material.values()) {
                if (material.isBlock()) {
                    completions.add(material.name().toLowerCase());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("spawn")) {
            completions.addAll(Arrays.asList("0.5", "1.0", "1.5", "2.0", "3.0"));
        }

        return completions;
    }
}