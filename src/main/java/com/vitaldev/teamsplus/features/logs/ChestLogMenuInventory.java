package com.vitaldev.teamsplus.features.logs;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.chest.ChestMenuInventory;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import com.vitaldev.vitallibs.items.ItemHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ChestLogMenuInventory {

    private final TeamsPlus plugin;
    private final ConfigHandler logConfig;
    private final Team team;
    private final Player player;
    private final InventoryBuilder builder;
    private final ConfigHandler langHandler;

    public ChestLogMenuInventory(TeamsPlus plugin, Player player) {
        this.plugin = plugin;
        this.logConfig = plugin.getLogManager().getConfig();
        this.langHandler = plugin.getLangFile();
        this.team = Team.getTeam(player);
        this.player = player;
        this.builder = new InventoryBuilder(logConfig.getInt("logs.menu-categories.size"),
                logConfig.getMessage("logs.menu-categories.title").replace("{TEAM}", team.getTeamName()), true);
    }

    public void openInventory() {
        setupMenu();
        setupCategories();
        builder.open(player);
    }

    public void setupMenu() {
        String fillerPath = "logs.menu-categories.filler";
        String closePath = "logs.menu-categories.close";
        String backPath = "logs.menu-categories.back";

        ItemStack filler = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(logConfig.getString(fillerPath + ".material"))),
                logConfig.getMessage(fillerPath + ".name"),
                logConfig.getInt(fillerPath + ".amount"),
                logConfig.getStringList(fillerPath + ".lore"),
                logConfig.getBoolean(fillerPath + ".glow"),
                true
        );

        ItemStack backButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(logConfig.getString(backPath + ".material"))),
                logConfig.getMessage(backPath + ".name"),
                logConfig.getInt(backPath + ".amount"),
                logConfig.getStringList(backPath + ".lore"),
                logConfig.getBoolean(backPath + ".glow"),
                true
        );

        ItemStack closeButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(logConfig.getString(closePath + ".material"))),
                logConfig.getMessage(closePath + ".name"),
                logConfig.getInt(closePath + ".amount"),
                logConfig.getStringList(closePath + ".lore"),
                logConfig.getBoolean(closePath + ".glow"),
                true
        );

        setupCategories();

        builder.setBackButton(backButton, event -> {
            event.setCancelled(true);
            new ChestMenuInventory(plugin, player).openInventory();
        });

        builder.setCloseButton(closeButton, event -> {
            event.setCancelled(true);
            if (event.getCursor().getType() == Material.AIR) {
                player.closeInventory();
            }
        });

        builder.fillWithBorderItem(filler);
    }

    public void setupCategories() {
        LogType[] types = LogType.values();

        int[] innerSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        for (int i = 0; i < types.length && i < innerSlots.length; i++) {
            LogType type = types[i];
            LogDefinition def = plugin.getLogManager().getDefinition(type);

            if (def == null) continue;

            ItemStack categoryItem = ItemHandler.buildItem(
                    def.getCategoryMaterial(),
                    def.getCategoryName(),
                    1,
                    def.getCategoryLore(),
                    def.isCategoryGlow(),
                    true
            );

            builder.addItem(innerSlots[i], categoryItem, event -> {
                event.setCancelled(true);
                new ChestLogInventory(plugin, player, type).openInventory();
            });
        }
    }
}