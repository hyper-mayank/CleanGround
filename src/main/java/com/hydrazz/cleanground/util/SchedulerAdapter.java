package com.hydrazz.cleanground.util;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class SchedulerAdapter {

    private static final boolean IS_FOLIA;
    private static Method getGlobalRegionSchedulerMethod;
    private static Method getRegionSchedulerMethod;
    private static Method getAsyncSchedulerMethod;

    private static Method globalRunAtFixedRateMethod;
    private static Method globalExecuteMethod;
    private static Method regionExecuteMethod;
    private static Method asyncRunNowMethod;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;

            Class<?> bukkitClass = Bukkit.class;
            getGlobalRegionSchedulerMethod = bukkitClass.getMethod("getGlobalRegionScheduler");
            getRegionSchedulerMethod = bukkitClass.getMethod("getRegionScheduler");
            getAsyncSchedulerMethod = bukkitClass.getMethod("getAsyncScheduler");

            Class<?> globalSchedClass = getGlobalRegionSchedulerMethod.getReturnType();
            globalRunAtFixedRateMethod = globalSchedClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
            globalExecuteMethod = globalSchedClass.getMethod("execute", Plugin.class, Runnable.class);

            Class<?> regionSchedClass = getRegionSchedulerMethod.getReturnType();
            regionExecuteMethod = regionSchedClass.getMethod("execute", Plugin.class, World.class, int.class, int.class, Runnable.class);

            Class<?> asyncSchedClass = getAsyncSchedulerMethod.getReturnType();
            asyncRunNowMethod = asyncSchedClass.getMethod("runNow", Plugin.class, Consumer.class);
        } catch (Throwable t) {
            folia = false;
        }
        IS_FOLIA = folia;
    }

    private SchedulerAdapter() {}

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public interface TaskHandle {
        void cancel();
        boolean isCancelled();
    }

    public static TaskHandle runTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (IS_FOLIA) {
            try {
                Object scheduler = getGlobalRegionSchedulerMethod.invoke(null);
                Consumer<Object> consumer = scheduledTask -> task.run();
                Object scheduledTask = globalRunAtFixedRateMethod.invoke(scheduler, plugin, consumer, Math.max(1L, delayTicks), Math.max(1L, periodTicks));

                Method cancelMethod = scheduledTask.getClass().getMethod("cancel");
                Method isCancelledMethod = scheduledTask.getClass().getMethod("isCancelled");

                return new TaskHandle() {
                    @Override
                    public void cancel() {
                        try {
                            cancelMethod.invoke(scheduledTask);
                        } catch (Exception ignored) {}
                    }

                    @Override
                    public boolean isCancelled() {
                        try {
                            return (boolean) isCancelledMethod.invoke(scheduledTask);
                        } catch (Exception e) {
                            return true;
                        }
                    }
                };
            } catch (Throwable t) {
                // Fallback to Bukkit if reflection failed
            }
        }

        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        return new TaskHandle() {
            @Override
            public void cancel() {
                bukkitTask.cancel();
            }

            @Override
            public boolean isCancelled() {
                return bukkitTask.isCancelled();
            }
        };
    }

    public static void runSync(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            try {
                Object scheduler = getGlobalRegionSchedulerMethod.invoke(null);
                globalExecuteMethod.invoke(scheduler, plugin, task);
                return;
            } catch (Throwable ignored) {}
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public static void runAsync(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            try {
                Object asyncSched = getAsyncSchedulerMethod.invoke(null);
                Consumer<Object> consumer = scheduledTask -> task.run();
                asyncRunNowMethod.invoke(asyncSched, plugin, consumer);
                return;
            } catch (Throwable ignored) {}
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static void executeOnRegion(Plugin plugin, World world, int chunkX, int chunkZ, Runnable task) {
        if (IS_FOLIA && world != null) {
            try {
                Object regionSched = getRegionSchedulerMethod.invoke(null);
                regionExecuteMethod.invoke(regionSched, plugin, world, chunkX, chunkZ, task);
                return;
            } catch (Throwable ignored) {}
        }
        // Paper/Spigot fallback: run on standard synchronous scheduler
        Bukkit.getScheduler().runTask(plugin, task);
    }
}
