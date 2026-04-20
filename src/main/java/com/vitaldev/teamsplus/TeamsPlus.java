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
import com.vitaldev.teamsplus.dependencies.DiscordSRVHook;
import com.vitaldev.teamsplus.dependencies.PlaceholderAPIHook;
import com.vitaldev.teamsplus.listeners.TeamChatListener;
import com.vitaldev.teamsplus.features.chest.ChestListener;
import com.vitaldev.teamsplus.listeners.TeamHomeListener;
import com.vitaldev.teamsplus.features.artifacts.ArtifactItemBuilder;
import com.vitaldev.teamsplus.features.artifacts.ArtifactManager;
import com.vitaldev.teamsplus.features.chest.ChestManager;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.teamsplus.model.TeamData;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import com.vitaldev.vitallibs.util.FileUtil;
import dev.respark.licensegate.LicenseGate;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Chunk;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public final class TeamsPlus extends JavaPlugin {

    TeamData teamData = new TeamData(this);
    private ConfigHandler lang;
    private ConfigHandler config;
    private ConfigHandler chest;
    private ConfigHandler upgrades;
    private ConfigHandler artifacts;
    private ConfigHandler claims;
    private ArtifactManager artifactManager;
    private ArtifactItemBuilder artifactItemBuilder;
    FileUtil fileUtil = new FileUtil();
    private static Economy eco;
    ChestManager chestUtil = new ChestManager(this);
    private final String ADMIN_PERM = "teamsplus.admin.*";

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

        // YML Files

        fileUtil.createYmlFile(this, "lang.yml");
        fileUtil.createYmlFile(this, "chest.yml");
        fileUtil.createYmlFile(this, "upgrades.yml");
        fileUtil.createYmlFile(this, "artifacts.yml");
        fileUtil.createYmlFile(this, "claims.yml");

        this.lang = new ConfigHandler(this, fileUtil.getYmlFile(this, "lang.yml"));
        this.config = new ConfigHandler(this, fileUtil.getYmlFile(this, "config.yml"));
        this.chest = new ConfigHandler(this, fileUtil.getYmlFile(this, "chest.yml"));
        this.upgrades = new ConfigHandler(this, fileUtil.getYmlFile(this, "upgrades.yml"));
        this.artifacts = new ConfigHandler(this, fileUtil.getYmlFile(this, "artifacts.yml"));
        this.claims = new ConfigHandler(this, fileUtil.getYmlFile(this, "claims.yml"));

        // Artifact Logic

        artifactManager = new ArtifactManager(this);
        artifactManager.loadDefinitions();
        artifactItemBuilder = new ArtifactItemBuilder(this);

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

        new ArtifactCmd(this,
                "artifact",
                new String[]{"artifacts", "arti"},
                "Main command for artifacts",
                "teamsplus.artifacts.*");

        // Dependencies

        new PlaceholderAPIHook(this).register();
        new DiscordSRVHook(this).initiateDiscordSRV();

        // Register Listeners

        getServer().getPluginManager().registerEvents(new TeamChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestListener(this), this);
        getServer().getPluginManager().registerEvents(new TeamHomeListener(this),this);

        // Eco

        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                eco = rsp.getProvider();
            }
        }

        // Register Recipes

        new ChestManager(this).registerRecipes();

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

                String fileName = file.getName().replace(".json", "");
                UUID teamUUID = UUID.fromString(fileName);

                Team team = teamData.loadTeam(teamUUID);
                Team.addUUID(team.getTeamUUID(), team);
                teamData.loadExtraData(teamUUID);
                team.setDurability(team.getMaxDurability());
                team.updateHologram();

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

        chestUtil.removeRecipes();

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
        this.claims = new ConfigHandler(this, fileUtil.getYmlFile(this, "claims.yml"));
    }

    public ArtifactManager getArtifactManager() {
        return artifactManager;
    }

    public ArtifactItemBuilder getArtifactItemBuilder() {
        return artifactItemBuilder;
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

    public ConfigHandler getClaims() { return this.claims; }

    public String getAdminPermission() {
        return this.ADMIN_PERM;
    }

    public Economy getEcon() {
        return eco;
    }
}
