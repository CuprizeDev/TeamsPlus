package com.vitaldev.teamsplus.features.boosters;

public enum BoosterType {
    EXP("Exp"),
    CROP_GROWTH("Crop Growth"),
    SPAWNER_RATE("Spawner Rate");

    private final String displayName;

    BoosterType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
