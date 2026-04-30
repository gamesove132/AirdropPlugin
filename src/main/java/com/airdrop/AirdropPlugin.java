package com.airdrop;

import com.airdrop.commands.AirdropCommand;
import com.airdrop.managers.AirdropManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AirdropPlugin extends JavaPlugin {

    private AirdropManager airdropManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.airdropManager = new AirdropManager(this);

        AirdropCommand cmd = new AirdropCommand(this);
        
        if (getCommand("airdrops") != null) {
            getCommand("airdrops").setExecutor(cmd);
        }

        getLogger().info("AirdropPlugin activated successfully!");
    }

    @Override
    public void onDisable() {
        if (airdropManager != null) {
            airdropManager.getActiveAirdrops().values().forEach(event -> event.cancel());
        }
        getLogger().info("AirdropPlugin disabled.");
    }

    public AirdropManager getAirdropManager() {
        return airdropManager;
    }
}
