package com.airdrop.model;

public enum Rarity {
    EPIC("Епічний", "§5", "EPIC"),
    MYTHIC("Міфічний", "§d", "MYTHIC"),
    LEGENDARY("Легендарний", "§6", "LEGENDARY");

    private final String displayName;
    private final String color;
    private final String configKey;

    Rarity(String displayName, String color, String configKey) {
        this.displayName = displayName;
        this.color = color;
        this.configKey = configKey;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public String getConfigKey() { return configKey; }

    public String getColoredName() {
        return color + "§l" + displayName;
    }

    public static Rarity fromString(String s) {
        for (Rarity r : values()) {
            if (r.name().equalsIgnoreCase(s) || r.configKey.equalsIgnoreCase(s)) {
                return r;
            }
        }
        return null;
    }
}
