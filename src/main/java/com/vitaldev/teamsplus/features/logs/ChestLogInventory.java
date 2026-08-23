package com.vitaldev.teamsplus.features.logs;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import com.vitaldev.vitallibs.items.ItemHandler;
import com.vitaldev.vitallibs.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChestLogInventory {

    private static final int[] INNER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final TeamsPlus plugin;
    private final ConfigHandler logConfig;
    private final Team team;
    private final Player player;
    private final LogType logType;
    private final InventoryBuilder builder;
    private final ConfigHandler langHandler;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private int page = 0;

    public ChestLogInventory(TeamsPlus plugin, Player player, LogType logType) {
        this.plugin = plugin;
        this.logConfig = plugin.getLogManager().getConfig();
        this.langHandler = plugin.getLangFile();
        this.team = Team.getTeam(player);
        this.player = player;
        this.logType = logType;
        this.builder = new InventoryBuilder(logConfig.getInt("logs.menu-logs.size"),
                logConfig.getMessage("logs.menu-logs.title").replace("{TEAM}", team.getTeamName()), true);
    }

    public void openInventory() {
        setupMenu();
        setupNavigation();
        setupLogs();
        builder.open(player);
    }

    public void setupMenu() {
        String fillerPath = "logs.menu-logs.filler";

        ItemStack filler = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(logConfig.getString(fillerPath + ".material"))),
                logConfig.getMessage(fillerPath + ".name"),
                logConfig.getInt(fillerPath + ".amount"),
                logConfig.getStringList(fillerPath + ".lore"),
                logConfig.getBoolean(fillerPath + ".glow"),
                true
        );

        setupNavigation();
        setupLogs();

        builder.fillWithBorderItem(filler);
    }

    public void setupNavigation() {
        List<LogEntry> logs = team.getLogs(logType);
        int maxPages = (int) Math.ceil((double) logs.size() / INNER_SLOTS.length);
        if (maxPages == 0) maxPages = 1;

        String backPath = "logs.menu-logs.back";
        int backSlot = logConfig.getInt(backPath + ".slot");
        ItemStack backBtn = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(logConfig.getString(backPath + ".material"))),
                logConfig.getMessage(backPath + ".name"),
                logConfig.getInt(backPath + ".amount"),
                logConfig.getStringList(backPath + ".lore"),
                logConfig.getBoolean(backPath + ".glow"),
                true
        );

        builder.addItem(backSlot, backBtn, event -> {
            event.setCancelled(true);
            new ChestLogMenuInventory(plugin, player).openInventory();
        });

        if (page > 0) {
            String prevPath = "logs.menu-logs.prev-page";
            int prevSlot = logConfig.getInt(prevPath + ".slot");
            ItemStack prevBtn = ItemHandler.buildItem(
                    Objects.requireNonNull(Material.getMaterial(logConfig.getString(prevPath + ".material"))),
                    logConfig.getMessage(prevPath + ".name"),
                    logConfig.getInt(prevPath + ".amount"),
                    logConfig.getStringList(prevPath + ".lore"),
                    logConfig.getBoolean(prevPath + ".glow"),
                    true
            );

            builder.addItem(prevSlot, prevBtn, event -> {
                event.setCancelled(true);
                page--;
                openInventory();
            });
        }

        if (page < maxPages - 1) {
            String nextPath = "logs.menu-logs.next-page";
            int nextSlot = logConfig.getInt(nextPath + ".slot");
            ItemStack nextBtn = ItemHandler.buildItem(
                    Objects.requireNonNull(Material.getMaterial(logConfig.getString(nextPath + ".material"))),
                    logConfig.getMessage(nextPath + ".name"),
                    logConfig.getInt(nextPath + ".amount"),
                    logConfig.getStringList(nextPath + ".lore"),
                    logConfig.getBoolean(nextPath + ".glow"),
                    true
            );

            builder.addItem(nextSlot, nextBtn, event -> {
                event.setCancelled(true);
                page++;
                openInventory();
            });
        }
    }

    public void setupLogs() {
        LogDefinition def = plugin.getLogManager().getDefinition(logType);
        if (def == null) return;

        List<LogEntry> logs = team.getLogs(logType);
        int startIndex = page * INNER_SLOTS.length;

        for (int i = 0; i < INNER_SLOTS.length; i++) {
            int logIndex = startIndex + i;
            if (logIndex >= logs.size()) break;

            LogEntry entry = logs.get(logIndex);

            Date date = new Date(entry.getTimestamp());
            String dateStr = dateFormat.format(date);
            String timeStr = timeFormat.format(date);

            String playerName = "Unknown";
            if (entry.getPlayerUUID() != null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getPlayerUUID());
                if (offlinePlayer.getName() != null) {
                    playerName = offlinePlayer.getName();
                }
            }

            List<String> formattedLore = new ArrayList<>();
            for (String line : def.getEntryLore()) {
                String formattedLine = line
                        .replace("{PLAYER}", playerName)
                        .replace("{DATE}", dateStr)
                        .replace("{TIME}", timeStr)
                        .replace("{LOCATION}", entry.getLocationStr());

                if (entry.getMetadata() != null) {
                    for (Map.Entry<String, String> meta : entry.getMetadata().entrySet()) {
                        formattedLine = formattedLine.replace("{" + meta.getKey().toUpperCase() + "}", meta.getValue());
                    }
                }

                formattedLore.add(ChatUtil.color(formattedLine));
            }

            ItemStack logItem = ItemHandler.buildItem(
                    def.getEntryMaterial(),
                    def.getEntryName(),
                    1,
                    formattedLore,
                    def.isEntryGlow(),
                    true
            );

            builder.addItem(INNER_SLOTS[i], logItem, event -> event.setCancelled(true));
        }
    }
}