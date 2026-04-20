package com.vitaldev.teamsplus.inventories.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.teamsplus.features.upgrades.UpgradeType;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import com.vitaldev.vitallibs.items.ItemHandler;
import com.vitaldev.vitallibs.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.Objects;

public class ChestUpgradeInventory {

    private final TeamsPlus plugin;
    private final ConfigHandler upgradeHandler;
    private final ConfigHandler langHandler;
    private final Team team;
    private final Player player;
    private final InventoryBuilder builder;

    public ChestUpgradeInventory(TeamsPlus plugin, Player player) {
        this.plugin = plugin;

        this.langHandler = plugin.getLangFile();
        this.upgradeHandler = plugin.getUpgrades();
        this.team = Team.getTeam(player);
        this.player = player;
        this.builder = new InventoryBuilder(upgradeHandler.getInt("upgrades.menu.size"),
                upgradeHandler.getMessage("upgrades.menu.title").replace("{TEAM}", team.getTeamName()), false);
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
            UpgradeType upgradeType = UpgradeType.valueOf(upgrade.toUpperCase());
            int level = team.getUpgradeLevel(upgradeType);
            String finalItemPath = itemPath;

            ItemStack itemStack = ItemHandler.buildItem(
                    Objects.requireNonNull(Material.getMaterial(upgradeHandler.getString(itemPath + ".item.material"))),
                    upgradeHandler.getMessage(itemPath + ".item.name"),
                    upgradeHandler.getInt(itemPath + ".item.amount"),
                    upgradeHandler.getStringList(itemPath + ".item.lore").stream()
                            .map(line -> line
                                    .replace("{PRICE}", NumberFormat.getInstance().format(upgradeHandler.getInt(finalItemPath + ".levels." + (level+1) + ".cost")))
                                    .replace("{LEVEL}", String.valueOf(level+1)))
                            .toList(),
                    upgradeHandler.getBoolean(finalItemPath + ".item.glow"),
                    true);
                            builder.addItem(upgradeHandler.getInt(itemPath + ".item.slot"), itemStack, inventoryClickEvent -> {
                                handlePurchase(player,
                                        upgrade,
                                        level,
                                        upgradeHandler.getConfigurationSection(finalItemPath + ".levels").getKeys(false).size());
                inventoryClickEvent.setCancelled(true);
            });
        }
    }

    private int getNextLevelCost(String path, int level) {
        return upgradeHandler.getInt(path + ".levels." + (level + 1) + ".cost");
    }

    public void handlePurchase(Player player, String upgrade, int level, int maxLevel) {

        if (isAtMaxLevel(level, maxLevel)) {
            player.sendMessage(langHandler.getMessage("messages.upgrades.maximum-level"));
            return;
        }

        int nextLevelCost = getNextLevelCost(upgradeHandler.getString("upgrades.menu.upgrades." + upgrade), level);

        if (!hasEnoughMoney(player, nextLevelCost)) {
            player.sendMessage(langHandler.getMessage("messages.upgrades.not-enough"));
            return;
        }

        team.setUpgradeLevel(UpgradeType.valueOf(upgrade.toUpperCase()), level + 1);
        plugin.getEcon().withdrawPlayer(player, nextLevelCost);
        player.sendMessage(langHandler.getMessage("messages.upgrades.upgraded")
                .replace("{LEVEL}", String.valueOf(level + 1))
                .replace("{UPGRADE}", StringUtil.toUpperCaseFirstChar(upgrade.toLowerCase())));

        if (upgrade.equalsIgnoreCase("durability")) {
            team.setDurability(team.getMaxDurability());
            team.updateHologram();
        }
        player.closeInventory();
    }

    private boolean isAtMaxLevel(int level, int maxLevel) {
        return level >= maxLevel;
    }

    private boolean hasEnoughMoney(Player player, int cost) {
        return plugin.getEcon().getBalance(player) >= cost;
    }

}
