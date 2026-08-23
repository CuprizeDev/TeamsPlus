package com.vitaldev.teamsplus.model;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.teamsplus.features.boosters.ActiveBooster;
import com.vitaldev.teamsplus.features.boosters.BoosterType;
import com.vitaldev.teamsplus.features.logs.LogEntry;
import com.vitaldev.teamsplus.features.logs.LogType;
import com.vitaldev.teamsplus.features.permissions.PermissableAction;
import com.vitaldev.teamsplus.features.permissions.PlayerRank;
import com.vitaldev.teamsplus.features.upgrades.UpgradeType;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ChatUtil;
import com.vitaldev.vitallibs.util.FileUtil;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.DisableCause;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.Collections;

public class Team {

    private String teamName;
    private UUID leader;
    private final Set<UUID> members;
    private final Set<UUID> allies;
    private final Set<UUID> allyRequests;
    private final Set<ClaimKey> claims;
    public Map<UUID, PlayerRank> playerRanks = new HashMap<>();
    public Map<UUID, Boolean> teamChat = new HashMap<>();
    private final Hologram hologram;
    private final List<UUID> invites = new LinkedList<>();
    private final UUID uuid;
    private int power = 0;
    private final Location claimChest;
    public TeamsPlus plugin;
    private final ConfigHandler configHandler;
    private final ConfigHandler upgradeHandler;
    // Cached hologram template lines — avoids re-reading config on every updateHologram() call.
    private List<String> hologramTemplates;
    private boolean shieldStatus = false;
    private long shieldChargeSeconds = 0;
    private boolean autoShieldEnabled = false;
    private boolean shieldDeploying = false;
    private long shieldDeployStartTime = 0;
    private boolean shieldManualDeploy = false;
    private boolean shieldActive = false;
    private long shieldActivationTime = 0;
    private long shieldCooldownEndTime = 0;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final EnumMap<UpgradeType, Integer> upgrades = new EnumMap<>(UpgradeType.class);
    private final EnumMap<ArtifactType, Integer> artifacts = new EnumMap<>(ArtifactType.class);
    private final EnumMap<BoosterType, ActiveBooster> activeBoosters = new EnumMap<>(BoosterType.class);
    private final EnumMap<LogType, LinkedList<LogEntry>> logs = new EnumMap<>(LogType.class);
    // permissions[rank][action] = allowed
    private final Map<PlayerRank, Map<PermissableAction, Boolean>> permissions = new EnumMap<>(PlayerRank.class);
    int durability;

    public Team(TeamsPlus plugin, String teamName, UUID leader, UUID uuid, Location claimChestLocation) {
        this.teamName = teamName;
        this.leader = leader;
        this.claimChest = claimChestLocation;
        this.allyRequests = new HashSet<>();
        this.members = new HashSet<>();
        this.allies = new HashSet<>();
        this.claims = new HashSet<>(); // stores ClaimKey, not Chunk
        this.plugin = plugin;
        this.uuid = uuid;
        this.configHandler = plugin.getConfigFile();
        this.upgradeHandler = plugin.getUpgrades();

        upgrades.put(UpgradeType.EXP, 0);
        upgrades.put(UpgradeType.ARTIFACTS, 0);
        upgrades.put(UpgradeType.DURABILITY, 0);

        durability = getUpgradeEffect(UpgradeType.DURABILITY);

        // Initialize per-rank permissions from permissions.yml defaults
        initDefaultPermissions(plugin.getPermissionsFile());

       if (DHAPI.getHologram(uuid.toString()) != null) {
           removeHologram();
       }

        this.hologram = new Hologram(uuid.toString(), getClaimChest().add(0.5, 2.5, 0.5));

        // Cache templates once — reused in updateHologram() without re-reading config.
        this.hologramTemplates = configHandler.getColoredList("teams.chest.hologram");
        String initDurability = String.valueOf(durability);
        String initShield = getShieldStatusPlaceHolder();
        String initName = getTeamName();
        for (String line : hologramTemplates) {
            DHAPI.addHologramLine(hologram, line
                    .replace("{DURABILITY}", initDurability)
                    .replace("{SHIELD-STATUS}", initShield)
                    .replace("{TEAM}", initName));
        }

        addPlayerToTeam(leader, getTeamUUID());
        allTeams.add(getTeamUUID());
        // Use chunk-coordinate math to avoid loading the chunk during startup data loading.
        ClaimKey chestKey = new ClaimKey(
                claimChestLocation.getWorld().getUID(),
                claimChestLocation.getBlockX() >> 4,
                claimChestLocation.getBlockZ() >> 4);
        addClaimChest(this, chestKey);
        addClaim(chestKey);
        addMember(leader, PlayerRank.LEADER);
        new FileUtil().createJsonFile(plugin, "data/" + getTeamUUID());
    }

