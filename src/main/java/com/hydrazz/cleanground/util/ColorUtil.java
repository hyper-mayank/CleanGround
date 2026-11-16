package com.hydrazz.cleanground.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private ColorUtil() {}

    public static Component parse(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        // Translate legacy hex &#RRGGBB
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, "<#" + hex + ">");
        }
        matcher.appendTail(buffer);
        String processed = buffer.toString();

        // If message contains MiniMessage tags, parse with MiniMessage
        if (processed.contains("<") && processed.contains(">")) {
            try {
                return MINI_MESSAGE.deserialize(processed);
            } catch (Exception ignored) {
                // Fallback to legacy parsing if MiniMessage fails
            }
        }

        // Fallback to classic Bukkit section codes
        String legacyTranslated = ChatColor.translateAlternateColorCodes('&', processed);
        return LegacyComponentSerializer.legacySection().deserialize(legacyTranslated);
    }
}
