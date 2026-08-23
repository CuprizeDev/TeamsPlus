package com.vitaldev.teamsplus.features.artifacts.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.teamsplus.model.Team;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class BeastBaneListener implements Listener {

    public BeastBaneListener(TeamsPlus plugin) {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        Team team = ArtifactListenerUtil.getPlayerTeamInOwnClaim(killer, ArtifactType.BEAST_BANE);
        if (team == null) {
            return;
        }

        for (ItemStack drop : event.getDrops().toArray(new ItemStack[0])) {
            event.getDrops().add(drop.clone());
        }
    }
}
