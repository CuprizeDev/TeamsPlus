package com.vitaldev.teamsplus.features.artifacts.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.teamsplus.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class InquisitiveListener implements Listener {

    public InquisitiveListener(TeamsPlus plugin) {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        Team team = ArtifactListenerUtil.getPlayerTeamInOwnClaim(killer, ArtifactType.INQUISITIVE);
        if (team == null) {
            return;
        }

        event.setDroppedExp(event.getDroppedExp() * 2);
    }
}
