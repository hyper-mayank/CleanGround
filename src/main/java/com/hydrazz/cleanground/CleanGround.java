package com.hydrazz.cleanground;

import com.hydrazz.cleanground.cleaner.CleanerService;
import com.hydrazz.cleanground.command.CleanGroundCommand;
import com.hydrazz.cleanground.config.ConfigManager;
import com.hydrazz.cleanground.hook.PlaceholderHook;
import com.hydrazz.cleanground.util.SchedulerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CleanGround extends JavaPlugin {

    private static CleanGround instance;
    private ConfigManager configManager;
    private CleanerService cleanerService;
    private PlaceholderHook placeholderHook;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize and load configurations
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        // Initialize cleaner service
        this.cleanerService = new CleanerService(this);
        this.cleanerService.start();

        // Register core command & tab completer
        CleanGroundCommand commandHandler = new CleanGroundCommand(this);
        if (getCommand("cleanground") != null) {
            getCommand("cleanground").setExecutor(commandHandler);
            getCommand("cleanground").setTabCompleter(commandHandler);
        }

        // Initialize PlaceholderAPI hook
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                this.placeholderHook = new PlaceholderHook(this);
                if (this.placeholderHook.register()) {
                    getLogger().info("PlaceholderAPI expansion '%cleanground_*%' registered successfully.");
                }
            } catch (Throwable t) {
                getLogger().warning("Failed to register PlaceholderAPI expansion: " + t.getMessage());
            }
        }

        // Log platform startup banner
        String platform = SchedulerAdapter.isFolia() ? "Folia (Region Multi-Threading)" : "Paper/Spigot (Synchronous Scheduler)";
        getLogger().info("==================================================");
        getLogger().info("  CleanGround Pro v" + getDescription().getVersion() + " initialized successfully!");
        getLogger().info("  Platform: " + platform);
        getLogger().info("  Sweep Interval: " + this.configManager.getSweepIntervalSeconds() + "s");
        getLogger().info("  PlaceholderAPI: " + (placeholderHook != null ? "REGISTERED" : "DISABLED"));
        getLogger().info("==================================================");
    }

    @Override
    public void onDisable() {
        if (this.cleanerService != null) {
            this.cleanerService.stop();
        }

        if (this.placeholderHook != null) {
            try {
                this.placeholderHook.unregister();
            } catch (Throwable ignored) {}
            this.placeholderHook = null;
        }

        getLogger().info("CleanGround Pro v" + getDescription().getVersion() + " disabled cleanly.");
        instance = null;
    }

    public void reloadPlugin() {
        if (this.configManager != null) {
            this.configManager.load();
        }
        if (this.cleanerService != null) {
            this.cleanerService.start();
        }
        getLogger().info("CleanGround Pro configuration and sweep timers reloaded.");
    }

    public static CleanGround getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CleanerService getCleanerService() {
        return cleanerService;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }
}
