package com.vitaldev.teamsplus.features.upgrades;

import com.vitaldev.teamsplus.features.artifacts.ArtifactType;

public enum UpgradeType {
    DURABILITY(),
    ARTIFACTS(),
    EXP();


    public static ArtifactType fromString(String value) {
        try {
            return ArtifactType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
