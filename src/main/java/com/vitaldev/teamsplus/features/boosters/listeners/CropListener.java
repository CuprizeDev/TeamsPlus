package com.vitaldev.teamsplus.features.boosters.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.boosters.ActiveBooster;
import com.vitaldev.teamsplus.features.boosters.BoosterType;
import com.vitaldev.teamsplus.model.Team;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import java.util.UUID;

public class CropListener implements Listener {

    private final TeamsPlus plugin;

    public CropListener(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCropGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        UUID ownerId = Team.getClaim(block.getChunk());
        if (ownerId == null) return;
        
        Team team = Team.getTeam(ownerId);
        if (team == null) return;

        if (team.hasActiveBooster(BoosterType.CROP_GROWTH)) {
            ActiveBooster booster = team.getActiveBooster(BoosterType.CROP_GROWTH);
            if (booster != null && !booster.isExpired()) {
                BlockState newState = event.getNewState();
                if (newState.getBlockData() instanceof Ageable ageable) {
                    double multiplier = booster.getMultiplier();
                    int extraGrowth = (int) multiplier - 1;
                    if (Math.random() < (multiplier % 1)) {
                        extraGrowth++;
                    }
                    
                    if (extraGrowth > 0) {
                        int currentAge = ageable.getAge();
                        int maxAge = ageable.getMaximumAge();
                        int newAge = Math.min(maxAge, currentAge + extraGrowth);
                        ageable.setAge(newAge);
                        newState.setBlockData(ageable);
                    }
                }
            }
        }
    }
}
