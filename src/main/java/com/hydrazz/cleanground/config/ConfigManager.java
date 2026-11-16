package com.hydrazz.cleanground.config;

import com.hydrazz.cleanground.CleanGround;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final CleanGround plugin;

    private boolean sweepEnabled;
    private int sweepIntervalSeconds;
    private Set<Integer> warningTimes;
    private boolean removeEmptyVehicles;
    private boolean removeProjectiles;

    private boolean protectNamedItems;
    private boolean protectLoreItems;
    private boolean protectEnchantedItems;
    private int minimumAgeTicks;
    private Set<String> itemWhitelist;

    private boolean mobCullingEnabled;
    private int maxHostilesPerChunk;
    private boolean cullDuringSweep;
    private boolean protectNamedMobs;
    private boolean protectTamedMobs;
    private boolean protectVillagers;
    private boolean protectBosses;

    private boolean tpsProtectionEnabled;
    private double tpsThreshold;
    private int tpsCooldownSeconds;

    private String soundWarning;
    private String soundSweep;
    private String soundEmergency;

    private String prefix;

    public ConfigManager(CleanGround plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.plugin.saveDefaultConfig();
        this.plugin.reloadConfig();
        FileConfiguration config = this.plugin.getConfig();

        this.sweepEnabled = config.getBoolean("sweep.enabled", true);
        this.sweepIntervalSeconds = Math.max(10, config.getInt("sweep.interval-seconds", 300));

        List<Integer> rawWarnings = config.getIntegerList("sweep.warning-times");
        this.warningTimes = new HashSet<>();
        if (rawWarnings != null && !rawWarnings.isEmpty()) {
            this.warningTimes.addAll(rawWarnings);
        } else {
            this.warningTimes.addAll(List.of(60, 30, 10, 3, 2, 1));
        }

        this.removeEmptyVehicles = config.getBoolean("sweep.remove-empty-vehicles", true);
        this.removeProjectiles = config.getBoolean("sweep.remove-projectiles", true);

        this.protectNamedItems = config.getBoolean("item-filter.protect-named", true);
        this.protectLoreItems = config.getBoolean("item-filter.protect-lore", true);
        this.protectEnchantedItems = config.getBoolean("item-filter.protect-enchanted", true);
        this.minimumAgeTicks = Math.max(0, config.getInt("item-filter.minimum-age-ticks", 100));

        List<String> rawWhitelist = config.getStringList("item-filter.whitelist");
        this.itemWhitelist = new HashSet<>();
        if (rawWhitelist != null) {
            for (String mat : rawWhitelist) {
                if (mat != null && !mat.trim().isEmpty()) {
                    this.itemWhitelist.add(mat.trim().toUpperCase(Locale.ROOT));
                }
            }
        }

        this.mobCullingEnabled = config.getBoolean("mob-culling.enabled", true);
        this.maxHostilesPerChunk = Math.max(1, config.getInt("mob-culling.max-hostiles-per-chunk", 25));
        this.cullDuringSweep = config.getBoolean("mob-culling.cull-during-sweep", true);
        this.protectNamedMobs = config.getBoolean("mob-culling.protect-named", true);
        this.protectTamedMobs = config.getBoolean("mob-culling.protect-tamed", true);
        this.protectVillagers = config.getBoolean("mob-culling.protect-villagers", true);
        this.protectBosses = config.getBoolean("mob-culling.protect-bosses", true);

        this.tpsProtectionEnabled = config.getBoolean("tps-protection.enabled", true);
        this.tpsThreshold = config.getDouble("tps-protection.tps-threshold", 17.5);
        this.tpsCooldownSeconds = Math.max(10, config.getInt("tps-protection.cooldown-seconds", 180));

        this.soundWarning = config.getString("sounds.warning", "UI_BUTTON_CLICK");
        this.soundSweep = config.getString("sounds.sweep", "ENTITY_EXPERIENCE_ORB_PICKUP");
        this.soundEmergency = config.getString("sounds.emergency", "BLOCK_NOTE_BLOCK_BASS");

        this.prefix = config.getString("messages.prefix", "<gradient:#00d2ff:#3a7bd5>[CleanGround]</gradient> ");
    }

    public boolean isSweepEnabled() { return sweepEnabled; }
    public int getSweepIntervalSeconds() { return sweepIntervalSeconds; }
    public Set<Integer> getWarningTimes() { return Collections.unmodifiableSet(warningTimes); }
    public boolean isRemoveEmptyVehicles() { return removeEmptyVehicles; }
    public boolean isRemoveProjectiles() { return removeProjectiles; }

    public boolean isProtectNamedItems() { return protectNamedItems; }
    public boolean isProtectLoreItems() { return protectLoreItems; }
    public boolean isProtectEnchantedItems() { return protectEnchantedItems; }
    public int getMinimumAgeTicks() { return minimumAgeTicks; }
    public boolean isWhitelistedMaterial(String materialName) {
        return materialName != null && itemWhitelist.contains(materialName.toUpperCase(Locale.ROOT));
    }

    public boolean isMobCullingEnabled() { return mobCullingEnabled; }
    public int getMaxHostilesPerChunk() { return maxHostilesPerChunk; }
    public boolean isCullDuringSweep() { return cullDuringSweep; }
    public boolean isProtectNamedMobs() { return protectNamedMobs; }
    public boolean isProtectTamedMobs() { return protectTamedMobs; }
    public boolean isProtectVillagers() { return protectVillagers; }
    public boolean isProtectBosses() { return protectBosses; }

    public boolean isTpsProtectionEnabled() { return tpsProtectionEnabled; }
    public double getTpsThreshold() { return tpsThreshold; }
    public int getTpsCooldownSeconds() { return tpsCooldownSeconds; }

    public String getSoundWarning() { return soundWarning; }
    public String getSoundSweep() { return soundSweep; }
    public String getSoundEmergency() { return soundEmergency; }

    public String getPrefix() { return prefix; }

    public String getMessage(String path, String def) {
        return this.plugin.getConfig().getString("messages." + path, def);
    }
}
