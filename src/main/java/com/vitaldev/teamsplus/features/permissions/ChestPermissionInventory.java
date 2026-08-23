package com.vitaldev.teamsplus.features.permissions;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.chest.ChestMenuInventory;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import com.vitaldev.vitallibs.items.ItemHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ChestPermissionInventory {

    private static final PlayerRank[] EDITABLE_RANKS = {
            PlayerRank.MEMBER,
            PlayerRank.OFFICER,
            PlayerRank.CO_LEADER
    };

    private static final List<Integer> ACTION_SLOTS = Arrays.asList(
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    );

    private static final int SLOT_MEMBER_BTN = 3;
    private static final int SLOT_OFFICER_BTN = 4;
    private static final int SLOT_COLEADER_BTN = 5;

    private final TeamsPlus plugin;
    private final ConfigHandler permHandler;
    private final ConfigHandler langHandler;
    private final Team team;
    private final Player player;
    private final InventoryBuilder builder;

    private int rankIndex = 0;

    public ChestPermissionInventory(TeamsPlus plugin, Player player) {
        this.plugin = plugin;
        this.permHandler = plugin.getPermissionsFile();
        this.langHandler = plugin.getLangFile();
        this.team = Team.getTeam(player);
        this.player = player;

        // The title is now more generic and doesn't include the rank.
        // This avoids having to re-create the inventory on rank change.
        // Assumes the title in permissions.yml is changed to something like "Team Permissions for {TEAM}"
        this.builder = new InventoryBuilder(
                permHandler.getInt("permissions.menu.size"),
                permHandler.getMessage("permissions.menu.title")
                        .replace("{TEAM}", team.getTeamName()),
                false
        );
    }

    public void openInventory() {
        setupMenu();
        setupRankSelectors();
        setupPermissionItems();
        builder.open(player);
    }

    private void setupMenu() {
        String fillerPath = "permissions.menu.filler";
        String closePath = "permissions.menu.close";
        String backPath = "permissions.menu.back";

        ItemStack filler = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(permHandler.getString(fillerPath + ".material"))),
                permHandler.getMessage(fillerPath + ".name"),
                permHandler.getInt(fillerPath + ".amount"),
                permHandler.getStringList(fillerPath + ".lore"),
                permHandler.getBoolean(fillerPath + ".glow"),
                true
        );

        ItemStack closeButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(permHandler.getString(closePath + ".material"))),
                permHandler.getMessage(closePath + ".name"),
                permHandler.getInt(closePath + ".amount"),
                permHandler.getStringList(closePath + ".lore"),
                permHandler.getBoolean(closePath + ".glow"),
                true
        );

        ItemStack backButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(permHandler.getString(backPath + ".material"))),
                permHandler.getMessage(backPath + ".name"),
                permHandler.getInt(backPath + ".amount"),
                permHandler.getStringList(backPath + ".lore"),
                permHandler.getBoolean(backPath + ".glow"),
                true
        );

        builder.setBackButton(backButton, event -> {
            event.setCancelled(true);
            new ChestMenuInventory(plugin, player).openInventory();
        });

        builder.setCloseButton(closeButton, event -> {
            event.setCancelled(true);
            player.closeInventory();
        });

        builder.fillWithBorderItem(filler);
    }

    private void setupRankSelectors() {
        int[] targetSlots = { SLOT_MEMBER_BTN, SLOT_OFFICER_BTN, SLOT_COLEADER_BTN };

        for (int i = 0; i < EDITABLE_RANKS.length; i++) {
            PlayerRank rank = EDITABLE_RANKS[i];
            int slot = targetSlots[i];
            boolean isCurrent = (i == rankIndex);

            String rankColor = getRankColor(rank);
            String prefix = isCurrent ? "&e&l> " : "";
            String suffix = isCurrent ? " &e&l <" : "";

            List<String> lore = isCurrent
                    ? Arrays.asList("", "&a&lEditing these permissions now.")
                    : Arrays.asList("", "&eClick to edit " + rankColor + rank.getDisplayName() + " &epermissions.");

            ItemStack rankButton = ItemHandler.buildItem(
                    getRankMaterial(rank),
                    prefix + rankColor + "&l" + rank.getDisplayName() + suffix,
                    1,
                    lore,
                    isCurrent,
                    true
            );

            final int targetIndex = i;
            builder.addItem(slot, rankButton, event -> {
                event.setCancelled(true);
                if (rankIndex != targetIndex) {
                    rankIndex = targetIndex;
                    updateInventory();
                }
            });
        }
    }

    private void setupPermissionItems() {
        PlayerRank currentRank = EDITABLE_RANKS[rankIndex];
        PermissableAction[] actions = PermissableAction.values();

        for (int i = 0; i < actions.length && i < ACTION_SLOTS.size(); i++) {
            PermissableAction action = actions[i];
            int slot = ACTION_SLOTS.get(i);
            placePermissionItem(slot, currentRank, action);
        }
    }

    private void placePermissionItem(int slot, PlayerRank rank, PermissableAction action) {
        boolean allowed = team.getPermission(rank, action);

        String statusColor = allowed ? "&a" : "&c";
        String statusText  = allowed ? "&aAllowed" : "&cDenied";
        String clickHint   = "&7Click to toggle";

        ItemStack item = ItemHandler.buildItem(
                action.getDisplayMaterial(),
                statusColor + "&l" + action.getDisplayName(),
                1,
                Arrays.asList(
                        "&7" + action.getDescription(),
                        "",
                        "&fStatus: " + statusText,
                        "",
                        clickHint
                ),
                allowed,
                true
        );

        builder.addItem(slot, item, event -> {
            event.setCancelled(true);
            handleToggle(rank, action, slot);
        });
    }

    public void handleToggle(PlayerRank rank, PermissableAction action, int slot) {
        team.togglePermission(rank, action);
        boolean nowAllowed = team.getPermission(rank, action);

        String msg = nowAllowed
                ? langHandler.getMessage("messages.permissions.toggled-on")
                : langHandler.getMessage("messages.permissions.toggled-off");

        player.sendMessage(msg
                .replace("{ACTION}", action.getDisplayName())
                .replace("{RANK}", rank.getDisplayName()));

        placePermissionItem(slot, rank, action);
    }

    // Updates the inventory items in-place without closing and reopening the inventory.
// This provides a smoother, flicker-free user experience when changing ranks.
    private void updateInventory() {
        setupRankSelectors();
        setupPermissionItems();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String getRankColor(PlayerRank rank) {
        return switch (rank) {
            case LEADER     -> "&6";
            case CO_LEADER  -> "&b";
            case OFFICER    -> "&a";
            case MEMBER     -> "&7";
        };
    }

    private Material getRankMaterial(PlayerRank rank) {
        return switch (rank) {
            case LEADER     -> Material.NETHER_STAR;
            case CO_LEADER  -> Material.DIAMOND;
            case OFFICER    -> Material.IRON_INGOT;
            case MEMBER     -> Material.STONE;
        };
    }
}
