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

        // /airdrops — show menu
        if (args.length == 0) {
            sendMenu(sender);
            return true;
        }

        // /airdrops <rarity> start [world]
        if (args.length >= 2 && args[1].equalsIgnoreCase("start")) {
            if (!sender.hasPermission("airdrops.start")) {
                sender.sendMessage("§cУ вас немає дозволу!");
                return true;
            }

            Rarity rarity = Rarity.fromString(args[0]);
            if (rarity == null) {
                sender.sendMessage("§cНевідома рідкість! Доступні: epic, mythic, legendary");
                return true;
            }

            World world;
            if (args.length >= 3) {
                world = Bukkit.getWorld(args[2]);
                if (world == null) {
                    sender.sendMessage("§cСвіт §e" + args[2] + " §cне знайдено!");
                    return true;
                }
            } else if (sender instanceof Player) {
                world = ((Player) sender).getWorld();
            } else {
                sender.sendMessage("§cВкажіть світ: /airdrops " + args[0] + " start <world>");
                return true;
            }

            boolean started = plugin.getAirdropManager().startAirdrop(rarity, world);
            if (started) {
                sender.sendMessage(rarity.getColor() + "§lАйрдроп " + rarity.getDisplayName()
                        + " §aзапущено у світі §f" + world.getName() + "§a!");
            } else {
                sender.sendMessage("§cНе вдалося запустити айрдроп!");
            }
            return true;
        }

        // /airdrops reload
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("airdrops.reload")) {
                sender.sendMessage("§cУ вас немає дозволу!");
                return true;
            }
            plugin.getAirdropManager().reload();
            sender.sendMessage("§a[Айрдроп] §fКонфіг перезавантажено!");
            return true;
        }

        // /airdrops stop [world]
        if (args[0].equalsIgnoreCase("stop")) {
            if (!sender.hasPermission("airdrops.stop")) {
                sender.sendMessage("§cУ вас немає дозволу!");
                return true;
            }

            World world = null;
            if (args.length >= 2) {
                world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    sender.sendMessage("§cСвіт §e" + args[1] + " §cне знайдено!");
                    return true;
                }
            }

            int stopped = plugin.getAirdropManager().stopAirdrops(world);
            if (stopped == 0) {
                sender.sendMessage("§7Немає активних айрдропів" + (world != null ? " у світі §f" + world.getName() : "") + "§7.");
            } else {
                sender.sendMessage("§a[Айрдроп] §fЗупинено §e" + stopped + " §fайрдроп(ів).");
            }
            return true;
        }

        // /airdrops list — show active
        if (args[0].equalsIgnoreCase("list")) {
            Map<UUID, AirdropEvent> active = plugin.getAirdropManager().getActiveAirdrops();
            if (active.isEmpty()) {
                sender.sendMessage("§7Активних айрдропів немає.");
                return true;
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

        sendMenu(sender);
        return true;
    }

    private void sendMenu(CommandSender sender) {
        sender.sendMessage("§8§m--------------------");
        sender.sendMessage("§6§lАйрдроп Система");
        sender.sendMessage("§8§m--------------------");
        sender.sendMessage("§7Рідкості:");
        sender.sendMessage("§5§l● Епічний §7- /airdrops epic start [world]");
        sender.sendMessage("§d§l● Міфічний §7- /airdrops mythic start [world]");
        sender.sendMessage("§6§l● Легендарний §7- /airdrops legendary start [world]");
        sender.sendMessage(" ");
        sender.sendMessage("§7Команди:");
        sender.sendMessage("§e/airdrops list §7- активні айрдропи");
        sender.sendMessage("§e/airdrops stop [world] §7- зупинити айрдроп");
        sender.sendMessage("§e/airdrops reload §7- перезавантажити конфіг");
        sender.sendMessage("§8§m--------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> opts = new ArrayList<>(Arrays.asList("epic", "mythic", "legendary", "list", "stop", "reload"));
            List<String> result = new ArrayList<>();
            for (String o : opts) {
                if (o.startsWith(args[0].toLowerCase())) result.add(o);
            }
            return result;
        }

        if (args.length == 2) {
            if (Rarity.fromString(args[0]) != null) {
                return Collections.singletonList("start");
            }
            if (args[0].equalsIgnoreCase("stop")) {
                List<String> worlds = new ArrayList<>();
                for (World w : Bukkit.getWorlds()) {
                    if (w.getName().startsWith(args[1])) worlds.add(w.getName());
                }
                return worlds;
            }
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("start")) {
            List<String> worlds = new ArrayList<>();
            for (World w : Bukkit.getWorlds()) {
                if (w.getName().startsWith(args[2])) worlds.add(w.getName());
            }
            return worlds;
        }

        return Collections.emptyList();
    }
}
