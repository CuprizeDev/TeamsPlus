package com.vitaldev.teamsplus.features.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.ClaimKey;
import com.vitaldev.teamsplus.model.Relation;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.teamsplus.commands.BypassCmd;
import com.vitaldev.teamsplus.features.permissions.PermissableAction;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import com.vitaldev.vitallibs.items.ItemHandler;
import com.vitaldev.vitallibs.util.StringUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

// Claim-map GUI. All chunk ownership lookups use {@link ClaimKey} so that no
// {@code world.getChunkAt()} calls are made during rendering, preventing silent
// chunk loads from disk.
public class ChestClaimInventory {

    private final TeamsPlus plugin;
    private final ConfigHandler claimsHandler;
    private final Team team;
    private final Player player;
    private final InventoryBuilder builder;
    private final int amount;
    private final int rows;

    // World the claim map is displayed for. Stored by reference (no chunk involved).
    private final World viewedWorld;
    private final UUID viewedWorldId;

    // Centre of the visible claim grid — chunk coordinates, never a Chunk object.
    private int viewedX;
    private int viewedZ;

    private Set<Integer> blockedSlots;

    public ChestClaimInventory(TeamsPlus plugin, Player player, Team team) {
        this.plugin = plugin;
        this.claimsHandler = plugin.getClaims();
        this.team = team;
        this.player = player;
        int size = claimsHandler.getInt("claims.menu.size");
        this.amount = size;
        this.rows = size / 9;
        this.builder = new InventoryBuilder(54,
                claimsHandler.getMessage("claims.menu.title").replace("{TEAM}", team.getTeamName()), false);

        // Derive chunk coords from chest location without calling getChunk() (avoids chunk load).
        Location chestLoc = team.getClaimChest();
        this.viewedWorld = chestLoc.getWorld();
        this.viewedWorldId = viewedWorld.getUID();
        this.viewedX = chestLoc.getBlockX() >> 4;
        this.viewedZ = chestLoc.getBlockZ() >> 4;
    }

    public void openInventory() {
        setupMenu();
        builder.open(player);
    }

    private void setupMenu() {
        String backPath = "claims.menu.back";
        ItemStack backButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(claimsHandler.getString(backPath + ".material"))),
                claimsHandler.getMessage(backPath + ".name"),
                claimsHandler.getInt(backPath + ".amount"),
                claimsHandler.getStringList(backPath + ".lore"),
                claimsHandler.getBoolean(backPath + ".glow"),
                true
        );

        setupItems();

        builder.setBackButton(backButton, event -> {
            event.setCancelled(true);
            new ChestMenuInventory(plugin, player, team).openInventory();
        });
    }

    private void setupItems() {
        String claimedPath = "claims.menu.chest-claim";

        int middleRow = rows / 2;
        int topSlot    = 4;
        int bottomSlot = amount - 5;
        int leftSlot   = middleRow * 9;
        int rightSlot  = middleRow * 9 + 8;

        int claimChestSlot = getClaimChestSlot();

        blockedSlots = new HashSet<>();
        blockedSlots.add(topSlot);
        blockedSlots.add(bottomSlot);
        blockedSlots.add(leftSlot);
        blockedSlots.add(rightSlot);
        blockedSlots.add(bottomSlot - 1);

        int centerRow = rows / 2;
        int centerCol = 4;
        Direction facing = getDirections(player);

        // Render the chunk grid — pure coordinate math, no chunk loading.
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int dx = col - centerCol;
                int dz = row - centerRow;
                int[] rotated = rotate(dx, dz, facing);
                int chunkX = viewedX + rotated[0];
                int chunkZ = viewedZ + rotated[1];
                int slot = row * 9 + col;
                buildMapItem(chunkX, chunkZ, slot);
            }
        }

        Direction top    = getDirections(player);
        Direction right  = top.rotateRight();
        Direction bottom = top.opposite();
        Direction left   = top.rotateLeft();

        ItemStack claimChest = ItemHandler.buildItem(
                Material.valueOf(claimsHandler.getString(claimedPath + ".material")),
                claimsHandler.getMessage(claimedPath + ".name"),
                claimsHandler.getInt(claimedPath + ".amount"),
                claimsHandler.getColoredList(claimedPath + ".lore"),
                claimsHandler.getBoolean("claims.menu.claimable.glow"),
                true);

        if (claimChestSlot != -1) {
            blockedSlots.add(claimChestSlot);
            builder.setItem(claimChestSlot, claimChest);
        }

        builder.addItem(topSlot,    buildDirectionItems(top.name().toLowerCase()),    event -> { shift(top);    event.setCancelled(true); });
        builder.addItem(rightSlot,  buildDirectionItems(right.name().toLowerCase()),  event -> { shift(right);  event.setCancelled(true); });
        builder.addItem(bottomSlot, buildDirectionItems(bottom.name().toLowerCase()), event -> { shift(bottom); event.setCancelled(true); });
        builder.addItem(leftSlot,   buildDirectionItems(left.name().toLowerCase()),   event -> { shift(left);   event.setCancelled(true); });
    }

    private int[] inverseRotate(int dx, int dz, Direction dir) {
        return switch (dir) {
            case NORTH -> new int[]{dx,  dz};
            case SOUTH -> new int[]{-dx, -dz};
            case EAST  -> new int[]{dz,  -dx};
            case WEST  -> new int[]{-dz, dx};
        };
    }

    // Computes which GUI slot the claim chest occupies using chunk-coordinate arithmetic.
