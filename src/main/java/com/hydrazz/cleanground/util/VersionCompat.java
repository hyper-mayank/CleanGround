package com.hydrazz.cleanground.util;

import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;

public final class VersionCompat {

    private static Method getTPSMethod;

    static {
        try {
            getTPSMethod = Bukkit.getServer().getClass().getMethod("getTPS");
        } catch (Exception ignored) {
            getTPSMethod = null;
        }
    }

    private VersionCompat() {}

    public static double getCurrentTPS() {
        if (getTPSMethod != null) {
            try {
                double[] tps = (double[]) getTPSMethod.invoke(Bukkit.getServer());
                if (tps != null && tps.length > 0) {
                    return Math.min(20.0, Math.round(tps[0] * 100.0) / 100.0);
                }
            } catch (Exception ignored) {}
        }
        return 20.0;
    }

    public static boolean isHostileMonster(Entity entity) {
        if (entity instanceof Monster) {
            return true;
        }
        String typeName = entity.getType().name();
        return typeName.equals("SLIME") || typeName.equals("MAGMA_CUBE") ||
               typeName.equals("PHANTOM") || typeName.equals("GHAST") ||
               typeName.equals("SHULKER") || typeName.equals("HOGLIN") ||
               typeName.equals("PIGLIN_BRUTE");
    }

    public static boolean isBoss(Entity entity) {
        String typeName = entity.getType().name();
        return typeName.equals("WITHER") ||
               typeName.equals("ENDER_DRAGON") ||
               typeName.equals("ELDER_GUARDIAN") ||
               typeName.equals("WARDEN");
    }

    public static boolean isTamed(Entity entity) {
        if (entity instanceof Tameable) {
            try {
                return ((Tameable) entity).isTamed();
            } catch (Exception ignored) {}
        }
        return false;
    }

    public static boolean isVillager(Entity entity) {
        return entity instanceof Villager || entity instanceof WanderingTrader;
    }

    public static boolean isEmptyVehicle(Entity entity) {
        if (entity instanceof Vehicle) {
            return entity.getPassengers().isEmpty();
        }
        return false;
    }

    public static boolean isGroundProjectile(Entity entity) {
        if (entity instanceof Projectile) {
            String name = entity.getType().name();
            if (name.contains("ARROW") || name.contains("TRIDENT")) {
                try {
                    Method inGroundMethod = entity.getClass().getMethod("isInGround");
                    return (boolean) inGroundMethod.invoke(entity);
                } catch (Exception ignored) {
                    return true;
                }
            }
        }
        return false;
    }
}
