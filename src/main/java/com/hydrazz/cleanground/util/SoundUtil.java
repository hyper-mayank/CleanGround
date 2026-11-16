package com.hydrazz.cleanground.util;

import java.util.Locale;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SoundUtil {

    private SoundUtil() {}

    public static Sound resolveSound(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String clean = name.trim().toUpperCase(Locale.ROOT).replace(".", "_");
        try {
            return Sound.valueOf(clean);
        } catch (IllegalArgumentException e) {
            // Attempt common version renames
            if (clean.equals("UI_BUTTON_CLICK")) {
                try { return Sound.valueOf("CLICK"); } catch (Exception ignored) {}
            } else if (clean.equals("ENTITY_EXPERIENCE_ORB_PICKUP")) {
                try { return Sound.valueOf("ORB_PICKUP"); } catch (Exception ignored) {}
            } else if (clean.equals("BLOCK_NOTE_BLOCK_BASS")) {
                try { return Sound.valueOf("NOTE_BASS"); } catch (Exception ignored) {}
            } else if (clean.equals("ENTITY_ITEM_BREAK")) {
                try { return Sound.valueOf("ITEM_BREAK"); } catch (Exception ignored) {}
            }
            return null;
        }
    }

    public static void playSound(Player player, String soundName, float volume, float pitch) {
        if (player == null || !player.isOnline()) return;
        Sound sound = resolveSound(soundName);
        if (sound != null) {
            try {
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (Exception ignored) {}
        }
    }
}
