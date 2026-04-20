package com.vitaldev.teamsplus.commands.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class FindChestCmd extends SubCmd {

    private final TeamsPlus plugin;
    public FindChestCmd(TeamsPlus teamsPlus) {
        super("findchest", "teamsplus.base.findchest", "teamsplus.admin.findchest", Arrays.asList("findchest", "chestlocation", "locatechest"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        ConfigHandler langHandler = this.plugin.getLangFile();

        if (!(sender instanceof Player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }

        Player player = (Player) sender;

        if (!Team.hasTeam(player)) {
            player.sendMessage(langHandler.getMessage("messages.need-team"));
            return;
        }

        Location chestLocation = Team.getTeam(player).getClaimChest();

        player.sendMessage(langHandler.getMessage("messages.chest.find")
                .replace("{Z}", String.valueOf(chestLocation.getBlockZ()))
                .replace("{Y}", String.valueOf(chestLocation.getBlockY()))
                .replace("{X}", String.valueOf(chestLocation.getBlockX())
        ));
    }
}
