package com.vitaldev.teamsplus.teams;

public enum UpgradeType {
    DURABILITY("DURABILITY"),
    ARTIFACTS("ARTIFACTS"),
    EXP("EXP");

    private final String displayName;

    UpgradeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
