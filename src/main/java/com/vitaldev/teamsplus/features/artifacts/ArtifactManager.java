package com.vitaldev.teamsplus.features.artifacts;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.listeners.AerialListener;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.items.NBTHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArtifactManager {

    private final TeamsPlus plugin;
    private final ConfigHandler artifactHandler;
    private final NBTHandler nbtHandler;

    private final Map<ArtifactType, ArtifactDefinition> definitions =
            new EnumMap<>(ArtifactType.class);

    public ArtifactManager(TeamsPlus plugin) {
        this.plugin = plugin;
        this.artifactHandler = plugin.getArtifacts();
        this.nbtHandler = new NBTHandler(plugin);
    }

    public void loadDefinitions() {

        definitions.clear();

        ConfigurationSection root = artifactHandler.getConfigurationSection("artifacts.types");
        if (root == null) return;

        for (String key : root.getKeys(false)) {

            ArtifactType type;
            try {
                type = ArtifactType.valueOf(key.toUpperCase().replace("-", "_"));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Unknown artifact type in config: " + key);
                continue;
            }

            ArtifactTier tier;
            try {
                tier = ArtifactTier.valueOf(
                        root.getString(key + ".tier").toUpperCase()
                );
            } catch (Exception ex) {
                plugin.getLogger().warning("Invalid tier for artifact: " + key);
                continue;
            }

            int power;
            try {
                power = root.getInt(key + ".power");
            } catch (Exception ex) {
                plugin.getLogger().warning("Invalid power for artifact: " + key);
                continue;
            }

            ArtifactDefinition definition = new ArtifactDefinition(
                    type,
                    tier,
                    power,
                    Material.valueOf(root.getString(key + ".item.material")),
                    root.getString(key + ".item.name").replace("{TIER-COLOR}", tier.getColor()),
                    root.getStringList(key + ".item.lore"),
                    root.getBoolean(key + ".item.glow")
            );
            definitions.put(type, definition);
        }
        Bukkit.getServer().getPluginManager().registerEvents(new AerialListener(this.plugin), plugin);
    }

    public ArtifactDefinition get(ArtifactType type) {
        return definitions.get(type);
    }

    public boolean isRegistered(ArtifactType type) {
        return definitions.containsKey(type);
    }

    public ArtifactType getType(ItemStack item) {
        if (!isArtifact(item)) return null;

        try {
            ArtifactType type = ArtifactType.valueOf(nbtHandler.getString(item, nbtHandler.getKey() + "artifact_type").toUpperCase());
            return isRegistered(type) ? type : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean isArtifact(ItemStack itemStack) {
        return nbtHandler.getBoolean(itemStack, nbtHandler.getKey() + "artifact");
    }
}
