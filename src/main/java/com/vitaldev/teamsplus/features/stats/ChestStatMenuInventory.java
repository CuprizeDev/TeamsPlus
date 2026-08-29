package com.vitaldev.teamsplus.features.stats;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.PlayerData;
import com.vitaldev.teamsplus.model.TeamPlayer;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import com.vitaldev.vitallibs.items.ItemHandler;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ChestStatMenuInventory {

    private final TeamsPlus plugin;
    private final Player viewer;
    private final OfflinePlayer target;
    private final UUID targetUUID;
    private final ConfigHandler statConfig;
    private final InventoryBuilder builder;

    public ChestStatMenuInventory(TeamsPlus plugin, Player viewer, OfflinePlayer target) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.target = target;
        this.targetUUID = target.getUniqueId();
        this.statConfig = plugin.getStatsFile();
        
        this.builder = new InventoryBuilder(54,
                statConfig.getMessage("stats.menu.title")
                        .replace("{PLAYER}", target.getName() != null ? target.getName() : "Unknown"), true);
    }

    public void openInventory() {
        setupMenu();
        setupItems();
        builder.open(viewer);
    }

    public void setupMenu() {
        String fillerPath = "stats.menu.filler";
        String closePath = "stats.menu.close";
        String backPath = "stats.menu.back";

        ItemStack filler = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(statConfig.getString(fillerPath + ".material"))),
                statConfig.getString(fillerPath + ".name"),
                statConfig.getInt(fillerPath + ".amount"),
                statConfig.getStringList(fillerPath + ".lore"),
                statConfig.getBoolean(fillerPath + ".glow"),
                true
        );

        if (statConfig.contains(closePath)) {
            ItemStack closeButton = ItemHandler.buildItem(
                    Objects.requireNonNull(Material.getMaterial(statConfig.getString(closePath + ".material"))),
                    statConfig.getString(closePath + ".name"),
                    statConfig.getInt(closePath + ".amount"),
                    statConfig.getStringList(closePath + ".lore"),
                    statConfig.getBoolean(closePath + ".glow"),
                    true
            );

            builder.setCloseButton(closeButton, event -> {
                event.setCancelled(true);
                if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) {
                    viewer.closeInventory();
                }
            });
        }

        if (statConfig.contains(backPath)) {
            ItemStack backButton = ItemHandler.buildItem(
                    Objects.requireNonNull(Material.getMaterial(statConfig.getString(backPath + ".material"))),
                    statConfig.getMessage(backPath + ".name"),
                    statConfig.getInt(backPath + ".amount"),
                    statConfig.getColoredList(backPath + ".lore"),
                    statConfig.getBoolean(backPath + ".glow"),
                    true
            );

            builder.setBackButton(backButton, event -> {
                event.setCancelled(true);
                new ChestTeamStatsInventory(plugin, viewer, com.vitaldev.teamsplus.model.Team.getTeamByPlayerUUID(target.getUniqueId())).openInventory();
            });
        }

        builder.fillWithBorderItem(filler);
    }

    public void setupItems() {
        // Get target's stats
        Map<StatType, Long> stats;
        if (target.isOnline()) {
            stats = TeamPlayer.get(target.getPlayer()).getStats();
        } else {
            stats = PlayerData.getOfflineStats(targetUUID);
        }

        // Populate items
        for (StatType type : StatType.values()) {
            StatDefinition def = plugin.getStatManager().get(type);
            if (def == null) continue;

            long value = stats.getOrDefault(type, 0L);
            String valueStr = type == StatType.PLAYTIME 
                    ? formatTimeShort(value * 1000L) 
                    : String.valueOf(value);
            
            ItemStack statItem = StatItemBuilder.buildStatItem(def, valueStr);
            
            builder.addItem(def.getSlot(), statItem, event -> {
                event.setCancelled(true);
            });
        }
    }

    private String formatTimeShort(long millis) {
        if (millis <= 0) return "0s";

        java.time.Duration duration = java.time.Duration.ofMillis(millis);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append("s");
        }

        return sb.toString().trim();
    }
}
