package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ChatCmd extends SubCmd {

    private final TeamsPlus plugin;
    public ChatCmd(TeamsPlus teamsPlus) {
        super("chat", "teamsplus.base.chat", "teamsplus.admin.chat", Arrays.asList("chats", "text", "message", "c"));
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

        Team team = Team.getTeam(player);

        if (team.isTeamChatEnabled(player)) {
            team.setTeamChat(player, false);
            player.sendMessage(langHandler.getMessage("messages.chat.disabled"));
        } else {
            team.setTeamChat(player, true);
            player.sendMessage(langHandler.getMessage("messages.chat.enabled"));        }
    }
}