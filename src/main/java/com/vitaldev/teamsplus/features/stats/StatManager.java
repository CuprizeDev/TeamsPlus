package com.vitaldev.teamsplus.features.stats;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.vitallibs.config.ConfigHandler;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.Map;

public class StatManager {
    private final TeamsPlus plugin;
    private ConfigHandler statsHandler;

    private final Map<StatType, StatDefinition> definitions = new EnumMap<>(StatType.class);

    public StatManager(TeamsPlus plugin) {
        this.plugin = plugin;
        this.statsHandler = plugin.getStatsFile();
    }

    public void loadDefinitions() {
        definitions.clear();

        ConfigurationSection root = statsHandler.getConfigurationSection("stats.types");
        if (root == null) return;

        for (String key : root.getKeys(false)) {
            StatType type = StatType.fromString(key);
            if (type == null) {
                plugin.getLogger().warning("Unknown stat type in config: " + key);
                continue;
            }

            int slot = root.getInt(key + ".slot");
            Material material = Material.valueOf(root.getString(key + ".item.material", "STONE"));
            String name = root.getString(key + ".item.name", "&fUnknown");
            java.util.List<String> lore = root.getStringList(key + ".item.lore");
            boolean glow = root.getBoolean(key + ".item.glow", false);

            StatDefinition def = new StatDefinition(type, material, name, lore, glow, slot);
            definitions.put(type, def);
        }
    }

    public void reload() {
        this.statsHandler = plugin.getStatsFile();
        loadDefinitions();
    }

    public StatDefinition get(StatType type) {
        return definitions.get(type);
    }
    
    public Map<StatType, StatDefinition> getDefinitions() {
        return definitions;
    }
}
