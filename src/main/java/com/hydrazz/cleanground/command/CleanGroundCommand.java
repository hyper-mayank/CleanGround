package com.hydrazz.cleanground.command;

import com.hydrazz.cleanground.CleanGround;
import com.hydrazz.cleanground.cleaner.CleanerService;
import com.hydrazz.cleanground.config.ConfigManager;
import com.hydrazz.cleanground.util.ColorUtil;
import com.hydrazz.cleanground.util.VersionCompat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

public class CleanGroundCommand implements CommandExecutor, TabCompleter {

    private final CleanGround plugin;
    private final CleanerService cleanerService;

    public CleanGroundCommand(CleanGround plugin) {
        this.plugin = plugin;
        this.cleanerService = plugin.getCleanerService();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ConfigManager config = this.plugin.getConfigManager();

        if (!sender.hasPermission("cleanground.admin")) {
            sender.sendMessage(ColorUtil.parse(config.getPrefix() + config.getMessage("no-permission", "<red>You lack permission to use this command.</red>")));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("sweep") || sub.equals("clean") || sub.equals("clear")) {
            sender.sendMessage(ColorUtil.parse(config.getPrefix() + "<yellow>Initiating ground item and entity sweep...</yellow>"));
            this.cleanerService.performSweep(true, sender);
            return true;
        }

        if (sub.equals("count") || sub.equals("stats") || sub.equals("status")) {
            int items = this.cleanerService.countTotalGroundItems();
            int entities = this.cleanerService.countTotalEntities();
            int chunks = this.cleanerService.countLoadedChunks();
            double tps = VersionCompat.getCurrentTPS();
            int remaining = this.cleanerService.getRemainingSeconds();

            String statsMsg = config.getMessage("count", "<gray>World stats: <white><bold>%items%</bold></white> items, <white><bold>%entities%</bold></white> entities in <white><bold>%chunks%</bold></white> loaded chunks.</gray>")
                    .replace("%items%", String.valueOf(items))
                    .replace("%entities%", String.valueOf(entities))
                    .replace("%chunks%", String.valueOf(chunks));

            sender.sendMessage(ColorUtil.parse(config.getPrefix() + statsMsg));
            sender.sendMessage(ColorUtil.parse(config.getPrefix() + "<gray>Current TPS: <green>" + String.format("%.2f", tps) + "</green> | Next sweep in: <gold>" + remaining + "s</gold></gray>"));
            return true;
        }

        if (sub.equals("cullmobs") || sub.equals("killmobs")) {
            sender.sendMessage(ColorUtil.parse(config.getPrefix() + "<yellow>Culling excess hostile mobs across chunks...</yellow>"));
            this.cleanerService.performSweep(true, sender);
            return true;
        }

        if (sub.equals("reload")) {
            this.plugin.reloadPlugin();
            sender.sendMessage(ColorUtil.parse(config.getPrefix() + config.getMessage("reloaded", "<green>CleanGround Pro configuration reloaded successfully.</green>")));
            return true;
        }

        sendHelp(sender, label);
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(ColorUtil.parse("<gradient:#00d2ff:#3a7bd5><bold>CleanGround Pro</bold></gradient> <gray>v" + this.plugin.getDescription().getVersion() + " Commands:</gray>"));
        sender.sendMessage(ColorUtil.parse(" <gradient:#00d2ff:#3a7bd5>•</gradient> <yellow>/" + label + " sweep</yellow> <gray>- Force instant ground item & entity cleanup</gray>"));
        sender.sendMessage(ColorUtil.parse(" <gradient:#00d2ff:#3a7bd5>•</gradient> <yellow>/" + label + " count</yellow> <gray>- Show total dropped items, entities & chunks</gray>"));
        sender.sendMessage(ColorUtil.parse(" <gradient:#00d2ff:#3a7bd5>•</gradient> <yellow>/" + label + " cullmobs</yellow> <gray>- Cull excess hostile mobs over chunk limits</gray>"));
        sender.sendMessage(ColorUtil.parse(" <gradient:#00d2ff:#3a7bd5>•</gradient> <yellow>/" + label + " reload</yellow> <gray>- Hot-reload configuration files & timers</gray>"));
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("cleanground.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subs = Arrays.asList("sweep", "count", "cullmobs", "reload", "help");
            List<String> matched = new ArrayList<>();
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (String s : subs) {
                if (s.startsWith(prefix)) matched.add(s);
            }
            return matched;
        }

        return Collections.emptyList();
    }
}
