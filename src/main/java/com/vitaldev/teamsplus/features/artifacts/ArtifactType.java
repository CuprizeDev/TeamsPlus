package com.vitaldev.teamsplus.features.artifacts;

public enum ArtifactType {
    HASTE,
    LOOTING,
    SMELTER,
    VERDANT,
    SENTRY,
    BLOOM_STONE,
    BEAST_BANE,
    BEACON,
    BEAST_FORGE,
    AERIAL,
    INQUISITIVE,
    HARVESTER,
    SANCTUARY,
    WARDEN;

    public static ArtifactType fromString(String value) {
        if (value == null) {
            return null;
        }

        try {
            return ArtifactType.valueOf(value.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
