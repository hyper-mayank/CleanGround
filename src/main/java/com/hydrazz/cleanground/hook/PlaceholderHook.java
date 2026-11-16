package com.hydrazz.cleanground.hook;

import com.hydrazz.cleanground.CleanGround;
import com.hydrazz.cleanground.cleaner.CleanResult;
import com.hydrazz.cleanground.cleaner.CleanerService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PlaceholderExpansion {

    private final CleanGround plugin;
    private final CleanerService cleanerService;

    public PlaceholderHook(CleanGround plugin) {
        this.plugin = plugin;
        this.cleanerService = plugin.getCleanerService();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cleanground";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Hydrazz";
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        int seconds = this.cleanerService.getRemainingSeconds();
        CleanResult last = this.cleanerService.getLastResult();

        switch (params.toLowerCase()) {
            case "time_left":
            case "seconds_left":
                return String.valueOf(seconds);

            case "time_formatted": {
                int m = seconds / 60;
                int s = seconds % 60;
                return String.format("%02d:%02d", m, s);
            }

            case "last_removed":
            case "last_total":
                return String.valueOf(last.getTotalRemoved());

            case "last_items":
                return String.valueOf(last.getItemsRemoved());

            case "last_mobs":
                return String.valueOf(last.getMobsCulled());

            case "ground_items":
                return String.valueOf(this.cleanerService.countTotalGroundItems());

            case "status":
                return this.plugin.getConfigManager().isSweepEnabled() ? "Active" : "Disabled";

            default:
                return null;
        }
    }
}
