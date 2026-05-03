package com.airdrop.commands;

import com.airdrop.AirdropPlugin;
import com.airdrop.model.AirdropEvent;
import com.airdrop.model.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class AirdropCommand implements CommandExecutor, TabCompleter {

    private final AirdropPlugin plugin;

    public AirdropCommand(AirdropPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendMenu(sender);
            return true;
        }

        // /airdrops reload
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("airdrops.reload")) {
                sender.sendMessage("§cНемає дозволу!"); return true;
            }
            plugin.getAirdropManager().reload();
            sender.sendMessage("§a[Айрдроп] Конфіг перезавантажено!");
            return true;
        }

        // /airdrops list
        if (args[0].equalsIgnoreCase("list")) {
            Map<UUID, AirdropEvent> active = plugin.getAirdropManager().getActiveAirdrops();
            if (active.isEmpty()) {
                sender.sendMessage("§7Активних айрдропів немає."); return true;
            }
            sender.sendMessage("§6§lАктивні айрдропи:");
            for (AirdropEvent e : active.values()) {
                sender.sendMessage("§7- " + e.getRarity().getColoredName()
                        + " §7| Світ: §f" + e.getWorld().getName()
                        + " §7| Координати: §b" + e.getCoords()
                        + " §7| Час: §e" + e.getSecondsLeft() + "с");
            }
            return true;
        }

        // /airdrops stop [world]
        if (args[0].equalsIgnoreCase("stop")) {
            if (!sender.hasPermission("airdrops.stop")) {
                sender.sendMessage("§cНемає дозволу!"); return true;
            }
            World world = null;
            if (args.length >= 2) {
                world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    sender.sendMessage("§cСвіт §e" + args[1] + " §cне знайдено!"); return true;
                }
            }
            int stopped = plugin.getAirdropManager().stopAirdrops(world);
            sender.sendMessage(stopped == 0
                    ? "§7Немає активних айрдропів."
                    : "§a[Айрдроп] §fЗупинено §e" + stopped + " §fайрдроп(ів).");
            return true;
        }

        // /airdrops <rarity> <world> [x z]  або  /airdrops <rarity> <world> start
        Rarity rarity = Rarity.fromString(args[0]);
        if (rarity == null) {
            sender.sendMessage("§cНевідома рідкість! Доступні: §eepic§c, §emythic§c, §elegendary");
            return true;
        }

        if (!sender.hasPermission("airdrops.start")) {
            sender.sendMessage("§cНемає дозволу!"); return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cВкажіть світ: §e/airdrops " + args[0].toLowerCase() + " <world> [x z]");
            return true;
        }

        World world = Bukkit.getWorld(args[1]);
        if (world == null) {
            sender.sendMessage("§cСвіт §e" + args[1] + " §cне знайдено!"); return true;
        }

        // З координатами: /airdrops legendary world x z
        if (args.length >= 4) {
            int x, z;
            try {
                x = Integer.parseInt(args[2]);
                z = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cКоординати мають бути числами! §e/airdrops legendary world 100 200");
                return true;
            }
            boolean started = plugin.getAirdropManager().startAirdropAt(rarity, world, x, z);
            if (started) {
                sender.sendMessage(rarity.getColor() + "§lАйрдроп " + rarity.getDisplayName()
                        + " §aзапущено на координатах §b" + x + ", " + z
                        + " §7у світі §f" + world.getName());
            }
            return true;
        }

        // Рандомні координати: /airdrops legendary world  або  /airdrops legendary world start
        boolean started = plugin.getAirdropManager().startAirdrop(rarity, world);
        if (started) {
            sender.sendMessage(rarity.getColor() + "§lАйрдроп " + rarity.getDisplayName()
                    + " §aзапущено у світі §f" + world.getName() + " §aна рандомних координатах!");
        } else {
            sender.sendMessage("§cНе вдалося запустити айрдроп!");
        }
        return true;
    }

    private void sendMenu(CommandSender sender) {
        sender.sendMessage("§8§m--------------------");
        sender.sendMessage("§6§lАйрдроп Система");
        sender.sendMessage("§8§m--------------------");
        sender.sendMessage("§7Рідкості та команди:");
        sender.sendMessage("§e/airdrops §5epic §fworld §7- рандомні координати");
        sender.sendMessage("§e/airdrops §depic §fworld §bX Z §7- конкретні координати");
        sender.sendMessage("§e/airdrops §6legendary §fworld §7- рандомні координати");
        sender.sendMessage("§e/airdrops §6legendary §fworld §b100 200 §7- конкретні координати");
        sender.sendMessage(" ");
        sender.sendMessage("§e/airdrops list §7- активні айрдропи");
        sender.sendMessage("§e/airdrops stop [world] §7- зупинити");
        sender.sendMessage("§e/airdrops reload §7- перезавантажити конфіг");
        sender.sendMessage("§8§m--------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> opts = Arrays.asList("epic", "mythic", "legendary", "list", "stop", "reload");
            List<String> result = new ArrayList<>();
            for (String o : opts) if (o.startsWith(args[0].toLowerCase())) result.add(o);
            return result;
        }

        if (args.length == 2) {
            Rarity r = Rarity.fromString(args[0]);
            if (r != null || args[0].equalsIgnoreCase("stop")) {
                List<String> worlds = new ArrayList<>();
                for (World w : Bukkit.getWorlds())
                    if (w.getName().startsWith(args[1])) worlds.add(w.getName());
                return worlds;
            }
        }

        if (args.length == 3 && Rarity.fromString(args[0]) != null) {
            return Collections.singletonList("<X>");
        }
        if (args.length == 4 && Rarity.fromString(args[0]) != null) {
            return Collections.singletonList("<Z>");
        }

        return Collections.emptyList();
    }
}
