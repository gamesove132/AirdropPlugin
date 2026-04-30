package com.airdrop.managers;

import com.airdrop.AirdropPlugin;
import com.airdrop.model.AirdropEvent;
import com.airdrop.model.Rarity;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class AirdropManager {

    private final AirdropPlugin plugin;
    private final Map<UUID, AirdropEvent> activeAirdrops = new HashMap<>();
    private BukkitTask timerTask;

    public AirdropManager(AirdropPlugin plugin) {
        this.plugin = plugin;
        startTimer();
    }

    private void startTimer() {
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, AirdropEvent>> it = activeAirdrops.entrySet().iterator();
                while (it.hasNext()) {
                    AirdropEvent event = it.next().getValue();
                    if (!event.isActive()) {
                        it.remove();
                        continue;
                    }

                    // Show actionbar to players IN THE SAME WORLD
                    for (Player p : event.getWorld().getPlayers()) {
                        p.spigot().sendMessage(
                                ChatMessageType.ACTION_BAR,
                                new TextComponent(event.getActionBarText())
                        );
                    }

                    if (event.getSecondsLeft() <= 0) {
                        openAirdrop(event);
                        event.cancel();
                        it.remove();
                    } else {
                        event.tick();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public boolean startAirdrop(Rarity rarity, World world) {
        // Random location in world
        Location loc = getRandomLocation(world);
        if (loc == null) return false;

        int duration = plugin.getConfig().getInt("duration", 300);
        AirdropEvent event = new AirdropEvent(rarity, world, loc, duration);
        activeAirdrops.put(event.getId(), event);

        // Broadcast to world
        String msg = rarity.getColor() + "§l[Айрдроп] §r" + rarity.getColor()
                + "Айрдроп " + rarity.getDisplayName()
                + " §7розпочато! Координати: §b" + event.getCoords()
                + " §7у світі §f" + world.getName();

        for (Player p : world.getPlayers()) {
            p.sendMessage(msg);
            p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
        }

        // Announce in chat for all worlds? No - only this world (as requested)
        return true;
    }

    private Location getRandomLocation(World world) {
        int border = 2000;
        Random rand = new Random();
        int x = rand.nextInt(border * 2) - border;
        int z = rand.nextInt(border * 2) - border;
        int y = world.getHighestBlockYAt(x, z) + 1;
        return new Location(world, x, y, z);
    }

    private void openAirdrop(AirdropEvent event) {
        Location loc = event.getLocation();
        World world = event.getWorld();

        // Place chest
        loc.getBlock().setType(Material.CHEST);

        Chest chest = (Chest) loc.getBlock().getState();
        Inventory inv = chest.getInventory();

        // Fill with items from config
        List<ItemStack> items = getItemsForRarity(event.getRarity());
        Collections.shuffle(items);

        int count = Math.min(items.size(), 20);
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 27; i++) slots.add(i);
        Collections.shuffle(slots);

        for (int i = 0; i < count && i < slots.size(); i++) {
            inv.setItem(slots.get(i), items.get(i));
        }

        // Announce opened
        String msg = event.getRarity().getColor() + "§l[Айрдроп] §r"
                + event.getRarity().getColor() + "Айрдроп " + event.getRarity().getDisplayName()
                + " §aвідкрито! §7Координати: §b" + event.getCoords();

        for (Player p : world.getPlayers()) {
            p.sendMessage(msg);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }

        // Fireworks
        spawnFirework(loc, event.getRarity());
    }

    private List<ItemStack> getItemsForRarity(Rarity rarity) {
        List<ItemStack> result = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig()
                .getConfigurationSection("rarities." + rarity.getConfigKey() + ".items");

        if (section == null) return result;

        for (String key : section.getKeys(false)) {
            String matName = section.getString(key + ".material", "DIRT");
            int amount = section.getInt(key + ".amount", 1);

            Material mat = Material.matchMaterial(matName);
            if (mat != null) {
                result.add(new ItemStack(mat, amount));
            }
        }
        return result;
    }

    private void spawnFirework(Location loc, Rarity rarity) {
        Color color;
        switch (rarity) {
            case EPIC: color = Color.PURPLE; break;
            case MYTHIC: color = Color.FUCHSIA; break;
            case LEGENDARY: color = Color.ORANGE; break;
            default: color = Color.WHITE;
        }

        org.bukkit.FireworkEffect effect = org.bukkit.FireworkEffect.builder()
                .with(org.bukkit.FireworkEffect.Type.STAR)
                .withColor(color)
                .withFade(Color.WHITE)
                .trail(true)
                .flicker(true)
                .build();

        org.bukkit.entity.Firework fw = loc.getWorld().spawn(loc, org.bukkit.entity.Firework.class);
        org.bukkit.inventory.meta.FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(effect);
        meta.setPower(2);
        fw.setFireworkMeta(meta);
    }

    public Map<UUID, AirdropEvent> getActiveAirdrops() {
        return activeAirdrops;
    }

    public void cancelAll() {
        activeAirdrops.values().forEach(AirdropEvent::cancel);
        activeAirdrops.clear();
        if (timerTask != null) timerTask.cancel();
    }
}
