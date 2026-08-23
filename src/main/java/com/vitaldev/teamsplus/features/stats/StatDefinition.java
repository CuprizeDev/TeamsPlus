package com.vitaldev.teamsplus.features.stats;

import com.vitaldev.vitallibs.util.ChatUtil;
import org.bukkit.Material;

import java.util.List;

public class StatDefinition {
    private final StatType type;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final boolean glow;
    private final int slot;

    public StatDefinition(StatType type, Material material, String displayName, List<String> lore, boolean glow, int slot) {
        this.type = type;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.glow = glow;
        this.slot = slot;
    }

    public StatType getType() { return type; }
    public Material getMaterial() { return material; }
    public String getDisplayName() { return ChatUtil.color(displayName); }
    public List<String> getLore() { return lore; }
    public boolean isGlow() { return glow; }
    public int getSlot() { return slot; }
}
