package com.vitaldev.teamsplus.model;

import com.google.gson.*;
import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.teamsplus.features.boosters.ActiveBooster;
import com.vitaldev.teamsplus.features.boosters.BoosterType;
import com.vitaldev.teamsplus.features.logs.LogEntry;
import com.vitaldev.teamsplus.features.logs.LogType;
import com.vitaldev.teamsplus.features.permissions.PermissableAction;
import com.vitaldev.teamsplus.features.permissions.PlayerRank;
import com.vitaldev.teamsplus.features.upgrades.UpgradeType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;
import java.util.Map;
import java.util.UUID;

public class TeamData {

    public TeamsPlus plugin;

    public TeamData(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    public void saveTeam(Team team) {

        JsonObject teamData = new JsonObject();

        // Basic Info

        teamData.addProperty("id", team.getTeamUUID().toString());
        teamData.addProperty("name", team.getTeamName());
        if (team.getDiscordLink() != null) {
            teamData.addProperty("discordLink", team.getDiscordLink());
        }
        
        teamData.addProperty("recruitmentEnabled", team.isRecruitmentEnabled());
        teamData.addProperty("teamType", team.getTeamType());
        teamData.addProperty("lookingFor", team.getLookingFor());

        // Ranks

        teamData.addProperty("leader", team.getLeaderUUID().toString());

        JsonArray coLeaderArray = new JsonArray();
        for (UUID member : team.getMembers()) {
            if (team.getPlayerRank(member) == PlayerRank.CO_LEADER) {
                coLeaderArray.add(member.toString());
            }
        }
        teamData.add("co-leaders", coLeaderArray);


        JsonArray officersArray = new JsonArray();
        for (UUID member : team.getMembers()) {
            if (team.getPlayerRank(member) == PlayerRank.OFFICER) {
                officersArray.add(member.toString());
            }
        }
        teamData.add("officers", officersArray);

        JsonArray membersArray = new JsonArray();
        for (UUID member : team.getMembers()) {
            if (team.getPlayerRank(member) == PlayerRank.MEMBER) {
                membersArray.add(member.toString());
            }
        }
        teamData.add("members", membersArray);

        // Invites

        JsonArray invitesArray = new JsonArray();
        for (UUID invite : team.getInvites()) {
            invitesArray.add(invite.toString());
        }
        teamData.add("invites", invitesArray);

        // Allies

        JsonArray alliesArray = new JsonArray();
        for (UUID teamUUID : team.getAllies()) {
            alliesArray.add(teamUUID.toString());
        }
        teamData.add("allies", alliesArray);

        JsonArray alliesRequestsArray = new JsonArray();
        for (UUID teamUUID : team.getAllyRequests()) {
            alliesRequestsArray.add(teamUUID.toString());
        }
        teamData.add("ally-requests", alliesRequestsArray);

        // Claims

        JsonArray claimsArray = new JsonArray();
        for (ClaimKey claim : team.getClaims()) {
            World world = Bukkit.getWorld(claim.worldId());
            String worldId = world != null ? world.getName() : claim.worldId().toString();
            String claimString = String.format("%s,%d,%d", worldId, claim.x(), claim.z());
            claimsArray.add(claimString);
        }
        teamData.add("claims", claimsArray);

        // Claim Chest

        JsonObject claimChestObject = new JsonObject();
        Location claimChestLoc = team.getClaimChest(); // Assuming this returns Location
        claimChestObject.addProperty("world", claimChestLoc.getWorld().getName());
        claimChestObject.addProperty("x", claimChestLoc.getBlockX());
        claimChestObject.addProperty("y", claimChestLoc.getBlockY());
        claimChestObject.addProperty("z", claimChestLoc.getBlockZ());
        teamData.add("claimChest", claimChestObject);

        // Upgrades

        JsonObject upgradesObject = new JsonObject();
        for (Map.Entry<UpgradeType, Integer> entry : team.getUpgrades().entrySet()) {
            upgradesObject.addProperty(entry.getKey().name(), entry.getValue());
        }
        teamData.add("upgrades", upgradesObject);

        // Artifacts

        JsonObject artifactsObject = new JsonObject();
        for (Map.Entry<ArtifactType, Integer> entry : team.getArtifacts().entrySet()) {
            artifactsObject.addProperty(entry.getKey().name(), entry.getValue());
        }
        teamData.add("artifacts", artifactsObject);

        // Permissions

        JsonObject permissionsObject = new JsonObject();
        for (Map.Entry<PlayerRank, Map<PermissableAction, Boolean>> rankEntry : team.getPermissions().entrySet()) {
            JsonObject rankObject = new JsonObject();
            for (Map.Entry<PermissableAction, Boolean> actionEntry : rankEntry.getValue().entrySet()) {
                rankObject.addProperty(actionEntry.getKey().name(), actionEntry.getValue());
            }
            permissionsObject.add(rankEntry.getKey().name(), rankObject);
        }
        teamData.add("permissions", permissionsObject);

        // Boosters
        JsonArray boostersArray = new JsonArray();
        for (ActiveBooster booster : team.getActiveBoosters().values()) {
            JsonObject boosterObject = new JsonObject();
            boosterObject.addProperty("id", booster.getBoosterId());
            boosterObject.addProperty("type", booster.getType().name());
            boosterObject.addProperty("multiplier", booster.getMultiplier());
            boosterObject.addProperty("activator", booster.getActivator().toString());
            boosterObject.addProperty("startTime", booster.getStartTime());
            boosterObject.addProperty("durationMillis", booster.getDurationMillis());
            boostersArray.add(boosterObject);
        }
        teamData.add("boosters", boostersArray);

        // Logs
        JsonObject logsObject = new JsonObject();
        for (Map.Entry<LogType, java.util.LinkedList<LogEntry>> entry : team.getAllLogs().entrySet()) {
            JsonArray typeLogsArray = new JsonArray();
            for (LogEntry logEntry : entry.getValue()) {
                JsonObject logObj = new JsonObject();
                logObj.addProperty("id", logEntry.getLogId().toString());
                logObj.addProperty("timestamp", logEntry.getTimestamp());
                logObj.addProperty("player", logEntry.getPlayerUUID() != null ? logEntry.getPlayerUUID().toString() : "");
                logObj.addProperty("location", logEntry.getLocationStr() != null ? logEntry.getLocationStr() : "");
                
                JsonObject metaObj = new JsonObject();
                if (logEntry.getMetadata() != null) {
                    for (Map.Entry<String, String> meta : logEntry.getMetadata().entrySet()) {
                        metaObj.addProperty(meta.getKey(), meta.getValue());
                    }
                }
                logObj.add("metadata", metaObj);
                typeLogsArray.add(logObj);
            }
            logsObject.add(entry.getKey().name(), typeLogsArray);
        }
        teamData.add("logs", logsObject);

        // Shield
        JsonObject shieldObject = new JsonObject();
        shieldObject.addProperty("chargeSeconds", team.getShieldChargeSeconds());
        shieldObject.addProperty("autoEnabled", team.isAutoShieldEnabled());
        shieldObject.addProperty("deploying", team.isShieldDeploying());
        shieldObject.addProperty("deployStartTime", team.getShieldDeployStartTime());
        shieldObject.addProperty("manualDeploy", team.isShieldManualDeploy());
        shieldObject.addProperty("active", team.isShieldActive());
        shieldObject.addProperty("activationTime", team.getShieldActivationTime());
        shieldObject.addProperty("cooldownEndTime", team.getShieldCooldownEndTime());
        teamData.add("shield", shieldObject);
        
        // Warps
        JsonObject warpsObject = new JsonObject();
        for (Map.Entry<String, Location> entry : team.getWarps().entrySet()) {
            Location loc = entry.getValue();
            String locString = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
            warpsObject.addProperty(entry.getKey(), locString);
        }
        teamData.add("warps", warpsObject);
        
        // Vault
        JsonArray vaultArray = new JsonArray();
        for (int i = 0; i < team.getVault().length; i++) {
            org.bukkit.inventory.ItemStack item = team.getVault()[i];
            if (item != null) {
                JsonObject itemObj = new JsonObject();
                itemObj.addProperty("slot", i);
                itemObj.addProperty("data", com.vitaldev.vitallibs.items.ItemSerializer.encodeToBase64(item));
                vaultArray.add(itemObj);
            }
        }
        teamData.add("vault", vaultArray);

        File file = new File(plugin.getDataFolder(), "data/" + team.getTeamUUID().toString() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(teamData));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Team loadTeam(UUID teamUUID) {
        File file = new File(plugin.getDataFolder(), "data/" + teamUUID.toString() + ".json");

        if (!file.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject teamData = JsonParser.parseReader(reader).getAsJsonObject();

            // Basic Info

            String teamName = teamData.get("name").getAsString();
            UUID leaderUUID = UUID.fromString(teamData.get("leader").getAsString());

            // Claim Chest

            JsonObject claimChestObject = teamData.getAsJsonObject("claimChest");
            World chestWorld = Bukkit.getWorld(claimChestObject.get("world").getAsString());
            int x = claimChestObject.get("x").getAsInt();
            int y = claimChestObject.get("y").getAsInt();
            int z = claimChestObject.get("z").getAsInt();
            Location claimChestLocation = new Location(chestWorld, x, y, z);

                        Team loadedTeam = new Team(plugin, teamName, leaderUUID, teamUUID, claimChestLocation);
            if (teamData.has("discordLink")) {
                loadedTeam.setDiscordLink(teamData.get("discordLink").getAsString());
            }
            if (teamData.has("recruitmentEnabled")) {
                loadedTeam.setRecruitmentEnabled(teamData.get("recruitmentEnabled").getAsBoolean());
            }
            if (teamData.has("teamType")) {
                loadedTeam.setTeamType(teamData.get("teamType").getAsString());
            }
            if (teamData.has("lookingFor")) {
                loadedTeam.setLookingFor(teamData.get("lookingFor").getAsString());
            }
            return loadedTeam;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadExtraData(UUID teamUUID) {
        File file = new File(plugin.getDataFolder(), "data/" + teamUUID.toString() + ".json");
        Team team = Team.getTeam(teamUUID);
        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject teamData = JsonParser.parseReader(reader).getAsJsonObject();

            // Co Leaders

            if (teamData.has("co-leaders") && !teamData.get("co-leaders").isJsonNull()) {
                JsonArray coLeaderArray = teamData.getAsJsonArray("co-leaders");
                for (JsonElement element : coLeaderArray) {
                    team.addMember(UUID.fromString(element.getAsString()), PlayerRank.CO_LEADER);
                }
            }

            // Officers

            if (teamData.has("officers") && !teamData.get("officers").isJsonNull()) {
                JsonArray officerArray = teamData.getAsJsonArray("officers");
                for (JsonElement element : officerArray) {
                    team.addMember(UUID.fromString(element.getAsString()), PlayerRank.OFFICER);
                }
            }

            // Members

            if (teamData.has("members") && !teamData.get("members").isJsonNull()) {
                JsonArray membersArray = teamData.getAsJsonArray("members");
                for (JsonElement element : membersArray) {
                    team.addMember(UUID.fromString(element.getAsString()), PlayerRank.MEMBER);
                }
            }

            // Claims

            if (teamData.has("claims") && !teamData.get("claims").isJsonNull()) {
                JsonArray claimsArray = teamData.getAsJsonArray("claims");
                for (JsonElement element : claimsArray) {
                    try {
                        String[] parts = element.getAsString().split(",");
                        org.bukkit.World world = getWorld(parts[0]);
                        int x = Integer.parseInt(parts[1]);
                        int z = Integer.parseInt(parts[2]);
                        if (world != null) {
                            // Use ClaimKey to avoid calling world.getChunkAt() which would
                            // synchronously load chunks from disk during startup.
                            team.addClaim(new com.vitaldev.teamsplus.model.ClaimKey(world.getUID(), x, z));
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Skipping malformed claim entry for team " + teamUUID + ": " + e.getMessage());
                    }
                }
            }

            // Allies

            if (teamData.has("allies") && !teamData.get("allies").isJsonNull()) {
               JsonArray alliesArray = teamData.getAsJsonArray("allies");
               for (JsonElement element : alliesArray) {
                   team.addAlly(UUID.fromString(element.getAsString()));
               }
            }

            if (teamData.has("ally-requests") && !teamData.get("ally-requests").isJsonNull()) {
                JsonArray allyRequests = teamData.getAsJsonArray("ally-requests");
                for (JsonElement element : allyRequests) {
                    team.addAllyRequest(UUID.fromString(element.getAsString()));
                }
            }

            // Invites

            if (teamData.has("invites") && !teamData.get("invites").isJsonNull()) {
                JsonArray invitesArray = teamData.getAsJsonArray("invites");
                for (JsonElement element : invitesArray) {
                    team.addInvite(UUID.fromString(element.getAsString()));
                }
            }

            // Artifacts

            JsonObject artifactsObject = teamData.getAsJsonObject("artifacts");

            if (artifactsObject != null) {
                for (Map.Entry<String, JsonElement> entry : artifactsObject.entrySet()) {
                    String artifactName = entry.getKey();
                    int value = entry.getValue().getAsInt();
                    team.setArtifactSlot(ArtifactType.fromString(artifactName), value);
                }
            }

            // Upgrades

            JsonObject upgradesObject = teamData.getAsJsonObject("upgrades");

            if (upgradesObject != null) {
                for (Map.Entry<String, JsonElement> entry : upgradesObject.entrySet()) {
                    String upgradeName = entry.getKey();
                    int value = entry.getValue().getAsInt();
                    team.setUpgradeLevel(UpgradeType.valueOf(upgradeName), value);
                }
            }

            // Permissions

            JsonObject permissionsObject = teamData.getAsJsonObject("permissions");
            if (permissionsObject != null) {
                for (Map.Entry<String, JsonElement> rankEntry : permissionsObject.entrySet()) {
                    try {
                        PlayerRank rank = PlayerRank.valueOf(rankEntry.getKey());
                        JsonObject rankObject = rankEntry.getValue().getAsJsonObject();
                        for (Map.Entry<String, JsonElement> actionEntry : rankObject.entrySet()) {
                            try {
                                PermissableAction action = PermissableAction.valueOf(actionEntry.getKey());
                                boolean value = actionEntry.getValue().getAsBoolean();
                                team.setPermission(rank, action, value);
                            } catch (IllegalArgumentException ignored) {
                                // Unknown action key in JSON — skip gracefully
                            }
                        }
                    } catch (IllegalArgumentException ignored) {
                        // Unknown rank key in JSON — skip gracefully
                    }
                }
            }

            // Boosters

            JsonArray boostersArray = teamData.getAsJsonArray("boosters");
            if (boostersArray != null) {
                for (JsonElement element : boostersArray) {
                    try {
                        JsonObject boosterObject = element.getAsJsonObject();
                        String id = boosterObject.get("id").getAsString();
                        BoosterType type = BoosterType.valueOf(boosterObject.get("type").getAsString());
                        double multiplier = boosterObject.get("multiplier").getAsDouble();
                        UUID activator = UUID.fromString(boosterObject.get("activator").getAsString());
                        long startTime = boosterObject.get("startTime").getAsLong();
                        long durationMillis = boosterObject.get("durationMillis").getAsLong();
                        ActiveBooster activeBooster = new ActiveBooster(id, type, multiplier, activator, startTime, durationMillis);
                        team.addActiveBooster(activeBooster);
                    } catch (Exception ignored) {
                        // Ignore malformed booster data
                    }
                }
            }

            // Logs

            JsonObject logsObject = teamData.getAsJsonObject("logs");
            if (logsObject != null) {
                for (Map.Entry<String, JsonElement> entry : logsObject.entrySet()) {
                    try {
                        LogType type = LogType.valueOf(entry.getKey());
                        JsonArray logsArray = entry.getValue().getAsJsonArray();
                        
                        // We iterate in reverse so the oldest saved logs are added first, 
                        // maintaining correct order when addLog uses addFirst.
                        for (int i = logsArray.size() - 1; i >= 0; i--) {
                            try {
                                JsonObject logObj = logsArray.get(i).getAsJsonObject();
                                UUID logId = UUID.fromString(logObj.get("id").getAsString());
                                long timestamp = logObj.get("timestamp").getAsLong();
                                
                                String pStr = logObj.get("player").getAsString();
                                UUID playerUUID = pStr.isEmpty() ? null : UUID.fromString(pStr);
                                
                                String locStr = logObj.get("location").getAsString();
                                
                                java.util.Map<String, String> metadata = new java.util.HashMap<>();
                                JsonObject metaObj = logObj.getAsJsonObject("metadata");
                                if (metaObj != null) {
                                    for (Map.Entry<String, JsonElement> meta : metaObj.entrySet()) {
                                        metadata.put(meta.getKey(), meta.getValue().getAsString());
                                    }
                                }
                                
                                team.addLog(new LogEntry(logId, type, timestamp, playerUUID, locStr, metadata));
                            } catch (Exception ignored) {
                                // Skip malformed individual log
                            }
                        }
                    } catch (Exception ignored) {
                        // Skip malformed log type
                    }
                }
            }

            // Shield

            JsonObject shieldObject = teamData.getAsJsonObject("shield");
            if (shieldObject != null) {
                try {
                    if (shieldObject.has("chargeSeconds"))
                        team.setShieldChargeSeconds(shieldObject.get("chargeSeconds").getAsLong());
                    if (shieldObject.has("autoEnabled"))
                        team.setAutoShieldEnabled(shieldObject.get("autoEnabled").getAsBoolean());
                    if (shieldObject.has("deploying"))
                        team.setShieldDeploying(shieldObject.get("deploying").getAsBoolean());
                    if (shieldObject.has("deployStartTime"))
                        team.setShieldDeployStartTime(shieldObject.get("deployStartTime").getAsLong());
                    if (shieldObject.has("manualDeploy"))
                        team.setShieldManualDeploy(shieldObject.get("manualDeploy").getAsBoolean());
                    if (shieldObject.has("active"))
                        team.setShieldActive(shieldObject.get("active").getAsBoolean());
                    if (shieldObject.has("activationTime"))
                        team.setShieldActivationTime(shieldObject.get("activationTime").getAsLong());
                    if (shieldObject.has("cooldownEndTime"))
                        team.setShieldCooldownEndTime(shieldObject.get("cooldownEndTime").getAsLong());
                } catch (Exception ignored) {
                    // Skip malformed shield data
                }
            }

        }  catch (IOException e) {
            e.printStackTrace();
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject teamData = JsonParser.parseReader(reader).getAsJsonObject();
            // Warps
            JsonObject warpsObject = teamData.getAsJsonObject("warps");
            if (warpsObject != null) {
                for (Map.Entry<String, JsonElement> entry : warpsObject.entrySet()) {
                    try {
                        String[] parts = entry.getValue().getAsString().split(",");
                        World w = Bukkit.getWorld(parts[0]);
                        if (w != null) {
                            Location loc = new Location(w, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
                            team.setWarp(entry.getKey(), loc);
                        }
                    } catch (Exception ignored) {}
                }
            }

            // Vault
            JsonArray vaultArray = teamData.getAsJsonArray("vault");
            if (vaultArray != null) {
                for (JsonElement element : vaultArray) {
                    try {
                        JsonObject itemObj = element.getAsJsonObject();
                        int slot = itemObj.get("slot").getAsInt();
                        org.bukkit.inventory.ItemStack item = com.vitaldev.vitallibs.items.ItemSerializer.decodeFromBase64(itemObj.get("data").getAsString());
                        team.setVaultItem(slot, item);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
    }

    private World getWorld(String worldNameOrId) {
        World world = Bukkit.getWorld(worldNameOrId);
        if (world != null) {
            return world;
        }

        try {
            return Bukkit.getWorld(UUID.fromString(worldNameOrId));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
