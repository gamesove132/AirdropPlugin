package com.airdrop.commands;

import com.airdrop.AirdropPlugin;
import com.airdrop.model.AirdropEvent;
import com.airdrop.model.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class AirdropCommand implements CommandExecutor {

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

        // Перезавантаження конфігу[cite: 5, 6]
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("airdrops.admin")) {
                sender.sendMessage("§cУ вас немає дозволу!");
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage("§aКонфігурацію успішно перезавантажено!");
            return true;
        }

        // Зупинка айрдропів[cite: 3, 4]
        if (args[0].equalsIgnoreCase("stop")) {
            if (!sender.hasPermission("airdrops.admin")) {
                sender.sendMessage("§cУ вас немає дозволу!");
                return true;
            }
            
            Map<UUID, AirdropEvent> active = plugin.getAirdropManager().getActiveAirdrops();
            if (active.isEmpty()) {
                sender.sendMessage("§7Немає активних айрдропів.");
                return true;
            }
            
            active.values().forEach(AirdropEvent::cancel); // Скасування подій[cite: 3, 9]
            sender.sendMessage("§cУсі активні айрдропи зупинено!");
            return true;
        }

        // Список активних айрдропів[cite: 4]
        if (args[0].equalsIgnoreCase("list")) {
            Map<UUID, AirdropEvent> active = plugin.getAirdropManager().getActiveAirdrops();
            if (active.isEmpty()) {
                sender.sendMessage("§7Активних айрдропів немає.");
                return true;
            }
            sender.sendMessage("§6§lАктивні айрдропи:");
            for (AirdropEvent e : active.values()) {
                sender.sendMessage("§7- " + e.getRarity().getColoredName()
                        + " §7| Координати: §b" + e.getCoords()
                        + " §7| Час: §e" + e.getSecondsLeft() + "с");
            }
            return true;
        }

        // Запуск айрдропа[cite: 4]
        if (args.length >= 2 && args[1].equalsIgnoreCase("start")) {
            if (!sender.hasPermission("airdrops.start")) {
                sender.sendMessage("§cУ вас немає дозволу!");
                return true;
            }

            Rarity rarity = Rarity.fromString(args[0]);
            if (rarity == null) {
                sender.sendMessage("§cНевідома рідкість!");
                return true;
            }

            World world;
            if (args.length >= 3) {
                world = Bukkit.getWorld(args[2]);
            } else if (sender instanceof Player) {
                world = ((Player) sender).getWorld();
            } else {
                sender.sendMessage("§cВкажіть світ!");
                return true;
            }

            if (world != null && plugin.getAirdropManager().startAirdrop(rarity, world)) {
                sender.sendMessage("§aАйрдроп запущено!");
            } else {
                sender.sendMessage("§cПомилка запуску!");
            }
            return true;
        }

        sendMenu(sender);
        return true;
    }

    private void sendMenu(CommandSender sender) {
        sender.sendMessage("§6§lАйрдроп Система");
        sender.sendMessage("§e/airdrops list §7- активні айрдропи");
        sender.sendMessage("§c/airdrops stop §7- зупинити всі");
        sender.sendMessage("§b/airdrops reload §7- перезавантажити");
        sender.sendMessage("§7/airdrops <rarity> start [world]");
    }
}
