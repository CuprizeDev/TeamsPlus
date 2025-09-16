package com.vitaldev.teamsplus.logs;

public enum LogType {
    JOIN("Player Joined"),
    LEAVE("Player Left"),
    KICK("Player Kicked"),
    INVITE_ADD("Invite Sent"),
    INVITE_REMOVE("Invite Revoked"),
    ALLY_REMOVE("Ally Removed"),
    ALLY_ADD("Ally Added"),
    RENAME("Team Renamed"),
    CLAIM_ADD("Claim Created"),
    CLAIM_REMOVE("Claim Deleted"),
    SHIELD_ACTIVATE("Shield Activated"),
    ARTIFACT_ADD("Artifact Added"),
    ARTIFACT_REMOVE("Artifact Removed"),
    UPGRADE_PURCHASE("Upgrade Purchased"),
    HOME_SET("Home Set");

    private final String displayName;

    LogType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
