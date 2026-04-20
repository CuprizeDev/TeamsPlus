package com.vitaldev.teamsplus.commands.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.teamsplus.features.chest.ChestManager;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ClaimChestCmd extends SubCmd {

    private final TeamsPlus plugin;
    public ClaimChestCmd(TeamsPlus teamsPlus) {
        super("claimchest", "teamsplus.base.claimchest", "teamsplus.admin.claimchest", Arrays.asList("chest", "claimchests", "chests"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        ConfigHandler langHandler = this.plugin.getLangFile();

        ChestManager chestUtil = new ChestManager(TeamsPlus.getPlugin(TeamsPlus.class));
        if (!(sender instanceof Player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }

        Player player = (Player) sender;

        if (Team.hasTeam(player)) {
            player.sendMessage(langHandler.getMessage("messages.chest.has-team"));
            return;
        }

        player.sendMessage(langHandler.getMessage("messages.chest.received"));
        if (!player.getInventory().addItem(chestUtil.getClaimChestItem()).isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), chestUtil.getClaimChestItem());
        }
    }
}
