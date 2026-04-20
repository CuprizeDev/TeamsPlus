package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.commands.CommandUtil;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ListCmd extends SubCmd {

    private final TeamsPlus plugin;
    public ListCmd(TeamsPlus teamsPlus) {
        super("list", "teamsplus.base.list", "teamsplus.admin.list", Arrays.asList("list", "lists", "listall"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        ConfigHandler langHandler = this.plugin.getLangFile();

        if (!(sender instanceof Player player)) {
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


        player.sendMessage(langHandler.getMessage("messages.list.header")
                .replace("{PAGE-COUNT}", String.valueOf((sortedTeams.size()/9)+1))
                .replace("{PAGE-NUMBER}", String.valueOf(listPage)));

        for (int i = sortedTeams.size()/9; i < listPage*9; i++) {

            if (sortedTeams.size() <= i) {
                return;
            }

            Team team = sortedTeams.get(i);

            player.sendMessage(langHandler.getMessage("messages.list.message")
                    .replace("{TEAM}", team.getTeamName())
                    .replace("{ONLINE}", String.valueOf(team.getOnlineMemberCount()))
                    .replace("{MEMBERS}", String.valueOf(team.getMembers().size()))
                    .replace("{POWER}", String.valueOf(team.getPower())));
        }
    }
}
