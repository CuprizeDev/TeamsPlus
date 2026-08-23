package com.vitaldev.teamsplus.dependencies;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.leaderboard.LeaderboardCache;
import com.vitaldev.teamsplus.features.leaderboard.LeaderboardService;
import com.vitaldev.teamsplus.model.Team;
import dev.rosewood.rosestacker.event.SpawnerStackEvent;
import dev.rosewood.rosestacker.event.SpawnerUnstackEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

// Optional RoseStacker integration for tracking stacked spawner events.
// <p>
// This class references RoseStacker classes directly, so it must only be
// constructed when RoseStacker is confirmed present on the classpath.
// {@link DependencyManager} guards construction with a try/catch.
public class RoseStackerHook implements Listener {

    private final TeamsPlus plugin;
    private final LeaderboardService service;
    private final LeaderboardCache cache;

    public RoseStackerHook(TeamsPlus plugin, LeaderboardService service, LeaderboardCache cache) {
        this.plugin = plugin;
        this.service = service;
        this.cache = cache;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawnerStack(SpawnerStackEvent event) {
        Block block = event.getStack().getBlock();
        if (block.getType() != Material.SPAWNER) return;

        Chunk chunk = block.getChunk();
        if (Team.isClaimed(chunk)) {
            UUID teamUUID = Team.getClaim(chunk);
            Team team = Team.getTeam(teamUUID);
            if (team != null) {
                // Wait 1 tick for spawner data to be set
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (block.getState() instanceof CreatureSpawner spawner) {
                        EntityType type = spawner.getSpawnedType();
                        if (type != null) {
                            int val = service.getSpawnerValue(type);
                            if (val > 0) {
                                cache.addLivePower(team, val);
                            }
                        }
                    }
                }, 1L);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerUnstack(SpawnerUnstackEvent event) {
        Block block = event.getStack().getBlock();

        if (block.getType() != Material.SPAWNER) return;
        if (block.getState() instanceof CreatureSpawner spawner) {
            EntityType type = spawner.getSpawnedType();
            Chunk chunk = block.getChunk();
            if (Team.isClaimed(chunk)) {
                UUID teamUUID = Team.getClaim(chunk);
                Team team = Team.getTeam(teamUUID);
                if (team != null && type != null) {
                    int val = service.getSpawnerValue(type);
                    if (val > 0) {
                        cache.removeLivePower(team, val);
                    }
                }
            }
        }
    }
}
