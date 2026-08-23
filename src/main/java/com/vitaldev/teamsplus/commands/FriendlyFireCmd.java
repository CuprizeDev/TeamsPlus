package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.teamsplus.model.TeamPlayer;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class FriendlyFireCmd extends SubCmd {

    private final TeamsPlus plugin;

    public FriendlyFireCmd(TeamsPlus plugin) {
        super("friendlyfire", "teamsplus.base.friendlyfire", "teamsplus.admin.friendlyfire", Arrays.asList("ff"));
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ConfigHandler langHandler = plugin.getLangFile();

        if (!(sender instanceof Player player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }

        if (!Team.hasTeam(player)) {
            player.sendMessage(langHandler.getMessage("messages.need-team"));
            return;
        }

        TeamPlayer teamPlayer = TeamPlayer.get(player);
        teamPlayer.toggleFriendlyFire();

        String messageKey = teamPlayer.isFriendlyFireEnabled()
                ? "messages.friendly-fire.enabled"
                : "messages.friendly-fire.disabled";

        player.sendMessage(langHandler.getMessage(messageKey));
    }
}
