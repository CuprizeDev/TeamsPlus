package com.vitaldev.teamsplus.features.artifacts.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.teamsplus.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BeaconListener implements Listener {

    public BeaconListener(TeamsPlus plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Team team : Team.getTeamList().stream().map(Team::getTeam).filter(t -> t != null && t.hasArtifactApplied(ArtifactType.BEACON)).toList()) {
                for (Player player : team.getOnlineMembers()) {
                    if (!team.isInClaim(player)) {
                        continue;
                    }

                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 0, true, false, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 80, 0, true, false, true));
                }
            }
        }, 20L, 20L);
    }
}
