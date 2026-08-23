package com.vitaldev.teamsplus.features.artifacts.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.teamsplus.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class HasteListener implements Listener {

    public HasteListener(TeamsPlus plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (UUID teamUUID : Team.getTeamList()) {
                Team team = Team.getTeam(teamUUID);
                if (team == null || !team.hasArtifactApplied(ArtifactType.HASTE)) {
                    continue;
                }

                for (Player player : team.getOnlineMembers()) {
                    if (!team.isInClaim(player)) {
                        continue;
                    }

                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 80, 0, true, false, true));
                }
            }
        }, 20L, 20L);
    }
}
