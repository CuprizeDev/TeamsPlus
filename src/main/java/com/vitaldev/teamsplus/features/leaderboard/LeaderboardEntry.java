package com.vitaldev.teamsplus.features.leaderboard;

import java.util.UUID;

public class LeaderboardEntry implements Comparable<LeaderboardEntry> {

    private final UUID teamUUID;
    private final String teamName;
    private long power;

    public LeaderboardEntry(UUID teamUUID, String teamName, long power) {
        this.teamUUID = teamUUID;
        this.teamName = teamName;
        this.power = power;
    }

    public UUID getTeamUUID() {
        return teamUUID;
    }

    public String getTeamName() {
        return teamName;
    }

    public long getPower() {
        return power;
    }

    public void setPower(long power) {
        this.power = power;
    }

    public void addPower(long amount) {
        this.power += amount;
    }

    public void removePower(long amount) {
        this.power -= amount;
        if (this.power < 0) this.power = 0;
    }

    @Override
    public int compareTo(LeaderboardEntry o) {
        return Long.compare(o.power, this.power);
    }
}
