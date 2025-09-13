package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.teams.PlayerRank;
import com.vitaldev.teamsplus.teams.Team;
import com.vitaldev.vitallibs.commands.CommandUtil;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ListCmd extends SubCmd {

    private final TeamsPlus plugin;
    public ListCmd(TeamsPlus teamsPlus) {
        super("list", "teamsplus.base.list", "teamsplus.admin.list", Arrays.asList("list", "lists", "listall"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        ConfigHandler langHandler = this.plugin.getLangFile();

        if (!(sender instanceof Player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }

        int listPage = CommandUtil.parseIntArg(sender,
                args,
                2,
                1,
                langHandler.getString("messages.invalid-number"));

        List<UUID> teamUUIDs = Team.getTeamList();

        List<Team> sortedTeams = teamUUIDs.stream()
                .map(Team::getTeam)
                .sorted(Comparator.comparingInt(Team::getOnlineMemberCount).reversed())
                .toList();


        for (int i = 0; i < listPage*9; i++) {

            if (sortedTeams.size() <= i) {
                return;
            }

            Bukkit.broadcastMessage("Team: " + sortedTeams.get(i).getTeamName());
        }

    }
}
