package com.vitaldev.teamsplus.teams;

import com.vitaldev.teamsplus.TeamsPlus;
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

public class Team {

    private String teamName;
    private UUID leader;
    private final Set<UUID> members;
    private final Set<UUID> allies;
    private final Set<UUID> allyRequests;
    private final Set<Chunk> claims;
    public Map<UUID, PlayerRank> playerRanks = new HashMap<>();
    public Map<UUID, Boolean> teamChat = new HashMap<>();
    private final Hologram hologram;
    private final List<UUID> invites = new LinkedList<>();
    private final UUID uuid;
    private int power;
    private final Location claimChest;
    public TeamsPlus plugin;
    public final ConfigHandler configHandler;
    private boolean shieldStatus;
    int durability;
    private Map<String, Long> cooldowns = new HashMap<>();
    private Map<UpgradeType, Integer> upgrades = new HashMap<>();


    public Team(TeamsPlus plugin, String teamName, UUID leader, UUID uuid, Location claimChestLocation) {
        this.teamName = teamName;
        this.leader = leader;
        this.claimChest = claimChestLocation;
        this.allyRequests = new HashSet<>();
        this.members = new HashSet<>();
        this.allies = new HashSet<>();
        this.claims = new HashSet<>();
        this.plugin = plugin;
        this.uuid = uuid;
        this.configHandler = plugin.getConfigFile();

       if (DHAPI.getHologram(uuid.toString()) != null) {
           removeHologram();
       }

        this.hologram = new Hologram(uuid.toString(), getClaimChest().add(0.5, 2.5, 0.5));

        for (String line : configHandler.getColoredList("teams.chest.hologram")) {
            DHAPI.addHologramLine(hologram, line
                    .replace("{DURABILITY}", String.valueOf(durability))
                    .replace("{SHIELD-STATUS}", getShieldStatusPlaceHolder())
                    .replace("{TEAM}",
                    getTeamName()));
        }

        hologram.setUpdateInterval(1);
        addPlayerToTeam(leader, getTeamUUID());
        allTeams.add(getTeamUUID());
        addClaimChest(this, claimChestLocation.getChunk());
        addClaim(claimChestLocation.getChunk());
        addMember(leader, PlayerRank.LEADER);
        new FileUtil().createJsonFile(plugin, "data/" + getTeamUUID());
    }

    // Upgrades

    public Map<UpgradeType, Integer> getUpgrades() {
        return upgrades;
    }

    public int getUpgradeLevel(UpgradeType upgrade) {
        return upgrades.get(upgrade);
    }

    public void setUpgradeLevel(UpgradeType upgrade, int level) {
        upgrades.put(upgrade, level);
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

    public void removeDurability(int amount) {
        durability -= amount;
    }

    public void addDurability(int amount) {
        durability += amount;
    }

    // Shield

    public boolean getShieldStatus() {
        return shieldStatus;
    }

    public String getShieldStatusPlaceHolder() {
        return shieldStatus ? "Active" : "Inactive";
    }

    public void setShieldStatus(boolean status) {
        shieldStatus = status;
    }

    // Hologram

    public void enableHologram() {
        hologram.enable();
    }

    public void disableHologram() {
        hologram.disable(DisableCause.API);
    }

    public void removeHologram() {
        DHAPI.removeHologram(hologram.getId());
    }

    public void updateHologram() {
        if (hologram == null || !hologram.isEnabled()) return;

        String durability = String.valueOf(getDurability());
        String shieldStatus = getShieldStatusPlaceHolder();
        String teamName = getTeamName();

        HologramPage page = DHAPI.getHologramPage(hologram, 0);
        assert page != null;
        List<HologramLine> currentLines = page.getLines();

        for (int i = 0; i < currentLines.size(); i++) {

            String template = configHandler.getColoredList("teams.chest.hologram").get(i);

            String updated = template
                    .replace("{DURABILITY}", durability)
                    .replace("{SHIELD-STATUS}", shieldStatus)
                    .replace("{TEAM}", teamName);

            String current = currentLines.get(i).getContent();
            if (!current.equals(updated)) {
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

        if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(playerUUID))) {
            setPlayerRank(Objects.requireNonNull(Bukkit.getPlayer(playerUUID)), rank);
        } else {
            setPlayerRank(Objects.requireNonNull(Bukkit.getOfflinePlayer(playerUUID).getPlayer()), rank);
        }
    }

    public void removeMember(Player player) {
        members.remove(player.getUniqueId());
        allPlayerTeams.remove(player.getUniqueId());
    }

    public Set<UUID> getMembers() {
        return new HashSet<>(members);
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
        return getMembers().size();
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

        if (!teamChat.containsKey(player.getUniqueId())) {
            return false;
        }

        return teamChat.get(player.getUniqueId());
    }

    public boolean isTeamChatEnabled(UUID playerUUID) {

        if (teamChat.containsKey(playerUUID)) {
            return false;
        }

        return teamChat.get(playerUUID);
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

    public Location getClaimChest() {
        return claimChest.clone();
    }

    public void addClaim(Chunk chunk) {
        claims.add(chunk);
        allClaims.put(chunk, getTeamUUID());
    }

    public void removeClaim(Chunk chunk) {
        claims.remove(chunk);
        allClaims.remove(chunk);
    }

    public Set<Chunk> getClaims() {
        return new HashSet<>(claims);
    }

    public int getClaimsCount() {
        return claims.size();
    }

    public boolean ownsClaim(Chunk chunk) {
        return claims.contains(chunk);
    }

    // Static Methods

    protected static final HashMap<UUID, UUID> allPlayerTeams = new HashMap<>();
    protected static final List<UUID> allTeams = new LinkedList<>();
    protected static final Map<UUID, Team> allTeamUUIDS = new HashMap<>();
    protected static final HashMap<Chunk, UUID> allClaims = new HashMap<>();
    protected static final HashMap<Chunk, UUID> allClaimChests = new HashMap<>();

    // Message Team

    public static void messageTeam(UUID teamUUID, String message) {
        for (Player player : getTeam(teamUUID).getOnlineMembers()) {
            player.sendMessage(ChatUtil.color(message));
        }
    }

    // Claims

    public static void addClaim(UUID teamUUID, Chunk chunk) {
        allClaims.put(chunk, teamUUID);
    }

    public static void removeClaim(UUID teamUUID, Chunk chunk) {
        allClaims.remove(chunk, teamUUID);
    }

    public static HashMap<Chunk, UUID> getAllClaims() {
        return allClaims;
    }

    public static UUID getClaim(Chunk chunk) {
        return allClaims.get(chunk);
    }

    public static boolean isClaimed(Chunk chunk) {
        return allClaims.containsKey(chunk);
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

        for (Chunk claim : new HashSet<>(team.getClaims())) {
            allClaims.remove(claim);
        }

        for (UUID memberUUID : new HashSet<>(team.getMembers())) {
            allPlayerTeams.remove(memberUUID);
        }

        allTeams.remove(teamUUID);
        allTeamUUIDS.remove(teamUUID);
        removeClaimChest(team);
        team.removeHologram();

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
        Block block = team.getClaimChest().getBlock();
        block.setType(Material.AIR);
        allClaimChests.remove(team.getClaimChest().getChunk());
    }

    public static Location getClaimChest(Team team) {
        return team.getClaimChest();
    }

    public static void addClaimChest(Team team, Chunk chunk) {
        allClaimChests.put(chunk, team.getTeamUUID());
        addClaim(team.getTeamUUID(), chunk);
    }

    public static boolean containsClaimChest(Chunk chunk) {
        return allClaimChests.containsKey(chunk);
    }

}
