package com.vitaldev.teamsplus.artifacts;

public enum ArtifactType {
    HASTE("HASTE"),
    LOOTING("LOOTING"),
    INQUISITIVE("INQUISITIVE");

    private final String name;

    ArtifactType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
