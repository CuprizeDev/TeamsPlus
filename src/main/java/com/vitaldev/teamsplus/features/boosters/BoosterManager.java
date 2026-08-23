package com.vitaldev.teamsplus.features.boosters;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.items.NBTHandler;
import com.vitaldev.vitallibs.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoosterManager {

    private final TeamsPlus plugin;
    private ConfigHandler boosterConfig;
    private final NBTHandler nbtHandler;

    private final Map<String, BoosterDefinition> definitions = new HashMap<>();

    public BoosterManager(TeamsPlus plugin) {
        this.plugin = plugin;
        this.boosterConfig = new ConfigHandler(plugin, new FileUtil().getYmlFile(plugin, "boosters.yml"));
        this.nbtHandler = new NBTHandler(plugin);
        
        loadDefinitions();
        startBoosterTask();
    }

    public void loadDefinitions() {
        definitions.clear();

        ConfigurationSection root = boosterConfig.getConfigurationSection("boosters.types");
        if (root == null) return;

        for (String key : root.getKeys(false)) {
            try {
                BoosterType type = BoosterType.valueOf(root.getString(key + ".type").toUpperCase());
                BoosterTier tier = BoosterTier.valueOf(root.getString(key + ".tier").toUpperCase());
                double multiplier = root.getDouble(key + ".multiplier");
                long durationSeconds = root.getLong(key + ".duration");
                Material material = Material.valueOf(root.getString(key + ".item.material").toUpperCase());
                String displayName = root.getString(key + ".item.name").replace("{TIER-COLOR}", tier.getColor());
                boolean glow = root.getBoolean(key + ".item.glow");

                BoosterDefinition definition = new BoosterDefinition(
                        key, type, tier, multiplier, durationSeconds,
                        material, displayName, root.getStringList(key + ".item.lore"), glow
                );
                definitions.put(key, definition);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to load booster definition: " + key + " - " + ex.getMessage());
            }
        }
    }

    public void reload() {
        this.boosterConfig = new ConfigHandler(plugin, new FileUtil().getYmlFile(plugin, "boosters.yml"));
        loadDefinitions();
    }

    public BoosterDefinition getBooster(String id) {
        return definitions.get(id);
    }

    public boolean isRegistered(String id) {
        return definitions.containsKey(id);
    }

    public String getBoosterIdFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        if (!nbtHandler.getBoolean(item, nbtHandler.getKey() + "booster")) return null;
        
        return nbtHandler.getString(item, nbtHandler.getKey() + "booster_id");
    }
    
    public ConfigHandler getConfig() {
        return boosterConfig;
    }

    private void startBoosterTask() {
        // Runs every 1 second (20 ticks) to check for expired boosters
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID teamUUID : Team.getTeamList()) {
                Team team = Team.getTeam(teamUUID);
                if (team == null) continue;
                
                if (team.getActiveBoosters().isEmpty()) continue;

                // Create a list to avoid ConcurrentModificationException if removing
                for (ActiveBooster activeBooster : new java.util.ArrayList<>(team.getActiveBoosters().values())) {
                    if (activeBooster.isExpired()) {
                        BoosterType type = activeBooster.getType();
                        team.removeActiveBooster(type);
                        
                        BoosterDefinition def = getBooster(activeBooster.getBoosterId());
                        String name = def != null ? def.getDisplayName() : type.getDisplayName();
                        
                        team.sendMessage(plugin.getLangFile().getMessage("messages.boosters.booster-expired")
                                .replace("{BOOSTER}", name));
                    }
                }
            }
        }, 20L, 20L);
    }
}
