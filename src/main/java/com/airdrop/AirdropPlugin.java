package com.airdrop;

import com.airdrop.commands.AirdropCommand;
import com.airdrop.managers.AirdropManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AirdropPlugin extends JavaPlugin {

    private static AirdropPlugin instance;
    private AirdropManager airdropManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        airdropManager = new AirdropManager(this);

        AirdropCommand cmd = new AirdropCommand(this);
        getCommand("airdrops").setExecutor(cmd);
        getCommand("airdrops").setTabCompleter(cmd);

        getLogger().info("AirdropPlugin enabled!");
    }

    @Override
    public void onDisable() {
        if (airdropManager != null) {
            airdropManager.cancelAll();
        }
        getLogger().info("AirdropPlugin disabled!");
    }

    public static AirdropPlugin getInstance() {
        return instance;
    }

    public AirdropManager getAirdropManager() {
        return airdropManager;
    }
}
