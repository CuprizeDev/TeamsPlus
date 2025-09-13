package com.vitaldev.teamsplus.commands.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.teams.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CreateCmd extends SubCmd {

    private final TeamsPlus plugin;
    public CreateCmd(TeamsPlus teamsPlus) {
        super("create", "teamsplus.base.create", "teamsplus.admin.create", Arrays.asList("create", "creates", "new"));
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

        if (Team.hasTeam(player)) {
            player.sendMessage(langHandler.getMessage("messages.already-in-team"));
            return;
        }

        player.sendMessage(langHandler.getMessage("messages.chest.must-place"));
    }
}
