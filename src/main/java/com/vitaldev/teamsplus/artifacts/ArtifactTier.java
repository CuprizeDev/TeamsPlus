package com.vitaldev.teamsplus.artifacts;

public enum ArtifactTier {
    COMMON("Common"),
    RARE("Rare"),
    LEGENDARY("Legendary");

    private final String displayName;

    ArtifactTier(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
