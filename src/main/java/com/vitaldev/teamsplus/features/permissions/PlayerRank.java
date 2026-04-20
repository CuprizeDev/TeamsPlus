package com.vitaldev.teamsplus.features.permissions;

public enum PlayerRank {
    LEADER("Leader", 3),
    CO_LEADER("Co-Leader", 2),
    OFFICER("Officer", 1),
    MEMBER("Member", 0);

    private final String displayName;
    private final int value;

    PlayerRank(String displayName, int value) {
        this.displayName = displayName;
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getValue() {
        return value;
    }

    public static PlayerRank getRankFromValue(int value) {
        for (PlayerRank rank : values()) {
            if (rank.value == value) {
                return rank;
            }
        }
        return MEMBER;
    }

    public static PlayerRank getRankFromDisplayName(String name) {
        for (PlayerRank rank : values()) {
            if (rank.displayName.equalsIgnoreCase(name)) {
                return rank;
            }
        }
        return null;
    }
}
