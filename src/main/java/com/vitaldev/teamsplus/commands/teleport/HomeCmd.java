package com.vitaldev.teamsplus.commands.teleport;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ChatUtil;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import com.vitaldev.vitallibs.util.TaskUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class HomeCmd extends SubCmd {

    private final TeamsPlus plugin;

    public HomeCmd(TeamsPlus teamsPlus) {
        super("home", "teamsplus.base.home", "teamsplus.admin.home", Arrays.asList("homes", "home"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        ConfigHandler langHandler = this.plugin.getLangFile();
        TaskUtil taskUtil = new TaskUtil(plugin);

        if (!(sender instanceof Player player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }

        if (!Team.hasTeam(player)) {
            player.sendMessage(langHandler.getMessage("messages.need-team"));
            return;
        }

        int cooldown = plugin.getConfig().getInt("teams.cooldowns.home");
        Location claimChestLocation = Team.getTeam(player).getClaimChest().clone();
        claimChestLocation.setX(claimChestLocation.getBlockX() + 0.5);
        claimChestLocation.setZ(claimChestLocation.getBlockZ() + 0.5);
        claimChestLocation.setY(claimChestLocation.getBlockY() + 1.0);
        player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.home.commence").replace("{TIME}",String.valueOf(cooldown))));

        taskUtil.scheduleTask(player, "teleport", () -> {
            player.teleport(claimChestLocation);
            player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.home.success")));
        }, cooldown);
    }
}
