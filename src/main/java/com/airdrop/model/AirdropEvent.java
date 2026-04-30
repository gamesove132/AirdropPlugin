package com.airdrop.model;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class AirdropEvent {

    private final UUID id;
    private final Rarity rarity;
    private final World world;
    private final Location location;
    private int secondsLeft;
    private boolean active;

    public AirdropEvent(Rarity rarity, World world, Location location, int seconds) {
        this.id = UUID.randomUUID();
        this.rarity = rarity;
        this.world = world;
        this.location = location;
        this.secondsLeft = seconds;
        this.active = true;
    }

    public UUID getId() { return id; }
    public Rarity getRarity() { return rarity; }
    public World getWorld() { return world; }
    public Location getLocation() { return location; }
    public int getSecondsLeft() { return secondsLeft; }
    public boolean isActive() { return active; }

    public void tick() { if (secondsLeft > 0) secondsLeft--; }
    public void cancel() { active = false; }

    public String getCoords() {
        return (int) location.getX() + ", " + (int) location.getY() + ", " + (int) location.getZ();
    }

    public String getActionBarText() {
        String timeStr = formatTime(secondsLeft);
        return rarity.getColor() + "§lАйрдроп " + rarity.getDisplayName()
                + " §7| §e§l" + timeStr
                + " §7| §fКоорд: §b" + getCoords();
    }

    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}
