package com.vitaldev.teamsplus.inventories.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.teamsplus.features.artifacts.ArtifactDefinition;
import com.vitaldev.teamsplus.features.artifacts.ArtifactItemBuilder;
import com.vitaldev.teamsplus.features.upgrades.UpgradeType;
import com.vitaldev.teamsplus.features.artifacts.ArtifactManager;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import com.vitaldev.vitallibs.items.ItemHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class ChestArtifactInventory {

    private final TeamsPlus plugin;
    private final ConfigHandler artifactHandler;
    private final Team team;
    private final Player player;
    private final InventoryBuilder builder;
    private final ConfigHandler langHandler;
    private final ArtifactManager artifactManager;
    private ItemStack emptyItem;

    public ChestArtifactInventory(TeamsPlus plugin, Player player) {
            this.plugin = plugin;
            this.artifactHandler = plugin.getArtifacts();
            this.langHandler = plugin.getLangFile();
            this.team = Team.getTeam(player);
            this.player = player;
            this.builder = new InventoryBuilder(artifactHandler.getInt("artifacts.menu.size"),
                    artifactHandler.getMessage("artifacts.menu.title").replace("{TEAM}", team.getTeamName()), true);
            this.artifactManager = plugin.getArtifactManager();
    }


    public void openInventory() {
        setupMenu();
        setupItems();
        builder.open(player);
    }

    public void setupMenu() {
        String fillerPath = "artifacts.menu.filler";
        String closePath = "artifacts.menu.close";

        ItemStack filler = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(artifactHandler.getString(fillerPath + ".material"))),
                artifactHandler.getMessage(fillerPath + ".name"),
                artifactHandler.getInt(fillerPath + ".amount"),
                artifactHandler.getStringList(fillerPath + ".lore"),
                artifactHandler.getBoolean(fillerPath + ".glow"),
                true
        );

        ItemStack closeButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(artifactHandler.getString(closePath + ".material"))),
                artifactHandler.getMessage(closePath + ".name"),
                artifactHandler.getInt(closePath + ".amount"),
                artifactHandler.getStringList(closePath + ".lore"),
                artifactHandler.getBoolean(closePath + ".glow"),
                true
        );

        setupItems();

        builder.setCloseButton(closeButton, event -> {
            event.setCancelled(true);
            if (event.getCursor().getType() == Material.AIR) {
                player.closeInventory();
            }
        });

        builder.fillWithBorderItem(filler);
    }

    public void setupItems() {

        List<Integer> artifactSlots = artifactHandler.getIntegerList("artifacts.menu.artifact-slots");
        String lockedPath = "artifacts.menu.locked";
        String emptyPath = "artifacts.menu.empty";

        ItemStack lockedItem = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(
                        artifactHandler.getString(lockedPath + ".material"))),
                artifactHandler.getMessage(lockedPath + ".name"),
                artifactHandler.getInt(lockedPath + ".amount"),
                artifactHandler.getStringList(lockedPath + ".lore"),
                artifactHandler.getBoolean(lockedPath + ".glow"),
                true
        );

        emptyItem = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(
                        artifactHandler.getString(emptyPath + ".material"))),
                artifactHandler.getMessage(emptyPath + ".name"),
                artifactHandler.getInt(emptyPath + ".amount"),
                artifactHandler.getStringList(emptyPath + ".lore"),
                artifactHandler.getBoolean(emptyPath + ".glow"),
                true
        );

        int upgradeLevel = team.getUpgradeLevel(UpgradeType.ARTIFACTS)+1;

        int totalSlots = artifactSlots.size();

        int lockedCount = Math.max(0, totalSlots - upgradeLevel);

        for (int i = 0; i < lockedCount; i++) {
            builder.addItem(artifactSlots.reversed().get(i), lockedItem, inventoryClickEvent -> {
                player.sendMessage(langHandler.getMessage("messages.artifacts.locked"));
                inventoryClickEvent.setCancelled(true);
            });
        }

        for (int i = 0; i < upgradeLevel; i++) {

            int slot = artifactSlots.get(i);
            ArtifactItemBuilder artifactBuilder = new ArtifactItemBuilder(plugin);

            ItemStack slotItem =getSlotDisplayItem(team, slot, artifactBuilder);

            builder.addItem(slot, slotItem, event -> {

                event.setCancelled(true);

                if (team.getArtifacts().containsValue(slot)) {
                    handleArtifactRemoval(event, slot, team, builder, artifactBuilder);
                } else {
                    handleArtifactAddition(event, slot, team, builder, artifactBuilder, player);
                }
            });
        }
    }

    private ItemStack getSlotDisplayItem(Team team, int slot, ArtifactItemBuilder artifactBuilder) {
        if (team.getArtifacts().containsValue(slot)) {
            return artifactBuilder.buildArtifact(team.getArtifactFromSlot(slot));
        }
        return emptyItem;
    }

    private void handleArtifactRemoval(InventoryClickEvent event, int slot, Team team,
                                       InventoryBuilder builder, ArtifactItemBuilder artifactBuilder) {
        ItemStack cursor = event.getCursor();

        if (cursor != null && !cursor.getType().isAir()) {
            return;
        }
        ArtifactType type = team.getArtifactFromSlot(slot);
        ArtifactDefinition def = artifactManager.get(type);

        player.sendMessage(
                langHandler.getMessage("messages.artifacts.artifact-removed")
                        .replace("{TIER-COLOR}", def.getTier().getColor())
                        .replace("{TYPE}", def.getDisplayName())
        );

        event.setCursor(artifactBuilder.buildArtifact(team.getArtifactFromSlot(slot)));
        team.clearSlot(slot);
        builder.setItem(slot, emptyItem);
    }

    private void handleArtifactAddition(InventoryClickEvent event, int slot, Team team,
                                        InventoryBuilder builder, ArtifactItemBuilder artifactBuilder, Player player) {
        ItemStack cursor = event.getCursor();

        if (cursor == null || cursor.getType().isAir()) return;
        if (!artifactManager.isArtifact(cursor)) return;

        ArtifactType type = artifactManager.getType(cursor);
        if (type == null) return;

        ArtifactDefinition def = artifactManager.get(type);
        if (def == null) return;

        if (!team.hasArtifactApplied(type)) {
            team.setArtifactSlot(type, slot);

            player.sendMessage(
                    langHandler.getMessage("messages.artifacts.artifact-added")
                            .replace("{TIER-COLOR}", def.getTier().getColor())
                            .replace("{TYPE}", def.getDisplayName())
            );

            event.setCursor(new ItemStack(Material.AIR));
            builder.setItem(slot, artifactBuilder.buildArtifact(type));
        } else {
            player.sendMessage(
                    langHandler.getMessage("messages.artifacts.already-applied"));
        }
    }
}
