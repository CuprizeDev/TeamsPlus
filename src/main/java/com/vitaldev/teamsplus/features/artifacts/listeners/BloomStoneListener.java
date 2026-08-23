package com.vitaldev.teamsplus.features.artifacts.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.teamsplus.model.Team;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

public class BloomStoneListener implements Listener {

    public BloomStoneListener(TeamsPlus plugin) {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        Team team = ArtifactListenerUtil.getOwningTeamWithArtifact(block.getLocation(), ArtifactType.BLOOM_STONE);
        if (team == null) {
            return;
        }

        if (!(event.getNewState().getBlockData() instanceof Ageable ageable)) {
            return;
        }

        int currentAge = ageable.getAge();
        int maxAge = ageable.getMaximumAge();
        if (currentAge >= maxAge) {
            return;
        }

        Ageable accelerated = (Ageable) ageable.clone();
        accelerated.setAge(Math.min(maxAge, currentAge + 2));
        event.getNewState().setBlockData(accelerated);
    }
}
