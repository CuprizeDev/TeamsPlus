package com.vitaldev.teamsplus.artifacts;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.vitallibs.config.ConfigHandler;

import java.util.*;

public class Artifact {

    private final UUID uuid;
    private ArtifactTier artifactTier;
    private ArtifactType artifactType;
    public TeamsPlus plugin;
    public final ConfigHandler configHandler;


    public Artifact(TeamsPlus plugin, UUID uuid, ArtifactType artifactType, ArtifactTier artifactTier) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.configHandler = plugin.getConfigFile();
        this.artifactTier = artifactTier;
        this.artifactType = artifactType;
    }

    public ArtifactTier getArtifactTier() {
        return artifactTier;
    }

    public ArtifactType getArtifactType() {
        return artifactType;
    }

    public UUID getUuid() {
        return uuid;
    }
}
