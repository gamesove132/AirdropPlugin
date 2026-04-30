package com.airdrop;

import com.airdrop.commands.AirdropCommand;
import com.airdrop.managers.AirdropManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AirdropPlugin extends JavaPlugin {

    private AirdropManager airdropManager;

    @Override
    public void onEnable() {
        // Створюємо конфігурацію за замовчуванням
        saveDefaultConfig();

        // Ініціалізуємо менеджер[cite: 3, 5]
        this.airdropManager = new AirdropManager(this);

        // Реєструємо команду
        AirdropCommand cmd = new AirdropCommand(this);
        
        // Встановлюємо лише Executor (обробник команди)
        if (getCommand("airdrops") != null) {
            getCommand("airdrops").setExecutor(cmd);
            // РЯДОК З setTabCompleter ВИДАЛЕНО, щоб не було помилки[cite: 5]
        }

        getLogger().info("AirdropPlugin активовано успішно!");[cite: 5]
    }

    @Override
    public void onDisable() {
        // Зупиняємо всі активні айрдропи перед вимкненням[cite: 3]
        if (airdropManager != null) {
            airdropManager.getActiveAirdrops().values().forEach(event -> event.cancel());[cite: 3, 9]
        }
        getLogger().info("AirdropPlugin вимкнено.");[cite: 5]
    }

    public AirdropManager getAirdropManager() {
        return airdropManager;
    }
}
