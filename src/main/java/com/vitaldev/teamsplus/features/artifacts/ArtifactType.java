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
    INQUISITIVE;

    public static ArtifactType fromString(String value) {
        try {
            return ArtifactType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
