package com.vitaldev.teamsplus.features.artifacts;

import com.vitaldev.teamsplus.TeamsPlus;

public enum ArtifactTier {
    COMMON,
    RARE,
    LEGENDARY,
    MYTHIC;

    private final String displayName;
    private final String color;
    private final int power;

    ArtifactTier() {
        TeamsPlus plugin = TeamsPlus.getPlugin(TeamsPlus.class);
        this.displayName = plugin.getArtifacts().getMessage("artifacts.tiers." + name() + ".displayName");
        this.color = plugin.getArtifacts().getMessage("artifacts.tiers." + name() + ".color");
        this.power = plugin.getArtifacts().getInt("artifacts.tiers." + name() + ".power");
    }

    public int getPower() {
        return power;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }
}
