package com.vitaldev.teamsplus.inventories.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.teams.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import com.vitaldev.vitallibs.items.ItemHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ChestUpgradeInventory {

    private final TeamsPlus plugin;
    private final ConfigHandler upgradeHandler;
    private final Team team;
    private final Player player;
    private final InventoryBuilder builder;

    public ChestUpgradeInventory(TeamsPlus plugin, Player player) {
        this.plugin = plugin;

        this.upgradeHandler = plugin.getUpgrades();
        this.team = Team.getTeam(player);
        this.player = player;
        this.builder = new InventoryBuilder(upgradeHandler.getInt("upgrades.menu.size"),
                upgradeHandler.getMessage("upgrades.menu.title").replace("{TEAM}", team.getTeamName()));
    }

    public void openInventory() {
        setupMenu();
        setupItems();
        builder.open(player);
    }

    private void setupMenu() {
        String fillerPath = "upgrades.menu.filler";
        String closePath = "upgrades.menu.close";

        ItemStack filler = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(upgradeHandler.getString(fillerPath + ".material"))),
                upgradeHandler.getMessage(fillerPath + ".name"),
                upgradeHandler.getInt(fillerPath + ".amount"),
                upgradeHandler.getStringList(fillerPath + ".lore"),
                upgradeHandler.getBoolean(fillerPath + ".glow"),
                true
        );

        ItemStack closeButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(upgradeHandler.getString(closePath + ".material"))),
                upgradeHandler.getMessage(closePath + ".name"),
                upgradeHandler.getInt(closePath + ".amount"),
                upgradeHandler.getStringList(closePath + ".lore"),
                upgradeHandler.getBoolean(closePath + ".glow"),
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

        for (String upgrade : upgradeHandler.getConfigurationSection("upgrades.menu.upgrades").getKeys(false)) {
            itemPath = "upgrades.menu.upgrades." + upgrade;

            ItemStack itemStack = ItemHandler.buildItem(
                    Objects.requireNonNull(Material.getMaterial(upgradeHandler.getString(itemPath + ".item.material"))),
                    upgradeHandler.getMessage(itemPath + ".item.name"),
                    upgradeHandler.getInt(itemPath + ".item.amount"),
                    upgradeHandler.getStringList(itemPath + ".item.lore"),
                    upgradeHandler.getBoolean(itemPath + ".item.glow"),
                    true);

            builder.addItem(upgradeHandler.getInt(itemPath + ".item.slot"), itemStack, inventoryClickEvent -> {



            });
        }
    }
}
