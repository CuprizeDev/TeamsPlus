package com.vitaldev.teamsplus.features.stats;

public enum StatType {
    CROPS_BROKEN,
    KILLS,
    DEATHS,
    PLAYTIME,
    BLOCKS_PLACED,
    FISH_CAUGHT,
    CACTUS_PLACED,
    BLOCKS_MINED;

    public static StatType fromString(String name) {
        try {
            return valueOf(name.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
