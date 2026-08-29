package com.vitaldev.teamsplus.features.artifacts;

public enum ArtifactType {
    ADRENALINE,
    AERIAL,
    ARACHNID,
    BEACON,
    BEAST_BANE,
    BEAST_FORGE,
    BLAST_SHIELD,
    BLOODHOUND,
    BLOOM_STONE,
    GRAVITY,
    HASTE,
    INQUISITIVE,
    RADAR,
    REPULSOR,
    SMELTER,
    TOXIC_AURA,
    TRUE_SIGHT,
    VAMPIRIC,
    VERDANT,
    VITALITY,
    WAR_HORN;

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
