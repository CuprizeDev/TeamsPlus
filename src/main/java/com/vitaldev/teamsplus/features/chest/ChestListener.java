package com.vitaldev.teamsplus.features.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.teamsplus.model.TeamData;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.items.NBTHandler;
import com.vitaldev.vitallibs.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ChestListener implements Listener {

    public TeamsPlus plugin;

    public ChestListener(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChestInteract(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        if (block.getType() != Material.CHEST) {
            return;
        }

        NBTHandler nbtHandler = new NBTHandler(plugin);

        if (!nbtHandler.getBoolean(block, nbtHandler.getKey() + "claim_chest")){
            return;
        }

        Team targetTeam = null;
        for (UUID uuid : Team.getTeamList()) {
            Team t = Team.getTeam(uuid);
            if (t.getClaimChest() != null && t.getClaimChest().getBlock().getLocation().equals(block.getLocation())) {
                targetTeam = t;
                break;
            }
        }

        if (targetTeam == null) {
            event.getPlayer().sendMessage(plugin.getLangFile().getMessage("messages.chest.no-team-chest"));
            event.setCancelled(true);
            return;
        }

        Team playerTeam = Team.getTeam(event.getPlayer());
        if (targetTeam != playerTeam) {
            if (!com.vitaldev.teamsplus.commands.BypassCmd.isBypassing(event.getPlayer())) {
                event.getPlayer().sendMessage(ChatUtil.color(plugin.getLangFile().getMessage("messages.chest.not-your-chest")));
                event.setCancelled(true);
                return;
            }
        }

        new ChestMenuInventory(plugin, event.getPlayer(), targetTeam).openInventory();
        event.setCancelled(true);


    }

    @EventHandler
    public void onChestBreak(BlockBreakEvent event) {

        Block block = event.getBlock();
        if (block == null) return;
        if (block.getType() != Material.CHEST) return;

        NBTHandler nbtHandler = new NBTHandler(plugin);
        if (!nbtHandler.getBoolean(block, nbtHandler.getKey() + "claim_chest")){
            return;
        }

        Player player = event.getPlayer();
        Team targetTeam = null;
        for (UUID uuid : Team.getTeamList()) {
            Team t = Team.getTeam(uuid);
            if (t.getClaimChest() != null && t.getClaimChest().getBlock().getLocation().equals(block.getLocation())) {
                targetTeam = t;
                break;
            }
        }

        if (targetTeam == null) {
            Location location = block.getLocation();
            location.getWorld().dropItemNaturally(location, new ChestItemBuilder(plugin).buildClaimChest());
            return;
        }

        Team playerTeam = Team.getTeam(player);
        if (playerTeam != null && playerTeam.getTeamUUID().equals(targetTeam.getTeamUUID())) {
            if (!com.vitaldev.teamsplus.commands.BypassCmd.isBypassing(player)) {
                player.sendMessage(ChatUtil.color(plugin.getLangFile().getMessage("messages.chest.cannot-break-own")));
                event.setCancelled(true);
                return;
            } else {
                Location location = block.getLocation();
                location.getWorld().dropItemNaturally(location, new ChestItemBuilder(plugin).buildClaimChest());
                return;
            }
        }

        if (plugin.getRaidManager().isRaided(targetTeam)) {
            com.vitaldev.teamsplus.features.raiding.RaidManager.RaidRecord raid = plugin.getRaidManager().getActiveRaid(targetTeam);
            if (raid != null && playerTeam != null && playerTeam.getTeamUUID().equals(raid.attackerId)) {
                if (targetTeam.getDurability() > 1) {
                    targetTeam.setDurability(targetTeam.getDurability() - 1);
                    targetTeam.updateHologram();
                    String msg = plugin.getLangFile().getMessage("messages.chest.damaged-chest")
                            .replace("{TEAM}", targetTeam.getTeamName())
                            .replace("{DURABILITY}", String.valueOf(targetTeam.getDurability()));
                    player.sendMessage(ChatUtil.color(msg));
                    event.setCancelled(true);
                } else {
                    String msg = plugin.getLangFile().getMessage("messages.chest.destroyed-chest")
                            .replace("{TEAM}", targetTeam.getTeamName());
                    player.sendMessage(ChatUtil.color(msg));
                    
                    plugin.getRaidManager().endRaid(targetTeam);
                    Team.disbandTeam(targetTeam, plugin);
                    
                    Location location = block.getLocation();
                    location.getWorld().dropItemNaturally(location, new ChestItemBuilder(plugin).buildClaimChest());
                }
            } else {
                if (!com.vitaldev.teamsplus.commands.BypassCmd.isBypassing(player)) {
                    player.sendMessage(ChatUtil.color(plugin.getLangFile().getMessage("messages.chest.cannot-break-enemy")));
                    event.setCancelled(true);
                }
            }
        } else {
            if (!com.vitaldev.teamsplus.commands.BypassCmd.isBypassing(player)) {
                player.sendMessage(ChatUtil.color(plugin.getLangFile().getMessage("messages.chest.cannot-break-enemy")));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onChestExplode(BlockExplodeEvent event) {

    }

    @EventHandler
    public void onChestPlace(BlockPlaceEvent event) {

        if (event.getBlockPlaced().getType() != Material.CHEST) {
            return;
        }

        Player player = event.getPlayer();
        NBTHandler nbtHandler = new NBTHandler(plugin);
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        ConfigHandler langHandler = plugin.getLangFile();
        TeamData teamData = new TeamData(plugin);

        if (offHand.getType() == Material.CHEST
                && Boolean.TRUE.equals(nbtHandler.getBoolean(offHand, nbtHandler.getKey() + "claim_chest"))) {
            event.setCancelled(true);
            return;
        }
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR
                && Boolean.TRUE.equals(nbtHandler.getBoolean(mainHand, nbtHandler.getKey() + "claim_chest"))) {

            if (Team.hasTeam(player)) {
                player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.chest.has-team")));
                event.setCancelled(true);
                return;
            }

            if (Team.containsClaimChest(event.getBlockPlaced().getLocation().getChunk())) {
                player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.chest.already-claimed")));
                event.setCancelled(true);
                return;
            }
            Team team = new Team(plugin, player.getName() + "'s Team", player.getUniqueId(), UUID.randomUUID(), event.getBlockPlaced().getLocation());
            com.vitaldev.teamsplus.events.TeamCreateEvent createEvent = new com.vitaldev.teamsplus.events.TeamCreateEvent(player, team.getTeamName(), team);
            org.bukkit.Bukkit.getPluginManager().callEvent(createEvent);
            if (createEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }
            player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.chest.placed")));
            teamData.saveTeam(team);
            nbtHandler.addBoolean(event.getBlockPlaced(), nbtHandler.getKey() + "claim_chest", true);
            Team.addUUID(team.getTeamUUID(), team);
        }
    }
}
