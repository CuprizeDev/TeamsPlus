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
import com.vitaldev.teamsplus.dependencies.DependencyManager;
import com.vitaldev.teamsplus.features.artifacts.ArtifactCmd;
import com.vitaldev.teamsplus.features.boosters.BoosterCmd;
import com.vitaldev.teamsplus.listeners.*;
import com.vitaldev.teamsplus.features.chest.ChestListener;
import com.vitaldev.teamsplus.features.artifacts.ArtifactManager;
import com.vitaldev.teamsplus.features.boosters.BoosterItemBuilder;
import com.vitaldev.teamsplus.features.boosters.BoosterManager;
import com.vitaldev.teamsplus.features.boosters.listeners.BoosterListener;
import com.vitaldev.teamsplus.features.chest.ChestManager;
import com.vitaldev.teamsplus.features.permissions.PermissionListener;
import com.vitaldev.teamsplus.listeners.TeamChatListener;
import com.vitaldev.teamsplus.features.logs.LogManager;
import com.vitaldev.teamsplus.features.shield.ShieldListener;
import com.vitaldev.teamsplus.features.shield.ShieldManager;
import com.vitaldev.teamsplus.features.leaderboard.LeaderboardCache;
import com.vitaldev.teamsplus.features.leaderboard.LeaderboardService;
import com.vitaldev.teamsplus.model.ClaimKey;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.teamsplus.model.TeamData;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import com.vitaldev.vitallibs.util.FileUtil;
import dev.respark.licensegate.LicenseGate;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
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
    private ConfigHandler permissions;
    private ConfigHandler logs;
    private ConfigHandler shield;
    private ConfigHandler leaderboard;
    private ConfigHandler stats;
    private ArtifactManager artifactManager;
    private com.vitaldev.teamsplus.features.stats.StatManager statManager;
    private com.vitaldev.teamsplus.features.artifacts.ArtifactItemBuilder artifactItemBuilder;
    private BoosterManager boosterManager;
    private BoosterItemBuilder boosterItemBuilder;
    private LogManager logManager;
    private ShieldManager shieldManager;
    private LeaderboardCache leaderboardCache;
    private LeaderboardService leaderboardService;
    private DependencyManager dependencyManager;
    FileUtil fileUtil = new FileUtil();
    ChestManager chestUtil = new ChestManager(this);
    private final String ADMIN_PERM = "teamsplus.admin.*";

    @Override
    public void onEnable() {

        // Bugs:
// - Unloading + saving leader
// Extra:
// - Claim chest recipe config
// - Command Cooldowns
// - Command Sounds

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
        fileUtil.createYmlFile(this, "permissions.yml");
        fileUtil.createYmlFile(this, "boosters.yml");
        fileUtil.createYmlFile(this, "logs.yml");
        fileUtil.createYmlFile(this, "shield.yml");
        fileUtil.createYmlFile(this, "leaderboard.yml");
        fileUtil.createYmlFile(this, "stats.yml");

        this.lang = new ConfigHandler(this, fileUtil.getYmlFile(this, "lang.yml"));
        this.config = new ConfigHandler(this, fileUtil.getYmlFile(this, "config.yml"));
        this.chest = new ConfigHandler(this, fileUtil.getYmlFile(this, "chest.yml"));
        this.upgrades = new ConfigHandler(this, fileUtil.getYmlFile(this, "upgrades.yml"));
        this.artifacts = new ConfigHandler(this, fileUtil.getYmlFile(this, "artifacts.yml"));
        this.claims = new ConfigHandler(this, fileUtil.getYmlFile(this, "claims.yml"));
        this.permissions = new ConfigHandler(this, fileUtil.getYmlFile(this, "permissions.yml"));
        this.logs = new ConfigHandler(this, fileUtil.getYmlFile(this, "logs.yml"));
        this.shield = new ConfigHandler(this, fileUtil.getYmlFile(this, "shield.yml"));
        this.leaderboard = new ConfigHandler(this, fileUtil.getYmlFile(this, "leaderboard.yml"));
        this.stats = new ConfigHandler(this, fileUtil.getYmlFile(this, "stats.yml"));

        // Artifact Logic

        artifactManager = new ArtifactManager(this);
        artifactManager.loadDefinitions();
        artifactItemBuilder = new com.vitaldev.teamsplus.features.artifacts.ArtifactItemBuilder(this);

        // Stats Logic
        statManager = new com.vitaldev.teamsplus.features.stats.StatManager(this);
        statManager.loadDefinitions();

        // Booster Logic

        boosterManager = new BoosterManager(this);
        boosterItemBuilder = new BoosterItemBuilder(this);

        // Log Logic
        
        logManager = new LogManager(this);

        // Shield Logic

        shieldManager = new ShieldManager(this);

        // Leaderboard Logic
        leaderboardCache = new LeaderboardCache();
        leaderboardService = new LeaderboardService(this, leaderboardCache);

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
        teamCommand.registerSubCommand(new FriendlyFireCmd(this));
        teamCommand.registerSubCommand(new VersionCmd(this));
        teamCommand.registerSubCommand(new ReloadCmd(this));
        teamCommand.registerSubCommand(new BypassCmd(this));
        teamCommand.registerSubCommand(new StatsCmd(this));

        teamCommand.registerSubCommand(new TopCmd(this));
        new ArtifactCmd(this,
                "artifact",
                new String[]{"artifacts", "arti"},
                "Main command for artifacts",
                "teamsplus.artifacts.*");
        
        new BoosterCmd(this,
                "booster",
                new String[]{"boosters", "boost"},
                "Main command for boosters",
                "teamsplus.admin");

        // Dependencies (Vault, PlaceholderAPI, RoseStacker, DiscordSRV)

        dependencyManager = new DependencyManager(this);

        // Register Listeners

        getServer().getPluginManager().registerEvents(new TeamChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestListener(this), this);
        getServer().getPluginManager().registerEvents(new TeamHomeListener(this),this);
        getServer().getPluginManager().registerEvents(new PermissionListener(this), this);
        getServer().getPluginManager().registerEvents(new BoosterListener(this), this);
        getServer().getPluginManager().registerEvents(new com.vitaldev.teamsplus.features.boosters.listeners.CropListener(this), this);
        getServer().getPluginManager().registerEvents(new com.vitaldev.teamsplus.features.boosters.listeners.ExpListener(this), this);
        getServer().getPluginManager().registerEvents(new ShieldListener(this), this);
        getServer().getPluginManager().registerEvents(new com.vitaldev.teamsplus.features.stats.StatListener(this), this);

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

                for (ClaimKey chunk : team.getClaims()) {
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

        // Save Players
        com.vitaldev.teamsplus.model.TeamPlayer.saveAll();

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
        this.permissions = new ConfigHandler(this, fileUtil.getYmlFile(this, "permissions.yml"));
        this.logs = new ConfigHandler(this, fileUtil.getYmlFile(this, "logs.yml"));
        this.shield = new ConfigHandler(this, fileUtil.getYmlFile(this, "shield.yml"));
        this.leaderboard = new ConfigHandler(this, fileUtil.getYmlFile(this, "leaderboard.yml"));
        this.stats = new ConfigHandler(this, fileUtil.getYmlFile(this, "stats.yml"));
        
        if (this.statManager != null) {
            this.statManager.reload();
        }
    }

    public ArtifactManager getArtifactManager() {
        return artifactManager;
    }

    public com.vitaldev.teamsplus.features.artifacts.ArtifactItemBuilder getArtifactItemBuilder() {
        return artifactItemBuilder;
    }

    public BoosterManager getBoosterManager() {
        return boosterManager;
    }

    public BoosterItemBuilder getBoosterItemBuilder() {
        return boosterItemBuilder;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public ShieldManager getShieldManager() {
        return shieldManager;
    }

    public com.vitaldev.teamsplus.features.stats.StatManager getStatManager() {
        return statManager;
    }

    public LeaderboardCache getLeaderboardCache() { return leaderboardCache; }
    public LeaderboardService getLeaderboardService() { return leaderboardService; }

    public DependencyManager getDependencyManager() { return dependencyManager; }

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

    public ConfigHandler getPermissionsFile() { return this.permissions; }

    public ConfigHandler getLogsFile() { return this.logs; }

    public ConfigHandler getShieldFile() { return this.shield; }

    public ConfigHandler getLeaderboardFile() { return this.leaderboard; }

    public ConfigHandler getStatsFile() { return this.stats; }

    public String getAdminPermission() {
        return this.ADMIN_PERM;
    }

    public Economy getEcon() {
        return dependencyManager != null ? dependencyManager.getEconomy() : null;
    }
}
