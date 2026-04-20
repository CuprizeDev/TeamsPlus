package com.vitaldev.teamsplus.model;

import com.google.gson.*;
import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.teamsplus.features.permissions.PlayerRank;
import com.vitaldev.teamsplus.features.upgrades.UpgradeType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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
        for (Chunk claim : team.getClaims()) {
            String claimString = String.format("%s,%d,%d", claim.getWorld().getName(), claim.getX(), claim.getZ());
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

            return new Team(plugin, teamName, leaderUUID, teamUUID, claimChestLocation);
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

            JsonArray coLeaderArray = teamData.getAsJsonArray("co-leaders");
            for (JsonElement element : coLeaderArray) {
                team.addMember(UUID.fromString(element.getAsString()), PlayerRank.CO_LEADER);
            }

            // Officers

            JsonArray officerArray = teamData.getAsJsonArray("officers");
            for (JsonElement element : officerArray) {
                team.addMember(UUID.fromString(element.getAsString()), PlayerRank.OFFICER);
            }

            // Members

            JsonArray membersArray = teamData.getAsJsonArray("members");
            for (JsonElement element : membersArray) {
                team.addMember(UUID.fromString(element.getAsString()), PlayerRank.MEMBER);
            }

            // Claims

            JsonArray claimsArray = teamData.getAsJsonArray("claims");
            for (JsonElement element : claimsArray) {
                String[] parts = element.getAsString().split(",");
                World world = Bukkit.getWorld(parts[0]);
                int x = Integer.parseInt(parts[1]);
                int z = Integer.parseInt(parts[2]);
                team.addClaim(world.getChunkAt(x, z));
            }

            // Allies

           JsonArray alliesArray = teamData.getAsJsonArray("allies");
           for (JsonElement element : alliesArray) {
               team.addAlly(UUID.fromString(element.getAsString()));
           }

            JsonArray allyRequests = teamData.getAsJsonArray("ally-requests");
            for (JsonElement element : allyRequests) {
                team.addAllyRequest(UUID.fromString(element.getAsString()));
            }

            // Invites

            JsonArray invitesArray = teamData.getAsJsonArray("invites");
            for (JsonElement element : invitesArray) {
                team.addInvite(UUID.fromString(element.getAsString()));
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

        }  catch (IOException e) {
            e.printStackTrace();
        }
    }
}
