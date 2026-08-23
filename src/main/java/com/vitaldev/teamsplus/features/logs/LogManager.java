package com.vitaldev.teamsplus.features.logs;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.FileUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class LogManager {

    private final TeamsPlus plugin;
    private ConfigHandler logConfig;
    private final Map<LogType, LogDefinition> definitions = new EnumMap<>(LogType.class);

    public LogManager(TeamsPlus plugin) {
        this.plugin = plugin;
        this.logConfig = new ConfigHandler(plugin, new FileUtil().getYmlFile(plugin, "logs.yml"));
        loadDefinitions();
    }

    public void loadDefinitions() {
        definitions.clear();

        ConfigurationSection root = logConfig.getConfigurationSection("logs.types");
        if (root == null) return;

        for (LogType type : LogType.values()) {
            String key = type.name().toLowerCase();
            
            if (!root.contains(key)) continue;

            try {
                // Category info
                Material categoryMat = Material.valueOf(root.getString(key + ".category.material").toUpperCase());
                String categoryName = root.getString(key + ".category.name");
                java.util.List<String> categoryLore = root.getStringList(key + ".category.lore");
                boolean categoryGlow = root.getBoolean(key + ".category.glow");

                // Entry info
                Material entryMat = Material.valueOf(root.getString(key + ".entry.material").toUpperCase());
                String entryName = root.getString(key + ".entry.name");
                java.util.List<String> entryLore = root.getStringList(key + ".entry.lore");
                boolean entryGlow = root.getBoolean(key + ".entry.glow");

                LogDefinition definition = new LogDefinition(
                        type,
                        categoryMat, categoryName, categoryLore, categoryGlow,
                        entryMat, entryName, entryLore, entryGlow
                );
                
                definitions.put(type, definition);
                
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to load log definition for: " + type.name());
            }
        }
    }

    public void reload() {
        this.logConfig = new ConfigHandler(plugin, new FileUtil().getYmlFile(plugin, "logs.yml"));
        loadDefinitions();
    }

    public LogDefinition getDefinition(LogType type) {
        return definitions.get(type);
    }
    
    public ConfigHandler getConfig() {
        return logConfig;
    }

    // Helper to easily create and add a log to a team.
    public void logEvent(Team team, LogType type, Player player, Location location, Map<String, String> metadata) {
        if (team == null || type == null) return;
        
        UUID playerUUID = player != null ? player.getUniqueId() : null;
        String locationStr = location != null 
                ? String.format("%s, %d, %d, %d", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()) 
                : "Unknown";
                
        LogEntry entry = new LogEntry(
                UUID.randomUUID(),
                type,
                System.currentTimeMillis(),
                playerUUID,
                locationStr,
                metadata
        );
        
        team.addLog(entry);
    }
}