    // Relations

    public Relation getRelationTo(Team randomTeam) {

            if (allies.contains(randomTeam.getTeamUUID())) {
                return Relation.ALLY;
            }

            if (randomTeam.getTeamUUID().equals(uuid)) {
                return Relation.OWN;
            }

        return Relation.ENEMY;
    }

    public Relation getClaimRelation(Chunk chunk) {
        if (isSpawnChunk(plugin, chunk.getWorld().getUID(), chunk.getX(), chunk.getZ())) return Relation.SPAWN;
        UUID randomTeamUUID = getClaim(chunk);

        if (randomTeamUUID == null) {
            return Relation.WILDERNESS;
        }

        if (allies.contains(randomTeamUUID)) {
            return Relation.ALLY;
        }

        if (randomTeamUUID.equals(uuid)) {
            return Relation.OWN;
        }

        return Relation.ENEMY;
    }

    // Artifacts

    public Map<ArtifactType, Integer> getArtifacts() {
        return artifacts;
    }

    public int getArtifactSlot(ArtifactType artifact) {
        return artifacts.get(artifact);
    }

    public boolean hasArtifactApplied(ArtifactType type) {
        return artifacts.containsKey(type);
    }

    public ArtifactType getArtifactFromSlot(int slot) {
        for (Map.Entry<ArtifactType, Integer> entry : artifacts.entrySet()) {
            if (entry.getValue() == slot) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void setArtifactSlot(ArtifactType artifact, int slot) {
        artifacts.put(artifact, slot);
    }

    public void clearSlot(int slot) {
        artifacts.values().remove(slot);
    }

    // Boosters

    public Map<BoosterType, ActiveBooster> getActiveBoosters() {
        return activeBoosters;
    }

    public void addActiveBooster(ActiveBooster booster) {
        activeBoosters.put(booster.getType(), booster);
    }

    public void removeActiveBooster(BoosterType type) {
        activeBoosters.remove(type);
    }

    public ActiveBooster getActiveBooster(BoosterType type) {
        return activeBoosters.get(type);
    }

    public boolean hasActiveBooster(BoosterType type) {
        return activeBoosters.containsKey(type) && !activeBoosters.get(type).isExpired();
    }

    // Logs

    public void addLog(LogEntry entry) {
        LinkedList<LogEntry> typeLogs = logs.computeIfAbsent(entry.getType(), k -> new LinkedList<>());
        typeLogs.addFirst(entry);
        if (typeLogs.size() > 30) {
            typeLogs.removeLast();
        }
    }

    public List<LogEntry> getLogs(LogType type) {
        return logs.getOrDefault(type, new LinkedList<>());
    }
    
    public Map<LogType, LinkedList<LogEntry>> getAllLogs() {
        return logs;
    }

    // Permissions

    // Populates the permissions map with defaults from permissions.yml.
// Called once in the constructor for brand-new teams. Existing teams are
// restored via TeamData#loadExtraData.
    private void initDefaultPermissions(ConfigHandler permissionsFile) {
        for (PlayerRank rank : new PlayerRank[]{PlayerRank.MEMBER, PlayerRank.OFFICER, PlayerRank.CO_LEADER}) {
            Map<PermissableAction, Boolean> rankPerms = new EnumMap<>(PermissableAction.class);
            for (PermissableAction action : PermissableAction.values()) {
                boolean defaultValue = false;
                if (permissionsFile != null) {
                    String path = "permissions.default." + rank.name() + ".actions." + action.name().toLowerCase();
                    // getBoolean returns false if path missing — that's an acceptable default
                    defaultValue = permissionsFile.getBoolean(path);
                }
                rankPerms.put(action, defaultValue);
            }
            permissions.put(rank, rankPerms);
        }
    }

    public boolean getPermission(PlayerRank rank, PermissableAction action) {
        // Leader always has full access
        if (rank == PlayerRank.LEADER) return true;
        Map<PermissableAction, Boolean> rankPerms = permissions.get(rank);
        if (rankPerms == null) return false;
        return Boolean.TRUE.equals(rankPerms.get(action));
    }

    public void setPermission(PlayerRank rank, PermissableAction action, boolean value) {
        // Leader permissions are immutable
        if (rank == PlayerRank.LEADER) return;
        permissions.computeIfAbsent(rank, r -> new EnumMap<>(PermissableAction.class)).put(action, value);
    }

    public void togglePermission(PlayerRank rank, PermissableAction action) {
        setPermission(rank, action, !getPermission(rank, action));
    }

    // Convenience check: can this player perform the given action in this team's claims?
    public boolean canDo(Player player, PermissableAction action) {
        PlayerRank rank = getPlayerRank(player);
        if (rank == null) return false;
        return getPermission(rank, action);
    }

    public Map<PlayerRank, Map<PermissableAction, Boolean>> getPermissions() {
        return permissions;
    }

    // Upgrades

    public Map<UpgradeType, Integer> getUpgrades() {
        return upgrades;
    }

    public int getUpgradeLevel(UpgradeType upgrade) {
        return upgrades.get(upgrade);
    }

    public void setUpgradeLevel(UpgradeType upgrade, int level) {
        upgrades.replace(upgrade, level);
    }

    public int getUpgradeEffect(UpgradeType upgrade) {

        int level = getUpgradeLevel(upgrade);
        String path = "upgrades.menu.upgrades." + upgrade.name().toLowerCase();

        if (level == 0) {
            return plugin.getUpgrades().getInt(path + ".default");
        }

        return plugin.getUpgrades().getInt(path + ".levels." + level + ".effect");
    }

    public void upgradeLevel(UpgradeType upgrade) {
        upgrades.put(upgrade, upgrades.get(upgrade) + 1);
    }

    // Cooldowns

    public Map<String, Long> getCooldowns() {
        return cooldowns;
    }

    public void startCooldown(String action, long durationMillis) {
        long expirationTime = System.currentTimeMillis() + durationMillis;
        getCooldowns().put(action, expirationTime);
    }


    public boolean isOnCooldown(Team team, String action) {
        Long expirationTime = team.getCooldowns().get(action);
        if (expirationTime == null) {
            return false;
        }
        return expirationTime > System.currentTimeMillis();
    }

    public long getRemainingCooldown(Team team, String action) {
        Long expirationTime = team.getCooldowns().get(action);
        if (expirationTime == null) {
            return 0;
        }
        long currentTime = System.currentTimeMillis();
        return Math.max(0, expirationTime - currentTime);
    }

    // Durability

    public int getDurability() {
        return durability;
    }

    public void setDurability(int amount) {
        durability = amount;
    }

    public int getMaxDurability() {

        int level = getUpgradeLevel(UpgradeType.DURABILITY);
        String path = "upgrades.menu.upgrades.durability.";
        if (level == 0) {
            return upgradeHandler.getInt(path + "default");
        }

        return plugin.getUpgrades().getInt(path + "levels." + level + ".effect");
    }

    public void removeDurability(int amount) {
        durability -= amount;
    }

    public void addDurability(int amount) {
        durability += amount;
    }

    // Shield

    public boolean getShieldStatus() {
        return shieldActive;
    }

    public String getShieldStatusPlaceHolder() {
        if (shieldActive) return "Active";
        if (shieldDeploying) return "Deploying";
        return "Inactive";
    }

    public void setShieldStatus(boolean status) {
        shieldActive = status;
    }

    public long getShieldChargeSeconds() { return shieldChargeSeconds; }
    public void setShieldChargeSeconds(long charge) { this.shieldChargeSeconds = Math.min(charge, 86400); }
    public void addShieldCharge(double seconds) { this.shieldChargeSeconds = (long) Math.min(shieldChargeSeconds + seconds, 86400); }

    public boolean isAutoShieldEnabled() { return autoShieldEnabled; }
    public void setAutoShieldEnabled(boolean enabled) { this.autoShieldEnabled = enabled; }

    public boolean isShieldDeploying() { return shieldDeploying; }
    public void setShieldDeploying(boolean deploying) { this.shieldDeploying = deploying; }

    public long getShieldDeployStartTime() { return shieldDeployStartTime; }
    public void setShieldDeployStartTime(long time) { this.shieldDeployStartTime = time; }

    public boolean isShieldManualDeploy() { return shieldManualDeploy; }
    public void setShieldManualDeploy(boolean manual) { this.shieldManualDeploy = manual; }

    public boolean isShieldActive() { return shieldActive; }
    public void setShieldActive(boolean active) { this.shieldActive = active; }

    public long getShieldActivationTime() { return shieldActivationTime; }
    public void setShieldActivationTime(long time) { this.shieldActivationTime = time; }

    public long getShieldCooldownEndTime() { return shieldCooldownEndTime; }
    public void setShieldCooldownEndTime(long time) { this.shieldCooldownEndTime = time; }

    // Hologram

    public void enableHologram() {
        hologram.enable();
    }

    public void disableHologram() {
        hologram.disable(DisableCause.API);
    }

    public void removeHologram() {
        if (hologram!= null) {
            DHAPI.removeHologram(hologram.getId());
        }
    }

    public void updateHologram() {
        if (hologram == null || !hologram.isEnabled()) return;

        // Use cached templates — no config I/O here.
        String durStr = String.valueOf(getDurability());
        String shieldStr = getShieldStatusPlaceHolder();
        String nameStr = getTeamName();

        HologramPage page = DHAPI.getHologramPage(hologram, 0);
        if (page == null) return;
        List<HologramLine> currentLines = page.getLines();

        int lineCount = Math.min(hologramTemplates.size(), currentLines.size());
        for (int i = 0; i < lineCount; i++) {
            String updated = hologramTemplates.get(i)
                    .replace("{DURABILITY}", durStr)
                    .replace("{SHIELD-STATUS}", shieldStr)
                    .replace("{TEAM}", nameStr);

            if (!currentLines.get(i).getContent().equals(updated)) {
                page.getLine(i).setContent(updated);
            }
        }
        hologram.updateAll();
    }

    // Message Team

    public void sendMessage(String message) {
        for (Player player : getOnlineMembers()) {
            player.sendMessage(ChatUtil.color(message));
        }
    }

    // Invites

    public void addInvite(Player player) {
        invites.add(player.getUniqueId());
    }

    public void addInvite(UUID playerUUID) {
        invites.add(playerUUID);
    }

    public void removeInvite(Player player) {
        invites.remove(player.getUniqueId());
    }

    public void removeInvite(UUID playerUUID) {
        invites.remove(playerUUID);
    }

    public boolean isInvited(Player player) {
        return invites.contains(player.getUniqueId());
    }

    public boolean isInvited(UUID playerUUID) {
        return invites.contains(playerUUID);
    }

    public List<UUID> getInvites() {
        return invites;
    }

    // Power

    public int getPower() {
        return power;
    }

    public void setPower(int amount) {
        power = amount;
    }

    public void addPower(int amount) {
        power += amount;
    }

    public void removePower(int amount) {
        power -= amount;
    }

    // Members

    public void addMember(Player player, PlayerRank rank) {
        allPlayerTeams.put(player.getUniqueId(), getTeamUUID());
        members.add(player.getUniqueId());
        setPlayerRank(player, rank);
    }

    public void addMember(UUID playerUUID, PlayerRank rank) {
        allPlayerTeams.put(playerUUID, getTeamUUID());
        members.add(playerUUID);
        setPlayerRank(playerUUID, rank);
    }

    public void removeMember(Player player) {
        members.remove(player.getUniqueId());
        allPlayerTeams.remove(player.getUniqueId());
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public int getOnlineMemberCount() {
        int onlineCount = 0;
        for (UUID memberId : getMembers()) {
            Player player = Bukkit.getPlayer(memberId);
            if (player != null && player.isOnline()) {
                onlineCount++;
            }
        }
        return onlineCount;
    }

    public int getMemberCount() {
        return members.size();
    }

    public List<Player> getOnlineMembers() {
        List<Player> onlinePlayers = new ArrayList<>();
        for (UUID memberId : getMembers()) {
            Player player = Bukkit.getPlayer(memberId);
            if (player != null && player.isOnline()) {
                onlinePlayers.add(player);
            }
        }
        return onlinePlayers;
    }

    public boolean isMember(Player player) {
        return members.contains(player.getUniqueId());
    }

    public boolean isMember(UUID playerUUID) {
        return members.contains(playerUUID);
    }

    // Chat

    public boolean hasChatToggled(UUID playerUUID) {
        return teamChat.containsKey(playerUUID);
    }

    public boolean isTeamChatEnabled(Player player) {
        return teamChat.getOrDefault(player.getUniqueId(), false);
    }

    public boolean isTeamChatEnabled(UUID playerUUID) {
        return teamChat.getOrDefault(playerUUID, false);
    }

    public void setTeamChat(Player player, Boolean toggle) {
        teamChat.put(player.getUniqueId(), toggle);
    }

    public void setTeamChat(UUID playerUUID, Boolean toggle) {
        teamChat.put(playerUUID, toggle);
    }

    // Ranks

    public void setPlayerRank(Player player, PlayerRank rank) {
        playerRanks.put(player.getUniqueId(), rank);
    }

    public void setPlayerRank(UUID playerUUID, PlayerRank rank) {
        playerRanks.put(playerUUID, rank);
    }

    public PlayerRank getPlayerRank(Player player) {
        return playerRanks.get(player.getUniqueId());
    }

    public boolean isHigherRank(Player player1, Player player2) {
        return getPlayerRank(player1).getValue() > getPlayerRank(player2).getValue();
    }

    public PlayerRank getPlayerRank(UUID playerUUID) {
        return playerRanks.get(playerUUID);
    }

    public void promote(Player player) {
        PlayerRank rank = getPlayerRank(player);
        if (rank == PlayerRank.MEMBER) {
            playerRanks.remove(player.getUniqueId());
            setPlayerRank(player, PlayerRank.OFFICER);
            return;
        }

        if (rank == PlayerRank.OFFICER) {
            playerRanks.remove(player.getUniqueId());
            setPlayerRank(player, PlayerRank.CO_LEADER);
            return;
        }

        if (rank == PlayerRank.CO_LEADER) {
            playerRanks.remove(player.getUniqueId());
            setPlayerRank(player, PlayerRank.LEADER);
        }
    }

    public void promote(UUID playerUUID) {
        PlayerRank rank = getPlayerRank(playerUUID);
        if (rank == PlayerRank.MEMBER) {
            playerRanks.remove(playerUUID);
            setPlayerRank(playerUUID, PlayerRank.OFFICER);
            return;
        }

        if (rank == PlayerRank.OFFICER) {
            playerRanks.remove(playerUUID);
            setPlayerRank(playerUUID, PlayerRank.CO_LEADER);
            return;
        }

        if (rank == PlayerRank.CO_LEADER) {
            playerRanks.remove(playerUUID);
            setPlayerRank(playerUUID, PlayerRank.LEADER);
        }
    }

    public void demote(Player player) {
        PlayerRank rank = getPlayerRank(player);

        if (rank == PlayerRank.OFFICER) {
            playerRanks.remove(player.getUniqueId());
            setPlayerRank(player, PlayerRank.MEMBER);
            return;
        }

        if (rank == PlayerRank.CO_LEADER) {
            playerRanks.remove(player.getUniqueId());
            setPlayerRank(player, PlayerRank.OFFICER);
        }
    }

    public void demote(UUID playerUUID) {
        PlayerRank rank = getPlayerRank(playerUUID);

        if (rank == PlayerRank.OFFICER) {
            playerRanks.remove(playerUUID);
            setPlayerRank(playerUUID, PlayerRank.MEMBER);
            return;
        }

        if (rank == PlayerRank.CO_LEADER) {
            playerRanks.remove(playerUUID);
            setPlayerRank(playerUUID, PlayerRank.OFFICER);
        }
    }


    // Leader

    public UUID getLeaderUUID() {
        return leader;
    }

    public Player getLeader() {
        Player leaderPlayer = Bukkit.getPlayer(leader);

        if (leaderPlayer != null && leaderPlayer.isOnline()) {
            return leaderPlayer;
        }

        return Bukkit.getOfflinePlayer(leader).getPlayer();
    }

    public void setLeader(Player player) {
        setPlayerRank(player, PlayerRank.LEADER);
        this.leader = player.getUniqueId();
    }

    public void setLeader(UUID playerUUID) {
        setPlayerRank(playerUUID, PlayerRank.LEADER);
        this.leader = playerUUID;
    }

    // Basic Properties

    public String getTeamName() {
        return teamName;
    }

    public UUID getTeamUUID() {
        return uuid;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    // Alies

    public void addAlly(UUID teamUUID) {
        allies.add(teamUUID);
    }

    public void removeAlly(UUID teamUUID) {
        allies.remove(teamUUID);
    }

    public Set<UUID> getAllies() {
        return new HashSet<>(allies);
    }

    public int getAllyCount() {
        return allies.size();
    }

    public void addAllyRequest(UUID teamUUID) {
        allyRequests.add(teamUUID);
    }

    public void removeAllyRequest(UUID teamUUId) {
        allyRequests.remove(teamUUId);
    }

    public Set<UUID> getAllyRequests() {
        return new HashSet<>(allyRequests);
    }

    // Claims

    public boolean isInClaim(Player player) {
        Location loc = player.getLocation();
        // Compute chunk coords from player location without calling getChunk() (avoids potential load).
        return claims.contains(new ClaimKey(
                loc.getWorld().getUID(),
                loc.getBlockX() >> 4,
                loc.getBlockZ() >> 4));
    }

    public Location getClaimChest() {
        return claimChest.clone();
    }

    // ── Claim mutators ────────────────────────────────────────────────────────

    // Add a claim from a ClaimKey — preferred for startup loading (no chunk load).
    public void addClaim(ClaimKey key) {
        claims.add(key);
        allClaims.put(key, getTeamUUID());
    }

    // Add a claim from an already-loaded Chunk (gameplay context).
    public void addClaim(Chunk chunk) {
        addClaim(ClaimKey.of(chunk));
    }

    public void removeClaim(Chunk chunk) {
        ClaimKey key = ClaimKey.of(chunk);
        claims.remove(key);
        allClaims.remove(key);
    }

    // Remove a claim by ClaimKey — no chunk loading required (GUI context).
    public void removeClaim(ClaimKey key) {
        claims.remove(key);
        allClaims.remove(key);
    }

    // Returns an unmodifiable view over ClaimKeys — no defensive copy allocation.
    public Set<ClaimKey> getClaims() {
        return Collections.unmodifiableSet(claims);
    }

    public int getClaimsCount() {
        return claims.size();
    }

    // Check ownership by already-loaded Chunk (event/gameplay context).
    public boolean ownsClaim(Chunk chunk) {
        return claims.contains(ClaimKey.of(chunk));
    }

    // Check ownership by coordinates — no chunk loading required (GUI/startup context).
    public boolean ownsClaim(int x, int z, UUID worldId) {
        return claims.contains(new ClaimKey(worldId, x, z));
    }

    public static boolean isSpawnChunk(TeamsPlus plugin, UUID worldId, int chunkX, int chunkZ) {
        if (plugin.getClaims().getConfigurationSection("claims.safe-zones") == null) return false;
        org.bukkit.configuration.ConfigurationSection root = plugin.getClaims().getConfigurationSection("claims.safe-zones");
        org.bukkit.World world = Bukkit.getWorld(worldId);
        if (world == null) return false;

        int cMinX = chunkX * 16;
        int cMaxX = cMinX + 15;
        int cMinZ = chunkZ * 16;
        int cMaxZ = cMinZ + 15;

        for (String key : root.getKeys(false)) {
            String wName = root.getString(key + ".world");
            if (wName != null && wName.equals(world.getName())) {
                double minX = root.getDouble(key + ".minX");
                double maxX = root.getDouble(key + ".maxX");
                double minZ = root.getDouble(key + ".minZ");
                double maxZ = root.getDouble(key + ".maxZ");

                double trueMinX = Math.min(minX, maxX);
                double trueMaxX = Math.max(minX, maxX);
                double trueMinZ = Math.min(minZ, maxZ);
                double trueMaxZ = Math.max(minZ, maxZ);

                // Check intersection
                if (cMinX <= trueMaxX && cMaxX >= trueMinX && cMinZ <= trueMaxZ && cMaxZ >= trueMinZ) {
                    return true;
                }
            }
        }
        return false;
    }

    // Coordinate-based claim relation — lets the GUI query ownership without ever calling
    // {@code world.getChunkAt(x, z)}.
    public Relation getClaimRelation(int chunkX, int chunkZ, UUID worldId) {
        if (isSpawnChunk(plugin, worldId, chunkX, chunkZ)) return Relation.SPAWN;
        UUID ownerId = getClaim(worldId, chunkX, chunkZ);
        if (ownerId == null) return Relation.WILDERNESS;
        if (allies.contains(ownerId)) return Relation.ALLY;
        if (ownerId.equals(uuid)) return Relation.OWN;
        return Relation.ENEMY;
    }

    // Static Methods

    protected static final HashMap<UUID, UUID> allPlayerTeams = new HashMap<>();
    protected static final List<UUID> allTeams = new LinkedList<>();
    protected static final Map<UUID, Team> allTeamUUIDS = new HashMap<>();
    protected static final HashMap<ClaimKey, UUID> allClaims = new HashMap<>();
    protected static final HashMap<ClaimKey, UUID> allClaimChests = new HashMap<>();

    // Message Team

    public static void messageTeam(UUID teamUUID, String message) {
        for (Player player : getTeam(teamUUID).getOnlineMembers()) {
            player.sendMessage(ChatUtil.color(message));
        }
    }

    // Claims

    // Add/update a claim from a key (startup-safe, no chunk loading).
    public static void addClaim(UUID teamUUID, ClaimKey key) {
        allClaims.put(key, teamUUID);
    }

    // Add/update a claim from an already-loaded Chunk (gameplay context).
    public static void addClaim(UUID teamUUID, Chunk chunk) {
        allClaims.put(ClaimKey.of(chunk), teamUUID);
    }

    public static void removeClaim(UUID teamUUID, Chunk chunk) {
        allClaims.remove(ClaimKey.of(chunk), teamUUID);
    }

    // Returns an unmodifiable view of the global claims map keyed by {@link ClaimKey}.
// Callers must not mutate this map. Use the mutator methods for writes.
    public static Map<ClaimKey, UUID> getAllClaims() {
        return Collections.unmodifiableMap(allClaims);
    }

    // Look up claim owner by already-loaded Chunk (event/gameplay context).
    public static UUID getClaim(Chunk chunk) {
        return allClaims.get(ClaimKey.of(chunk));
    }

    // Look up claim owner by coordinates — no chunk loading (GUI/startup context).
    public static UUID getClaim(UUID worldId, int x, int z) {
        return allClaims.get(new ClaimKey(worldId, x, z));
    }

    // Check if a loaded chunk is claimed.
    public static boolean isClaimed(Chunk chunk) {
        return allClaims.containsKey(ClaimKey.of(chunk));
    }

    // Check if coordinates are claimed — no chunk loading (GUI/startup context).
    public static boolean isClaimed(UUID worldId, int x, int z) {
        return allClaims.containsKey(new ClaimKey(worldId, x, z));
    }

    // Team

    public static List<UUID> getTeamList() {
        return allTeams;
    }

    public static void addPlayerToTeam(Player player, UUID teamUUID) {
        allPlayerTeams.put(player.getUniqueId(), teamUUID);
    }

    public static void addPlayerToTeam(UUID playerUUID, UUID teamUUID) {
        allPlayerTeams.put(playerUUID, teamUUID);
    }

    public static void removePlayerFromTeam(Player player) {
        allPlayerTeams.remove(player.getUniqueId());
    }

    public static Team getTeam(Player player) {
        return getTeam(allPlayerTeams.get(player.getUniqueId()));
    }

    public static boolean hasTeam(Player player) {
        return allPlayerTeams.containsKey(player.getUniqueId());
    }

    public static Team getTeam(UUID teamUUID) {
        return allTeamUUIDS.get(teamUUID);
    }

    public static void addUUID(UUID teamUUID, Team team) {
        allTeamUUIDS.put(teamUUID, team);
    }

    public static void disbandTeam(Team team, Plugin plugin) {
        UUID teamUUID = team.getTeamUUID();
        team.removeHologram();
        for (ClaimKey key : team.getClaims()) {
            allClaims.remove(key);
        }

        for (UUID memberUUID : new HashSet<>(team.getMembers())) {
            allPlayerTeams.remove(memberUUID);
        }

        allTeams.remove(teamUUID);
        allTeamUUIDS.remove(teamUUID);
        removeClaimChest(team);

        new FileUtil().removeFile(plugin, "data/" + team.getTeamUUID() + ".json");
    }

    public static void disbandTeam(UUID teamUUID, Plugin plugin) {
        Team team = getTeam(teamUUID);
        if (team != null) {
            disbandTeam(team, plugin);
        }
    }

    // Claim Chest

    public static void removeClaimChest(Team team) {
        Block block = team.getClaimChest().getBlock(); // chunk is already loaded at disband time
        block.setType(Material.AIR);
        Location chest = team.getClaimChest();
        allClaimChests.remove(new ClaimKey(
                chest.getWorld().getUID(),
                chest.getBlockX() >> 4,
                chest.getBlockZ() >> 4));
    }

    public static Location getClaimChest(Team team) {
        return team.getClaimChest();
    }

    // Register a claim chest from an already-loaded Chunk (gameplay context).
    public static void addClaimChest(Team team, Chunk chunk) {
        addClaimChest(team, ClaimKey.of(chunk));
    }

    // Register a claim chest from a key — startup-safe, no chunk loading.
    public static void addClaimChest(Team team, ClaimKey key) {
        allClaimChests.put(key, team.getTeamUUID());
        addClaim(team.getTeamUUID(), key);
    }

    public static boolean containsClaimChest(Chunk chunk) {
        return allClaimChests.containsKey(ClaimKey.of(chunk));
    }


}
