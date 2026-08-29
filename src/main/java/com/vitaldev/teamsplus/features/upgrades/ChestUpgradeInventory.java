package com.vitaldev.teamsplus.features.upgrades;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.chest.ChestMenuInventory;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.teamsplus.commands.BypassCmd;
import com.vitaldev.teamsplus.features.permissions.PermissableAction;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import com.vitaldev.vitallibs.items.ItemHandler;
import com.vitaldev.vitallibs.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Objects;

public class ChestUpgradeInventory {

    private final TeamsPlus plugin;
    private final ConfigHandler upgradeHandler;
    private final ConfigHandler langHandler;
    private final Team team;
    private final Player player;
    private final InventoryBuilder builder;

    public ChestUpgradeInventory(TeamsPlus plugin, Player player, Team team) {
        this.plugin = plugin;

        this.langHandler = plugin.getLangFile();
        this.upgradeHandler = plugin.getUpgrades();
        this.team = team;
        this.player = player;
        this.builder = new InventoryBuilder(54,
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
        String backPath = "upgrades.menu.back";


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

        ItemStack backButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(upgradeHandler.getString(backPath + ".material"))),
                upgradeHandler.getMessage(backPath + ".name"),
                upgradeHandler.getInt(backPath + ".amount"),
                upgradeHandler.getStringList(backPath + ".lore"),
                upgradeHandler.getBoolean(backPath + ".glow"),
                true
        );

        builder.setBackButton(backButton, event -> {
            event.setCancelled(true);
            new ChestMenuInventory(plugin, player, team).openInventory();
        });

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

        if (!team.canDo(player, PermissableAction.UPGRADES) && !BypassCmd.isBypassing(player)) {
            player.sendMessage(com.vitaldev.vitallibs.util.ChatUtil.color(plugin.getLangFile().getString("messages.permissions.denied")));
            return;
        }

        int nextLevelCost = getNextLevelCost(upgradeHandler.getString("upgrades.menu.upgrades." + upgrade), level);

        if (!hasEnoughMoney(player, nextLevelCost)) {
            player.sendMessage(langHandler.getMessage("messages.upgrades.not-enough"));
            return;
        }

        UpgradeType type = UpgradeType.valueOf(upgrade.toUpperCase());
        com.vitaldev.teamsplus.events.TeamUpgradeEvent upgradeEvent = new com.vitaldev.teamsplus.events.TeamUpgradeEvent(player, team, type, level, level + 1);
        org.bukkit.Bukkit.getPluginManager().callEvent(upgradeEvent);
        if (upgradeEvent.isCancelled()) return;

        team.setUpgradeLevel(type, level + 1);
        plugin.getEcon().withdrawPlayer(player, nextLevelCost);
        player.sendMessage(langHandler.getMessage("messages.upgrades.upgraded")
                .replace("{LEVEL}", String.valueOf(level + 1))
                .replace("{UPGRADE}", StringUtil.toUpperCaseFirstChar(upgrade.toLowerCase())));

        if (upgrade.equalsIgnoreCase("durability")) {
            team.setDurability(team.getMaxDurability());
            team.updateHologram();
        }
        plugin.getLogManager().logEvent(team, com.vitaldev.teamsplus.features.logs.LogType.UPGRADE_PURCHASE, player, player.getLocation(), Map.of("UPGRADE", upgrade, "LEVEL", String.valueOf(level + 1)));
        player.closeInventory();
    }

    private boolean isAtMaxLevel(int level, int maxLevel) {
        return level >= maxLevel;
    }

    private boolean hasEnoughMoney(Player player, int cost) {
        return plugin.getEcon().getBalance(player) >= cost;
    }

}
