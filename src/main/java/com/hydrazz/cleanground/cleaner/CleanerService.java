package com.hydrazz.cleanground.cleaner;

import com.hydrazz.cleanground.CleanGround;
import com.hydrazz.cleanground.config.ConfigManager;
import com.hydrazz.cleanground.util.ColorUtil;
import com.hydrazz.cleanground.util.SchedulerAdapter;
import com.hydrazz.cleanground.util.SoundUtil;
import com.hydrazz.cleanground.util.VersionCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CleanerService {

    private final CleanGround plugin;
    private SchedulerAdapter.TaskHandle tickerHandle;
    private int remainingSeconds;
    private long lastEmergencySweepTime = 0L;
    private CleanResult lastResult = new CleanResult();

    public CleanerService(CleanGround plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        this.remainingSeconds = this.plugin.getConfigManager().getSweepIntervalSeconds();
        // Run ticker every 20 ticks (1 second)
        this.tickerHandle = SchedulerAdapter.runTimer(this.plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (this.tickerHandle != null) {
            this.tickerHandle.cancel();
            this.tickerHandle = null;
        }
    }

    public int getRemainingSeconds() {
        return Math.max(0, this.remainingSeconds);
    }

    public CleanResult getLastResult() {
        return this.lastResult;
    }

    private void tick() {
        ConfigManager config = this.plugin.getConfigManager();
        if (!config.isSweepEnabled()) {
            return;
        }

        this.remainingSeconds--;

        // Check warning broadcasts
        if (config.getWarningTimes().contains(this.remainingSeconds)) {
            broadcastWarning(this.remainingSeconds);
        }

        // Trigger scheduled sweep
        if (this.remainingSeconds <= 0) {
            performSweep(false, null);
            this.remainingSeconds = config.getSweepIntervalSeconds();
        }

        // Check emergency TPS protection every 10 seconds
        if (this.remainingSeconds % 10 == 0 && config.isTpsProtectionEnabled()) {
            checkEmergencyTPS();
        }
    }

    private void broadcastWarning(int seconds) {
        ConfigManager config = this.plugin.getConfigManager();
        String path = (seconds <= 3) ? "warning-final" : "warning";
        String def = "<yellow>⚠ Ground items will be swept in <gold><bold>%time%s</bold></gold>!</yellow>";
        String raw = config.getMessage(path, def).replace("%time%", String.valueOf(seconds));

        Bukkit.broadcast(ColorUtil.parse(config.getPrefix() + raw));
        for (Player player : Bukkit.getOnlinePlayers()) {
            SoundUtil.playSound(player, config.getSoundWarning(), 0.7f, (seconds <= 3) ? 1.5f : 1.0f);
        }
    }

    private void checkEmergencyTPS() {
        ConfigManager config = this.plugin.getConfigManager();
        double tps = VersionCompat.getCurrentTPS();
        long now = System.currentTimeMillis();

        if (tps < config.getTpsThreshold() && (now - this.lastEmergencySweepTime >= (long) config.getTpsCooldownSeconds() * 1000L)) {
            this.lastEmergencySweepTime = now;
            String msg = config.getMessage("emergency-triggered", "<red>⚠ Low TPS detected (<white>%tps%</white>)! Emergency cleanup executed.</red>")
                    .replace("%tps%", String.format("%.2f", tps));
            Bukkit.broadcast(ColorUtil.parse(config.getPrefix() + msg));
            for (Player player : Bukkit.getOnlinePlayers()) {
                SoundUtil.playSound(player, config.getSoundEmergency(), 1.0f, 0.8f);
            }
            performSweep(true, null);
        }
    }

    public CleanResult performSweep(boolean manual, CommandSender initiator) {
        long startTime = System.currentTimeMillis();
        ConfigManager config = this.plugin.getConfigManager();
        CleanResult result = new CleanResult();

        if (SchedulerAdapter.isFolia()) {
            // Folia Multi-Threaded Chunk Sweep
            for (World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    SchedulerAdapter.executeOnRegion(this.plugin, world, chunk.getX(), chunk.getZ(), () -> {
                        cleanChunkEntities(chunk, config, result);
                    });
                }
            }
        } else {
            // Paper/Spigot Synchronous Sweep
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    processEntity(entity, config, result);
                }
                if (config.isMobCullingEnabled() && config.isCullDuringSweep()) {
                    for (Chunk chunk : world.getLoadedChunks()) {
                        cullChunkMobs(chunk, config, result);
                    }
                }
            }
        }

        result.setDurationMillis(Math.max(1, System.currentTimeMillis() - startTime));
        this.lastResult = result;

        // Post-sweep notification
        SchedulerAdapter.runSync(this.plugin, () -> {
            String msg = config.getMessage("sweep-complete", "<green>✔ Cleared <white><bold>%items%</bold></white> ground items and <white><bold>%mobs%</bold></white> excess mobs.</green> <gray>(%time%ms)</gray>")
                    .replace("%items%", String.valueOf(result.getItemsRemoved()))
                    .replace("%mobs%", String.valueOf(result.getMobsCulled()))
                    .replace("%time%", String.valueOf(result.getDurationMillis()));

            if (manual && initiator != null && !(initiator instanceof org.bukkit.command.ConsoleCommandSender)) {
                initiator.sendMessage(ColorUtil.parse(config.getPrefix() + msg));
            } else {
                Bukkit.broadcast(ColorUtil.parse(config.getPrefix() + msg));
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                SoundUtil.playSound(player, config.getSoundSweep(), 0.8f, 1.2f);
            }
        });

        return result;
    }

    private void cleanChunkEntities(Chunk chunk, ConfigManager config, CleanResult result) {
        if (!chunk.isLoaded()) return;
        for (Entity entity : chunk.getEntities()) {
            processEntity(entity, config, result);
        }
        if (config.isMobCullingEnabled() && config.isCullDuringSweep()) {
            cullChunkMobs(chunk, config, result);
        }
    }

    private void processEntity(Entity entity, ConfigManager config, CleanResult result) {
        if (!entity.isValid() || entity.isDead()) return;

        // 1. Dropped Items
        if (entity instanceof Item) {
            Item itemEntity = (Item) entity;
            if (shouldKeepItem(itemEntity, config)) {
                return;
            }
            itemEntity.remove();
            result.addItems(1);
            return;
        }

        // 2. Empty Vehicles (Boats / Minecarts)
        if (config.isRemoveEmptyVehicles() && VersionCompat.isEmptyVehicle(entity)) {
            entity.remove();
            result.addVehicles(1);
            return;
        }

        // 3. Ground Projectiles (Arrows / Tridents)
        if (config.isRemoveProjectiles() && VersionCompat.isGroundProjectile(entity)) {
            entity.remove();
            result.addItems(1);
        }
    }

    private boolean shouldKeepItem(Item itemEntity, ConfigManager config) {
        if (itemEntity.getTicksLived() < config.getMinimumAgeTicks()) {
            return true;
        }

        ItemStack stack = itemEntity.getItemStack();
        if (stack.getType().isAir()) {
            return false;
        }

        String matName = stack.getType().name();
        if (config.isWhitelistedMaterial(matName)) {
            return true;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return false;
        }

        if (config.isProtectNamedItems() && meta.hasDisplayName()) {
            return true;
        }

        if (config.isProtectLoreItems() && meta.hasLore()) {
            return true;
        }

        if (config.isProtectEnchantedItems() && meta.hasEnchants()) {
            return true;
        }

        return false;
    }

    private void cullChunkMobs(Chunk chunk, ConfigManager config, CleanResult result) {
        List<Entity> hostiles = new ArrayList<>();
        for (Entity e : chunk.getEntities()) {
            if (!e.isValid() || e.isDead()) continue;
            if (!VersionCompat.isHostileMonster(e)) continue;

            if (config.isProtectNamedMobs() && e.customName() != null) continue;
            if (config.isProtectTamedMobs() && VersionCompat.isTamed(e)) continue;
            if (config.isProtectVillagers() && VersionCompat.isVillager(e)) continue;
            if (config.isProtectBosses() && VersionCompat.isBoss(e)) continue;

            hostiles.add(e);
        }

        int max = config.getMaxHostilesPerChunk();
        if (hostiles.size() > max) {
            int toRemove = hostiles.size() - max;
            for (int i = 0; i < toRemove; i++) {
                hostiles.get(i).remove();
                result.addMobs(1);
            }
        }
    }

    public int countTotalGroundItems() {
        AtomicInteger total = new AtomicInteger(0);
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e instanceof Item && e.isValid()) {
                    total.incrementAndGet();
                }
            }
        }
        return total.get();
    }

    public int countTotalEntities() {
        AtomicInteger total = new AtomicInteger(0);
        for (World world : Bukkit.getWorlds()) {
            total.addAndGet(world.getEntities().size());
        }
        return total.get();
    }

    public int countLoadedChunks() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += world.getLoadedChunks().length;
        }
        return total;
    }
}
