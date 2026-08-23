package com.vitaldev.teamsplus.features.leaderboard;

import com.vitaldev.teamsplus.model.Team;
import java.util.*;

public class LeaderboardCache {

    private final List<LeaderboardEntry> leaderboard = new ArrayList<>();

    public void updateCache(Map<UUID, Long> rawPowerMap) {
        leaderboard.clear();
        for (Map.Entry<UUID, Long> entry : rawPowerMap.entrySet()) {
            Team team = Team.getTeam(entry.getKey());
            if (team != null) {
                leaderboard.add(new LeaderboardEntry(entry.getKey(), team.getTeamName(), entry.getValue()));
            }
        }
        Collections.sort(leaderboard);
    }

    public List<LeaderboardEntry> getTop(int limit) {
        if (leaderboard.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(leaderboard.subList(0, Math.min(limit, leaderboard.size())));
    }

    public int getRank(Team team) {
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).getTeamUUID().equals(team.getTeamUUID())) {
                return i + 1;
            }
        }
        return -1;
    }

    public void addLivePower(Team team, long amount) {
        boolean found = false;
        for (LeaderboardEntry entry : leaderboard) {
            if (entry.getTeamUUID().equals(team.getTeamUUID())) {
                entry.addPower(amount);
                found = true;
                break;
            }
        }
        if (!found) {
            leaderboard.add(new LeaderboardEntry(team.getTeamUUID(), team.getTeamName(), team.getPower() + amount));
        }
        Collections.sort(leaderboard);
        team.addPower((int) amount);
    }

    public void removeLivePower(Team team, long amount) {
        for (LeaderboardEntry entry : leaderboard) {
            if (entry.getTeamUUID().equals(team.getTeamUUID())) {
                entry.removePower(amount);
                break;
            }
        }
        Collections.sort(leaderboard);
        team.removePower((int) amount);
        if (team.getPower() < 0) team.setPower(0);
    }
}
