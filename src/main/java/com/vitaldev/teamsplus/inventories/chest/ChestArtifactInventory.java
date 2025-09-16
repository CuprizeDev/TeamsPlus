package com.vitaldev.teamsplus.inventories.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.teams.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import org.bukkit.entity.Player;

public class ChestArtifactInventory {

    private final TeamsPlus plugin;
    private final ConfigHandler chestHandler;
    private final Team team;
    private final Player player;
    private final InventoryBuilder builder;

    public ChestArtifactInventory(TeamsPlus plugin, Player player) {
            this.plugin = plugin;
            this.chestHandler = plugin.getChestFile();
            this.team = Team.getTeam(player);
            this.player = player;
            this.builder = new InventoryBuilder(chestHandler.getInt("chest.menu.size"),
                    chestHandler.getMessage("chest.menu.title").replace("{TEAM}", team.getTeamName()));
    }


    public void openInventory() {
        setupMenu();
        setupItems();
        builder.open(player);
    }

    public void setupMenu() {

    }

    public void setupItems() {

    }

}
