package com.vitaldev.teamsplus.inventories.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.teams.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import com.vitaldev.vitallibs.items.ItemHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ChestMenuInventory {

    private final TeamsPlus plugin;
    private final ConfigHandler configHandler;
    private final ConfigHandler chestHandler;
    private final Team team;
    private final Player player;
    private final InventoryBuilder builder;

    public ChestMenuInventory(TeamsPlus plugin, Player player) {
        this.plugin = plugin;
        this.configHandler = plugin.getConfigFile();
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


    private void setupMenu() {
        String fillerPath = "chest.menu.filler";
        String closePath = "chest.menu.close";

        ItemStack filler = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(chestHandler.getString(fillerPath + ".material"))),
                chestHandler.getMessage(fillerPath + ".name"),
                chestHandler.getInt(fillerPath + ".amount"),
                chestHandler.getStringList(fillerPath + ".lore"),
                chestHandler.getBoolean(fillerPath + ".glow"),
                true
        );

        ItemStack closeButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(chestHandler.getString(closePath + ".material"))),
                chestHandler.getMessage(closePath + ".name"),
                chestHandler.getInt(closePath + ".amount"),
                chestHandler.getStringList(closePath + ".lore"),
                chestHandler.getBoolean(closePath + ".glow"),
                true
        );

        setupItems();

        builder.setCloseButton(closeButton, event -> {
            event.setCancelled(true);
            player.closeInventory();
        });

        builder.fillWithBorderItem(filler);
    }

    private void setupItems() {

        String itemPath;

        for (String item : chestHandler.getConfigurationSection("chest.menu.items").getKeys(false)) {
            itemPath = "chest.menu.items." + item;

            ItemStack itemStack = ItemHandler.buildItem(
                    Objects.requireNonNull(Material.getMaterial(chestHandler.getString(itemPath + ".material"))),
                    chestHandler.getMessage(itemPath + ".name"),
                    chestHandler.getInt(itemPath + ".amount"),
                    chestHandler.getStringList(itemPath + ".lore"),
                    chestHandler.getBoolean(itemPath + ".glow"),
                    true);

            builder.addItem(chestHandler.getInt(itemPath + ".slot"), itemStack, inventoryClickEvent -> {

                if (item.equals("artifacts")) {
                    new ChestArtifactInventory(plugin, player).openInventory();
                }

                if (item.equals("upgrades")) {
                    new ChestUpgradeInventory(plugin, player).openInventory();
                }

                if (item.equals("shield")) {

                }
            });

        }
    }
}
