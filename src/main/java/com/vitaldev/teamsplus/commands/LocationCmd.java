package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;


public class LocationCmd extends SubCmd {

    private final TeamsPlus plugin;

    public LocationCmd(TeamsPlus teamsPlus) {

        super("location", "teamsplus.base.location", "", Arrays.asList("loc", "coords"));
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
        Team team = Team.getTeam(player);

        if (team == null) {
            player.sendMessage(langHandler.getMessage("messages.need-team"));
            return;
        }

        Location location = player.getLocation();

        team.sendMessage(langHandler.getMessage("messages.location.team-message")
                        .replace("{PLAYER}", player.getName())
                        .replace("{WORLD}", location.getWorld().getName())
                        .replace("{X}", String.valueOf(location.getBlockX()))
                        .replace("{Y}", String.valueOf(location.getBlockY()))
                        .replace("{Z}", String.valueOf(location.getBlockZ())));


    }
}
