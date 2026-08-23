package com.vitaldev.teamsplus.features.leaderboard;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.ClaimKey;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LeaderboardService {

    private final TeamsPlus plugin;
    private final LeaderboardCache cache;
    private final Map<String, Integer> spawnerValues = new HashMap<>();
    private final List<String> ignoredWorlds = new ArrayList<>();
    private int chunksPerSecond = 10;
    
    private boolean isScanning = false;

    public LeaderboardService(TeamsPlus plugin, LeaderboardCache cache) {
        this.plugin = plugin;
        this.cache = cache;
        loadConfig();
        startScanTimer();
    }

    public void loadConfig() {
        ConfigHandler config = new ConfigHandler(plugin, new FileUtil().getYmlFile(plugin, "leaderboard.yml"));
        ignoredWorlds.clear();
        ignoredWorlds.addAll(config.getStringList("leaderboard.settings.ignored-worlds"));

        spawnerValues.clear();
        ConfigurationSection spawnersSec = config.getConfigurationSection("leaderboard.spawners");
        if (spawnersSec != null) {
            for (String key : spawnersSec.getKeys(false)) {
                spawnerValues.put(key.toUpperCase(), spawnersSec.getInt(key));
            }
        }
        chunksPerSecond = config.getInt("leaderboard.settings.chunks-per-second");
        if (chunksPerSecond <= 0) chunksPerSecond = 10;
    }

    private void startScanTimer() {
        ConfigHandler config = new ConfigHandler(plugin, new FileUtil().getYmlFile(plugin, "leaderboard.yml"));
        long intervalSeconds = config.getLong("leaderboard.settings.scan-interval-seconds");
        long intervalTicks = intervalSeconds * 20L;

        new BukkitRunnable() {
            @Override
            public void run() {
                runScan();
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }

    public void runScan() {
        if (isScanning) {
            // Un-stick if something went terribly wrong
            plugin.getLogger().warning("Leaderboard scan overlapped, skipping this cycle.");
            return;
        }
        isScanning = true;

        List<ChunkData> chunksToScan = new ArrayList<>();

        for (Map.Entry<ClaimKey, UUID> entry : Team.getAllClaims().entrySet()) {
            ClaimKey claim = entry.getKey();
            World world = Bukkit.getWorld(claim.worldId());
            if (world == null || ignoredWorlds.contains(world.getName())) continue;
            chunksToScan.add(new ChunkData(world, claim.x(), claim.z(), entry.getValue()));
        }

        if (chunksToScan.isEmpty()) {
            isScanning = false;
            cache.updateCache(new HashMap<>());
            return;
        }

        Map<UUID, Long> newPowerMap = new HashMap<>();

        int finalChunksPerSecond = chunksPerSecond;
        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= chunksToScan.size()) {
                    finishScan(newPowerMap);
                    this.cancel();
                    return;
                }

                int end = Math.min(index + finalChunksPerSecond, chunksToScan.size());
                for (int i = index; i < end; i++) {
                    ChunkData data = chunksToScan.get(i);
                    scanChunkAsync(data.world, data.x, data.z, data.teamUUID, newPowerMap);
                }
                index = end;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Process chunksPerSecond every second
    }

    private void scanChunkAsync(World world, int x, int z, UUID teamUUID, Map<UUID, Long> powerMap) {
        // Paper async chunk loading API
        world.getChunkAtAsync(x, z, false, chunk -> {
            if (chunk == null) return;
            long chunkPower = 0;
            BlockState[] tileEntities = chunk.getTileEntities();
            for (BlockState state : tileEntities) {
                if (state instanceof CreatureSpawner) {
                    CreatureSpawner spawner = (CreatureSpawner) state;
                    EntityType type = spawner.getSpawnedType();
                    if (type != null) {
                        int val = spawnerValues.getOrDefault(type.name(), 0);
                        chunkPower += val;
                    }
                }
            }

            if (chunkPower > 0) {
                // We are in async callback, but standard hashmap writes should ideally be synced.
                // However getChunkAtAsync consumer runs on main thread in Paper/Spigot! 
                // So it's safe to mutate the map directly.
                powerMap.merge(teamUUID, chunkPower, Long::sum);
            }
        });
    }

    private void finishScan(Map<UUID, Long> newPowerMap) {
        // Update teams
        for (UUID teamUUID : Team.getTeamList()) {
            Team team = Team.getTeam(teamUUID);
            if (team != null) {
                long power = newPowerMap.getOrDefault(teamUUID, 0L);
                team.setPower((int) power);
            }
        }
        cache.updateCache(newPowerMap);
        isScanning = false;
        // Broadcast or log if needed
    }
    
    public int getSpawnerValue(EntityType type) {
        return spawnerValues.getOrDefault(type.name(), 0);
    }

    private static class ChunkData {
        World world;
        int x;
        int z;
        UUID teamUUID;

        public ChunkData(World world, int x, int z, UUID teamUUID) {
            this.world = world;
            this.x = x;
            this.z = z;
            this.teamUUID = teamUUID;
        }
    }
}
