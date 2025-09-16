package com.vitaldev.teamsplus;

import com.vitaldev.teamsplus.commands.*;
import com.vitaldev.teamsplus.commands.chest.CreateCmd;
import com.vitaldev.teamsplus.commands.chest.FindChestCmd;
import com.vitaldev.teamsplus.commands.ranks.LeaderCmd;
import com.vitaldev.teamsplus.commands.teleport.HomeCmd;
import com.vitaldev.teamsplus.commands.chest.ClaimChestCmd;
import com.vitaldev.teamsplus.commands.ranks.DemoteCmd;
import com.vitaldev.teamsplus.commands.ranks.PromoteCmd;
import com.vitaldev.teamsplus.commands.relation.*;
import com.vitaldev.teamsplus.dependencies.PlaceholderAPIHook;
import com.vitaldev.teamsplus.listeners.TeamChatListener;
import com.vitaldev.teamsplus.listeners.TeamChestListener;
import com.vitaldev.teamsplus.listeners.TeamHomeListener;
import com.vitaldev.teamsplus.teams.Team;
import com.vitaldev.teamsplus.util.ChestUtil;
import com.vitaldev.teamsplus.util.TeamData;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import com.vitaldev.vitallibs.util.FileUtil;
import dev.respark.licensegate.LicenseGate;
import org.bstats.bukkit.Metrics;
import org.bukkit.Chunk;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public final class TeamsPlus extends JavaPlugin {

    TeamData teamData = new TeamData(this);
    ChestUtil chestUtil = new ChestUtil(this);
    private ConfigHandler lang;
    private ConfigHandler config;
    private ConfigHandler chest;
    private ConfigHandler upgrades;
    private ConfigHandler artifacts;
    FileUtil fileUtil = new FileUtil();
    @Override
    public void onEnable() {

        /*

        Bugs:
        - Unloading + saving leader
        Extra:
        - Claim chest recipe config
        - Command Cooldowns
        - Command Sounds
         */

        // Config Folder

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        fileUtil.createTxtFile(this, "license");

        // Register License

        if (new LicenseGate("a1d82")
                .verify(fileUtil.getTextFromFile(this, "license").trim(), "teams-plus")
                .isValid()) {
            ConsoleUtil.sendMessage("  &f|");
            ConsoleUtil.sendMessage("  &f| TeamsPlus - Successfully verified license!");
            ConsoleUtil.sendMessage("  &f| Version - 1.0.0");
            ConsoleUtil.sendMessage("  &f| Vital Development - https://discord.gg/eqyXAH7T2k");
            ConsoleUtil.sendMessage("  &f|");
        } else {
            ConsoleUtil.sendMessage("  &f|");
            ConsoleUtil.sendMessage("  &f| TeamsPlus - Failed to verify license!");
            ConsoleUtil.sendMessage("  &f| Need your license? Join the discord below!");
            ConsoleUtil.sendMessage("  &f| Vital Development - https://discord.gg/eqyXAH7T2k");
            ConsoleUtil.sendMessage("  &f|");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Metrics

        Metrics metrics = new Metrics(this, 23383);

        // Create Default Files

        fileUtil.createFolder(this, "data");

        // Register Commands

        TeamCmd teamCommand = new TeamCmd(this,"team", new String[]{"t", "teams", "Team"}, "Main team command", "teamsplus.base", "teamsplus.admin");
        teamCommand.registerSubCommand(new NameCmd(this));
        teamCommand.registerSubCommand(new ClaimChestCmd(this));
        teamCommand.registerSubCommand(new FindChestCmd(this));
        teamCommand.registerSubCommand(new CreateCmd(this));
        teamCommand.registerSubCommand(new HomeCmd(this));
        teamCommand.registerSubCommand(new HelpCmd(this));
        teamCommand.registerSubCommand(new InviteCmd(this));
        teamCommand.registerSubCommand(new KickCmd(this));
        teamCommand.registerSubCommand(new JoinCmd(this));
        teamCommand.registerSubCommand(new UninviteCmd(this));
        teamCommand.registerSubCommand(new AllyCmd(this));
        teamCommand.registerSubCommand(new UnallyCmd(this));
        teamCommand.registerSubCommand(new PromoteCmd(this));
        teamCommand.registerSubCommand(new DemoteCmd(this));
        teamCommand.registerSubCommand(new DisbandCmd(this));
        teamCommand.registerSubCommand(new LeaderCmd(this));
        teamCommand.registerSubCommand(new LeaveCmd(this));
        teamCommand.registerSubCommand(new ChatCmd(this));
        teamCommand.registerSubCommand(new InfoCmd(this));
        teamCommand.registerSubCommand(new ListCmd(this));
        teamCommand.registerSubCommand(new LocationCmd(this));

        // Dependencies

        new PlaceholderAPIHook(this).register();

        // Register Recipes

        new ChestUtil(this).addCustomRecipe();

        // Register Listeners

        getServer().getPluginManager().registerEvents(new TeamChatListener(this), this);
        getServer().getPluginManager().registerEvents(new TeamChestListener(this), this);
        getServer().getPluginManager().registerEvents(new TeamHomeListener(this),this);

        // Lang File
        fileUtil.createYmlFile(this, "lang.yml");
        fileUtil.createYmlFile(this, "chest.yml");
        fileUtil.createYmlFile(this, "upgrades.yml");
        fileUtil.createYmlFile(this, "artifacts.yml");

        this.lang = new ConfigHandler(this, fileUtil.getYmlFile(this, "lang.yml"));
        this.config = new ConfigHandler(this, fileUtil.getYmlFile(this, "config.yml"));
        this.chest = new ConfigHandler(this, fileUtil.getYmlFile(this, "chest.yml"));
        this.upgrades = new ConfigHandler(this, fileUtil.getYmlFile(this, "upgrades.yml"));
        this.artifacts = new ConfigHandler(this, fileUtil.getYmlFile(this, "artifacts.yml"));


        // Initiate Teams

        File folder = fileUtil.getFolder(this, "data");

        // Ensure the folder exists
        if (!folder.exists() || !folder.isDirectory()) {
            this.getLogger().warning("Team data folder does not exist or is not a directory.");
            return;
        }

        // Filter to get only .json files
        File[] jsonFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        // Check if there are any JSON files
        if (jsonFiles == null || jsonFiles.length == 0) {
            this.getLogger().info("No team files found.");
            return;
        }

        // Iterate over each file and load the team
        for (File file : jsonFiles) {
            try {
                // Extract UUID from the filename (assuming filenames are UUIDs)
                String fileName = file.getName().replace(".json", "");
                UUID teamUUID = UUID.fromString(fileName);

                // Load the team using your loadTeam method
                Team team = teamData.loadTeam(teamUUID);
                Team.addUUID(team.getTeamUUID(), team);
                teamData.loadExtraData(teamUUID);

                for (Chunk chunk : team.getClaims()) {
                    Team.addClaim(team.getTeamUUID(), chunk);
                }

            } catch (IllegalArgumentException e) {
                this.getLogger().warning("Invalid UUID in file: " + file.getName());
            }
        }
    }

    @Override
    public void onDisable() {

        // Remove Recipes

        chestUtil.removeCustomRecipes();

        // Save Teams

        for (UUID uuid : Team.getTeamList()) {
            teamData.saveTeam(Team.getTeam(uuid));
        }

        // Team Holograms

        for (UUID teamUUID: Team.getTeamList()) {
            Team team = Team.getTeam(teamUUID);
            team.removeHologram();
        }

    }
    public void reloadConfiguration() {
        this.lang = new ConfigHandler(this, fileUtil.getYmlFile(this, "lang.yml"));
        this.config = new ConfigHandler(this, fileUtil.getYmlFile(this, "config.yml"));
        this.chest = new ConfigHandler(this, fileUtil.getYmlFile(this, "chest.yml"));
        this.upgrades = new ConfigHandler(this, fileUtil.getYmlFile(this, "upgrades.yml"));
        this.artifacts = new ConfigHandler(this, fileUtil.getYmlFile(this, "artifacts.yml"));
    }

    public ConfigHandler getLangFile() {
        return this.lang;
    }

    public ConfigHandler getConfigFile() {
        return this.config;
    }

    public ConfigHandler getChestFile() { return this.chest; }

    public ConfigHandler getUpgrades() { return this.upgrades;}

    public ConfigHandler getArtifacts() { return this.artifacts; }

}
