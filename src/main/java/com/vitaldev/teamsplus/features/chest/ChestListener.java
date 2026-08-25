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

        new ChestMenuInventory(plugin, event.getPlayer()).openInventory();
        event.setCancelled(true);


    }

    @EventHandler
    public void onChestBreak(BlockBreakEvent event) {

        Block block = event.getBlock();

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

        Location location = block.getLocation();

        location.getWorld().dropItemNaturally(location, new ChestItemBuilder(plugin).buildClaimChest());

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
