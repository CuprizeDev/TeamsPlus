package com.vitaldev.teamsplus.features.artifacts;

import com.vitaldev.vitallibs.util.ChatUtil;
import org.bukkit.Material;

import java.util.List;

public class ArtifactDefinition {

    private final ArtifactType type;
    private final int power;
    private final ArtifactTier tier;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final boolean glow;

    public ArtifactDefinition(ArtifactType type, ArtifactTier tier, int power,
                              Material material, String displayName,
                              List<String> lore, boolean glow) {
        this.type = type;
        this.tier = tier;
        this.power = power;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.glow = glow;
    }

    public ArtifactType getType() { return type; }
    public ArtifactTier getTier() { return tier; }
    public int getPower() { return power; }
    public Material getMaterial() { return material; }
    public String getDisplayName() { return ChatUtil.color(displayName); }
    public List<String> getLore() { return lore; }
    public boolean isGlow() { return glow; }
}
