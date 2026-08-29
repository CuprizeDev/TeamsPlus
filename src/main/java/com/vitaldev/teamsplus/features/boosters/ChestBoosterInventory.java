package com.vitaldev.teamsplus.features.boosters;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.chest.ChestMenuInventory;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import com.vitaldev.vitallibs.items.ItemHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ChestBoosterInventory {

    private final TeamsPlus plugin;
    private final ConfigHandler boosterConfig;
    private final Team team;
    private final Player player;
    private final InventoryBuilder builder;
    private final List<Integer> emptySlots;

    public ChestBoosterInventory(TeamsPlus plugin, Player player, Team team) {
        this.plugin = plugin;
        this.boosterConfig = plugin.getBoosterManager().getConfig();
        this.team = team;
        this.player = player;
        this.builder = new InventoryBuilder(54,
                boosterConfig.getMessage("boosters.menu.title").replace("{TEAM}", team.getTeamName()), true);
        this.emptySlots = boosterConfig.getIntegerList("boosters.menu.empty-slot.slots");
    }

    public void openInventory() {
        setupMenu();
        setupBoosters();
        builder.open(player);
    }

    public void setupMenu() {
        String fillerPath = "boosters.menu.filler";
        String closePath = "boosters.menu.close";
        String backPath = "boosters.menu.back";

        ItemStack filler = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(boosterConfig.getString(fillerPath + ".material"))),
                boosterConfig.getMessage(fillerPath + ".name"),
                boosterConfig.getInt(fillerPath + ".amount"),
                boosterConfig.getStringList(fillerPath + ".lore"),
                boosterConfig.getBoolean(fillerPath + ".glow"),
                true
        );

        ItemStack backButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(boosterConfig.getString(backPath + ".material"))),
                boosterConfig.getMessage(backPath + ".name"),
                boosterConfig.getInt(backPath + ".amount"),
                boosterConfig.getStringList(backPath + ".lore"),
                boosterConfig.getBoolean(backPath + ".glow"),
                true
        );

        ItemStack closeButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(boosterConfig.getString(closePath + ".material"))),
                boosterConfig.getMessage(closePath + ".name"),
                boosterConfig.getInt(closePath + ".amount"),
                boosterConfig.getStringList(closePath + ".lore"),
                boosterConfig.getBoolean(closePath + ".glow"),
                true
        );

        setupBoosters();

        builder.setBackButton(backButton, event -> {
            event.setCancelled(true);
            new ChestMenuInventory(plugin, player, team).openInventory();
        });

        builder.setCloseButton(closeButton, event -> {
            event.setCancelled(true);
            if (event.getCursor().getType() == Material.AIR) {
                player.closeInventory();
            }
        });

        builder.fillWithBorderItem(filler);
    }

    public void setupBoosters() {
        BoosterType[] types = BoosterType.values();

        for (int i = 0; i < types.length && i < emptySlots.size(); i++) {
            BoosterType type = types[i];
            int slot = emptySlots.get(i);

            if (team.hasActiveBooster(type)) {
                ActiveBooster active = team.getActiveBooster(type);
                BoosterDefinition def = plugin.getBoosterManager().getBooster(active.getBoosterId());

                if (def != null) {
                    OfflinePlayer activator = Bukkit.getOfflinePlayer(active.getActivator());
                    String activatorName = activator.getName() != null ? activator.getName() : "Unknown";
                    String remainingTime = formatTimeShort(active.getRemainingMillis());

                    ItemStack item = ItemHandler.buildItem(
                            def.getMaterial(),
                            def.getDisplayName(),
                            1,
                            Arrays.asList(
                                    "&7" + def.getType().getDisplayName() + " Booster",
                                    "",
                                    "&fMultiplier: &e" + def.getMultiplier() + "x",
                                    "&fActivated By: &e" + activatorName,
                                    "&fTime Remaining: &c" + remainingTime
                            ),
                            true,
                            true
                    );

                    builder.addItem(slot, item, event -> event.setCancelled(true));
                }
            } else {
                String emptyPath = "boosters.menu.empty-slot";
                ItemStack item = ItemHandler.buildItem(
                        Objects.requireNonNull(Material.getMaterial(boosterConfig.getString(emptyPath + ".material"))),
                        boosterConfig.getMessage(emptyPath + ".name").replace("{TYPE}", type.getDisplayName()),
                        boosterConfig.getInt(emptyPath + ".amount"),
                        boosterConfig.getStringList(emptyPath + ".lore")
                                .stream()
                                .map(line -> line.replace("{TYPE}", type.getDisplayName()))
                                .toList(),
                        boosterConfig.getBoolean(emptyPath + ".glow"),
                        true
                );

                builder.addItem(slot, item, event -> event.setCancelled(true));
            }
        }
    }
    private String formatTimeShort(long millis) {
        if (millis <= 0) return "0s";

        Duration duration = Duration.ofMillis(millis);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0) {
            builder.append(minutes).append("m ");
        }
        if (seconds > 0 || builder.length() == 0) {
            builder.append(seconds).append("s");
        }

        return builder.toString().trim();
    }
}