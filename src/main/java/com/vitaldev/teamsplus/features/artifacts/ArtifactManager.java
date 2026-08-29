package com.vitaldev.teamsplus.features.artifacts;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.listeners.BeaconListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.BeastBaneListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.BeastForgeListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.BloomStoneListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.HasteListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.InquisitiveListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.SmelterListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.VerdantListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.AerialListener;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.items.NBTHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import com.vitaldev.teamsplus.features.artifacts.listeners.VampiricListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.ArachnidListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.AdrenalineListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.BlastShieldListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.GravityListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.VitalityListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.WarHornListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.BloodhoundListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.RadarListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.TrueSightListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.ToxicAuraListener;
import com.vitaldev.teamsplus.features.artifacts.listeners.RepulsorListener;

public class ArtifactManager {

    private final TeamsPlus plugin;
    private ConfigHandler artifactHandler;
    private final NBTHandler nbtHandler;

    private final Map<ArtifactType, ArtifactDefinition> definitions =
            new EnumMap<>(ArtifactType.class);
    private boolean listenersRegistered = false;

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
        registerListeners();
    }

    public void reload() {
        this.artifactHandler = plugin.getArtifacts();
        loadDefinitions();
    }

    private void registerListeners() {
        if (listenersRegistered) {
            return;
        }

        var pluginManager = Bukkit.getServer().getPluginManager();
        pluginManager.registerEvents(new AerialListener(this.plugin), plugin);
        pluginManager.registerEvents(new SmelterListener(this.plugin), plugin);
        pluginManager.registerEvents(new VerdantListener(this.plugin), plugin);
        pluginManager.registerEvents(new BloomStoneListener(this.plugin), plugin);
        pluginManager.registerEvents(new BeaconListener(this.plugin), plugin);
        pluginManager.registerEvents(new HasteListener(this.plugin), plugin);
        pluginManager.registerEvents(new BeastBaneListener(this.plugin), plugin);
        pluginManager.registerEvents(new BeastForgeListener(this.plugin), plugin);
        pluginManager.registerEvents(new InquisitiveListener(this.plugin), plugin);
                        pluginManager.registerEvents(new VampiricListener(this.plugin), plugin);
        pluginManager.registerEvents(new ArachnidListener(this.plugin), plugin);
        pluginManager.registerEvents(new AdrenalineListener(this.plugin), plugin);
        pluginManager.registerEvents(new BlastShieldListener(this.plugin), plugin);
        pluginManager.registerEvents(new GravityListener(this.plugin), plugin);
        pluginManager.registerEvents(new VitalityListener(this.plugin), plugin);
        pluginManager.registerEvents(new WarHornListener(this.plugin), plugin);
        pluginManager.registerEvents(new BloodhoundListener(this.plugin), plugin);
        pluginManager.registerEvents(new RadarListener(this.plugin), plugin);
        pluginManager.registerEvents(new TrueSightListener(this.plugin), plugin);
        pluginManager.registerEvents(new ToxicAuraListener(this.plugin), plugin);
        pluginManager.registerEvents(new RepulsorListener(this.plugin), plugin);

        listenersRegistered = true;
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
            String rawType = nbtHandler.getString(item, nbtHandler.getKey() + "artifact_type");
            if (rawType == null) {
                return null;
            }

            ArtifactType type = ArtifactType.valueOf(rawType.toUpperCase().replace("-", "_"));
            return isRegistered(type) ? type : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean isArtifact(ItemStack itemStack) {
        return nbtHandler.getBoolean(itemStack, nbtHandler.getKey() + "artifact");
    }
}
