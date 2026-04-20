package com.vitaldev.teamsplus.inventories.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import com.vitaldev.vitallibs.items.ItemHandler;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Objects;

public class ChestClaimInventory {

    private final TeamsPlus plugin;
    private final ConfigHandler chestHandler;
    private final Team team;
    private final Player player;
    private final InventoryBuilder builder;
    private final Chunk chunk;
    private final Vector direction;

    public ChestClaimInventory(TeamsPlus plugin, Player player) {
        this.plugin = plugin;
        this.chestHandler = plugin.getClaims();
        this.team = Team.getTeam(player);
        this.player = player;
        this.direction = player.getLocation().getDirection();
        this.chunk = player.getLocation().getChunk();
        this.builder = new InventoryBuilder(chestHandler.getInt("claims.menu.size"),
                chestHandler.getMessage("claims.menu.title").replace("{TEAM}", team.getTeamName()), false);
    }

    public void openInventory() {
        setupMenu();
        builder.open(player);
    }


    private void setupMenu() {
        String backPath = "claims.menu.back";


        ItemStack backButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(chestHandler.getString(backPath + ".material"))),
                chestHandler.getMessage(backPath + ".name"),
                chestHandler.getInt(backPath + ".amount"),
                chestHandler.getStringList(backPath + ".lore"),
                chestHandler.getBoolean(backPath + ".glow"),
                true
        );

        setupItems();

        builder.setBackButton(backButton, event -> {
            event.setCancelled(true);
            new ChestMenuInventory(plugin, player).openInventory();
        });

    }

    private void setupItems() {


    }
    
}