// Never calls {@code getChunk()} on the location.
    private int getClaimChestSlot() {
        Location chestLoc = team.getClaimChest();
        int chestChunkX = chestLoc.getBlockX() >> 4;
        int chestChunkZ = chestLoc.getBlockZ() >> 4;

        int dx = chestChunkX - viewedX;
        int dz = chestChunkZ - viewedZ;

        Direction facing  = getDirections(player);
        int[] rotated     = inverseRotate(dx, dz, facing);

        int centerRow = rows / 2;
        int centerCol = 4;
        int col = centerCol + rotated[0];
        int row = centerRow + rotated[1];

        if (col < 0 || col > 8 || row < 0 || row >= rows) return -1;
        return row * 9 + col;
    }

    // Returns true if the visible grid centred on (centerX, centerZ) contains any claim owned
// by this team. Uses ClaimKey lookups — no chunk loading.
    private boolean containsTeamClaim(int centerX, int centerZ) {
        int centerRow = rows / 2;
        int centerCol = 4;
        Direction facing = getDirections(player);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int dx = col - centerCol;
                int dz = row - centerRow;
                int[] rotated = rotate(dx, dz, facing);
                if (team.ownsClaim(centerX + rotated[0], centerZ + rotated[1], viewedWorldId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void shift(Direction direction) {
        int[] offset = switch (direction) {
            case NORTH -> new int[]{0,  -1};
            case SOUTH -> new int[]{0,   1};
            case EAST  -> new int[]{1,   0};
            case WEST  -> new int[]{-1,  0};
        };

        int nextX = viewedX + offset[0];
        int nextZ = viewedZ + offset[1];

        if (!containsTeamClaim(nextX, nextZ)) return;

        viewedX = nextX;
        viewedZ = nextZ;

        setupMenu();
        player.updateInventory();
    }

    // Returns whether the chunk at (chunkX, chunkZ) can be claimed by this team.
// All checks use ClaimKey — no chunk loading.
    private boolean isClaimable(int chunkX, int chunkZ) {
        if (team.getClaimRelation(chunkX, chunkZ, viewedWorldId) == Relation.SPAWN) return false;
        if (Team.isClaimed(viewedWorldId, chunkX, chunkZ)) return false;
        if (hasNearbyEnemyChunks(chunkX, chunkZ, 1)) return false;

        // At least one adjacent chunk must be owned by this team.
        return team.ownsClaim(chunkX,     chunkZ - 1, viewedWorldId)
            || team.ownsClaim(chunkX,     chunkZ + 1, viewedWorldId)
            || team.ownsClaim(chunkX + 1, chunkZ,     viewedWorldId)
            || team.ownsClaim(chunkX - 1, chunkZ,     viewedWorldId);
    }

    private int[] rotate(int dx, int dz, Direction dir) {
        return switch (dir) {
            case NORTH -> new int[]{dx,  dz};
            case SOUTH -> new int[]{-dx, -dz};
            case EAST  -> new int[]{-dz, dx};
            case WEST  -> new int[]{dz,  -dx};
        };
    }

    private Direction getDirections(Player player) {
        float yaw = (player.getLocation().getYaw() + 180) % 360;
        if (yaw >= 45 && yaw < 135) return Direction.EAST;
        if (yaw >= 135 && yaw < 225) return Direction.SOUTH;
        if (yaw >= 225 && yaw < 315) return Direction.WEST;
        return Direction.NORTH;
    }

    // Renders one cell of the claim map at (chunkX, chunkZ) into the given GUI slot.
// Uses only ClaimKey lookups — never calls {@code world.getChunkAt()}.
    private void buildMapItem(int chunkX, int chunkZ, int slot) {
        Relation relation = team.getClaimRelation(chunkX, chunkZ, viewedWorldId);
        ItemStack itemStack;

        if (isClaimable(chunkX, chunkZ)) {
            String claimablePath = "claims.menu.claimable";
            itemStack = ItemHandler.buildItem(
                    Material.valueOf(claimsHandler.getString(claimablePath + ".material")),
                    claimsHandler.getMessage(claimablePath + ".name"),
                    claimsHandler.getInt(claimablePath + ".amount"),
                    claimsHandler.getColoredList(claimablePath + ".lore"),
                    claimsHandler.getBoolean(claimablePath + ".glow"),
                    true);
        } else {
            String relationPath = "claims.menu." + relation.toString().toLowerCase();
            itemStack = ItemHandler.buildItem(
                    Material.valueOf(claimsHandler.getString(relationPath + ".material")),
                    claimsHandler.getMessage(relationPath + ".name"),
                    claimsHandler.getInt(relationPath + ".amount"),
                    claimsHandler.getColoredList(relationPath + ".lore"),
                    claimsHandler.getBoolean(relationPath + ".glow"),
                    true);
        }

        builder.addItem(slot, itemStack, event -> {
            if (isClaimable(chunkX, chunkZ)) {
                if (!team.canDo(player, PermissableAction.TERRITORY) && !BypassCmd.isBypassing(player)) {
                    player.sendMessage(com.vitaldev.vitallibs.util.ChatUtil.color(plugin.getLangFile().getString("messages.permissions.denied")));
                    return;
                }
                team.addClaim(new ClaimKey(viewedWorldId, chunkX, chunkZ));
                java.util.Map<String, String> meta = new java.util.HashMap<>();
                meta.put("chunk", chunkX + "," + chunkZ);
                plugin.getLogManager().logEvent(team, com.vitaldev.teamsplus.features.logs.LogType.CLAIM_ADD, player, player.getLocation(), meta);
            } else if (team.ownsClaim(chunkX, chunkZ, viewedWorldId)) {
                if (!isUnclaimable(chunkX, chunkZ)) {
                    return; // interior chunk — not allowed to unclaim
                }
                if (!team.canDo(player, PermissableAction.TERRITORY) && !BypassCmd.isBypassing(player)) {
                    player.sendMessage(com.vitaldev.vitallibs.util.ChatUtil.color(plugin.getLangFile().getString("messages.permissions.denied")));
                    return;
                }
                team.removeClaim(new ClaimKey(viewedWorldId, chunkX, chunkZ));
                java.util.Map<String, String> meta = new java.util.HashMap<>();
                meta.put("chunk", chunkX + "," + chunkZ);
                plugin.getLogManager().logEvent(team, com.vitaldev.teamsplus.features.logs.LogType.CLAIM_REMOVE, player, player.getLocation(), meta);
            }

            if (blockedSlots.contains(slot)) return;

            updateSlot(chunkX, chunkZ, slot);
            event.setCancelled(true);
        });
    }

    // Checks whether any chunk in the given radius around (cx, cz) is an enemy claim.
// Uses ClaimKey lookups — no chunk loading.
    private boolean hasNearbyEnemyChunks(int cx, int cz, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (team.getClaimRelation(cx + dx, cz + dz, viewedWorldId) == Relation.ENEMY) {
                    return true;
                }
            }
        }
        return false;
    }

    // Updates the clicked cell and its four neighbours. Uses coordinate arithmetic —
// no chunk loading.
    public void updateSlot(int chunkX, int chunkZ, int slot) {
        buildMapItem(chunkX, chunkZ, slot);

        Direction facing = getDirections(player);

        // Above
        if (slot - 9 >= 0 && !blockedSlots.contains(slot - 9)) {
            int[] offset = rotate(0, -1, facing);
            buildMapItem(chunkX + offset[0], chunkZ + offset[1], slot - 9);
        }
        // Below
        if (slot + 9 < amount && !blockedSlots.contains(slot + 9)) {
            int[] offset = rotate(0, 1, facing);
            buildMapItem(chunkX + offset[0], chunkZ + offset[1], slot + 9);
        }
        // Left
        if (slot % 9 != 0 && !blockedSlots.contains(slot - 1)) {
            int[] offset = rotate(-1, 0, facing);
            buildMapItem(chunkX + offset[0], chunkZ + offset[1], slot - 1);
        }
        // Right
        if ((slot + 1) % 9 != 0 && !blockedSlots.contains(slot + 1)) {
            int[] offset = rotate(1, 0, facing);
            buildMapItem(chunkX + offset[0], chunkZ + offset[1], slot + 1);
        }
    }

    private ItemStack buildDirectionItems(String direction) {
        String directionPath = "claims.menu.direction";
        return ItemHandler.buildItem(
                Material.valueOf(claimsHandler.getString(directionPath + ".material")),
                claimsHandler.getMessage(directionPath + ".name").replace("{DIRECTION}", StringUtil.capitalize(direction)),
                claimsHandler.getInt(directionPath + ".amount"),
                claimsHandler.getColoredList(directionPath + ".lore"),
                claimsHandler.getBoolean(directionPath + ".glow"),
                true);
    }

    private boolean isOdd(int rows) {
        return (rows % 2 != 0);
    }

    private boolean isUnclaimable(int chunkX, int chunkZ) {
        if (!team.ownsClaim(chunkX, chunkZ, viewedWorldId)) return false;

        boolean isEdge = !team.ownsClaim(chunkX,     chunkZ - 1, viewedWorldId)
                || !team.ownsClaim(chunkX,     chunkZ + 1, viewedWorldId)
                || !team.ownsClaim(chunkX + 1, chunkZ,     viewedWorldId)
                || !team.ownsClaim(chunkX - 1, chunkZ,     viewedWorldId);

        if (!isEdge) return false;

        // Check that removing this chunk wouldn't disconnect the remaining claims.
        Set<ClaimKey> allClaims = team.getClaims(); // adjust to your actual API

        if (allClaims.size() <= 1) return false; // last claim — nothing left to strand

        ClaimKey removed = new ClaimKey(viewedWorldId, chunkX, chunkZ);

        Set<ClaimKey> remaining = new HashSet<>(allClaims);
        remaining.remove(removed);

        if (remaining.isEmpty()) return true;

        // BFS from an arbitrary remaining chunk; if it can't reach every other
        // remaining chunk, this removal would split the claim.
        ClaimKey start = remaining.iterator().next();
        Set<ClaimKey> visited = new HashSet<>();
        java.util.Deque<ClaimKey> queue = new java.util.ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        int[][] dirs = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}};
        while (!queue.isEmpty()) {
            ClaimKey cur = queue.poll();
            for (int[] d : dirs) {
                ClaimKey neighbor = new ClaimKey(viewedWorldId, cur.x() + d[0], cur.z() + d[1]);
                if (remaining.contains(neighbor) && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return visited.size() == remaining.size();
    }
}
