package com.vitaldev.teamsplus.features.upgrades.listeners;

import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.teamsplus.features.upgrades.UpgradeType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class ExpListener implements Listener {

    @EventHandler
    public void onExpUpgrade(EntityDeathEvent event) {

        if (event.getEntity().getKiller() == null) {
            return;
        }

        Player player = event.getEntity().getKiller();
        Team team = Team.getTeam(player);
        event.setDroppedExp(event.getDroppedExp() + (team.getUpgradeEffect(UpgradeType.EXP)));
    }
}
