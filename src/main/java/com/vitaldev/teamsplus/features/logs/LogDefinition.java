package com.vitaldev.teamsplus.features.logs;

import com.vitaldev.vitallibs.util.ChatUtil;
import org.bukkit.Material;

import java.util.List;
import java.util.stream.Collectors;

public class LogDefinition {

    private final LogType type;
    
    // Category Icon (for Log Menu)
    private final Material categoryMaterial;
    private final String categoryName;
    private final List<String> categoryLore;
    private final boolean categoryGlow;

    // Entry Template (for individual logs)
    private final Material entryMaterial;
    private final String entryName;
    private final List<String> entryLore;
    private final boolean entryGlow;

    public LogDefinition(LogType type, 
                         Material categoryMaterial, String categoryName, List<String> categoryLore, boolean categoryGlow,
                         Material entryMaterial, String entryName, List<String> entryLore, boolean entryGlow) {
        this.type = type;
        this.categoryMaterial = categoryMaterial;
        this.categoryName = categoryName;
        this.categoryLore = categoryLore;
        this.categoryGlow = categoryGlow;
        this.entryMaterial = entryMaterial;
        this.entryName = entryName;
        this.entryLore = entryLore;
        this.entryGlow = entryGlow;
    }

    public LogType getType() {
        return type;
    }

    public Material getCategoryMaterial() {
        return categoryMaterial;
    }

    public String getCategoryName() {
        return ChatUtil.color(categoryName);
    }

    public List<String> getCategoryLore() {
        return categoryLore.stream().map(ChatUtil::color).collect(Collectors.toList());
    }

    public boolean isCategoryGlow() {
        return categoryGlow;
    }

    public Material getEntryMaterial() {
        return entryMaterial;
    }

    public String getEntryName() {
        return ChatUtil.color(entryName);
    }

    public List<String> getEntryLore() {
        return entryLore;
    }

    public boolean isEntryGlow() {
        return entryGlow;
    }
}
