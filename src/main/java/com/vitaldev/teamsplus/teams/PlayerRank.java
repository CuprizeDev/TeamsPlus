package com.vitaldev.teamsplus.teams;

public enum PlayerRank {
    LEADER(3),
    CO_LEADER(2),
    OFFICER(1),
    MEMBER(0);

    public final int value;

    PlayerRank(int value) {
        this.value = value;
    }

    public String getName(PlayerRank playerRank) {
        if (playerRank == LEADER) {
            return "leader";
        }
        if (playerRank == CO_LEADER) {
            return "co-leader";
        }
        if (playerRank == OFFICER) {
            return "officer";
        }
        return "member";
    }

    public String getNameFromValue(int value) {
        if (value == 3) {
            return "leader";
        }
        if (value == 2) {
            return "co-leader";
        }
        if (value == 1) {
            return "officer";
        }
        return "member";
    }

    // Updated getValue method
    public int getValue() {
        return this.value;
    }
}
