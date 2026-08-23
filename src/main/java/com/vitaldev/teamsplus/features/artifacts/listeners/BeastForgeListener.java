package com.vitaldev.teamsplus.features.artifacts.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.teamsplus.model.Team;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.ThreadLocalRandom;

public class BeastForgeListener implements Listener {

    public BeastForgeListener(TeamsPlus plugin) {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        EntityType type = event.getEntityType();
        if (!type.isSpawnable() || !type.isAlive() || type.getEntityClass() == null) {
            return;
        }

        Team team = ArtifactListenerUtil.getOwningTeamWithArtifact(event.getLocation(), ArtifactType.BEAST_FORGE);
        if (team == null) {
            return;
        }

        if (ThreadLocalRandom.current().nextDouble() > 0.25D) {
            return;
        }

        World world = event.getLocation().getWorld();
        if (world == null) {
            return;
        }

        Location extraSpawn = event.getLocation().clone().add(0.5D, 0.0D, 0.5D);
        world.spawn(extraSpawn, (Class<? extends LivingEntity>) type.getEntityClass(), CreatureSpawnEvent.SpawnReason.CUSTOM, entity -> {
        });
    }
}
