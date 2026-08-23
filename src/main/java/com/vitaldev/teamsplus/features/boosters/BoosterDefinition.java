package com.vitaldev.teamsplus.features.boosters;

import com.vitaldev.vitallibs.util.ChatUtil;
import org.bukkit.Material;

import java.util.List;

public class BoosterDefinition {

    private final String id;
    private final BoosterType type;
    private final BoosterTier tier;
    private final double multiplier;
    private final long durationSeconds;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final boolean glow;

    public BoosterDefinition(String id, BoosterType type, BoosterTier tier, double multiplier, long durationSeconds,
                             Material material, String displayName, List<String> lore, boolean glow) {
        this.id = id;
        this.type = type;
        this.tier = tier;
        this.multiplier = multiplier;
        this.durationSeconds = durationSeconds;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.glow = glow;
    }

    public String getId() { return id; }
    public BoosterType getType() { return type; }
    public BoosterTier getTier() { return tier; }
    public double getMultiplier() { return multiplier; }
    public long getDurationSeconds() { return durationSeconds; }
    public Material getMaterial() { return material; }
    public String getDisplayName() { return ChatUtil.color(displayName); }
    public List<String> getLore() { return lore; }
    public boolean isGlow() { return glow; }
}
