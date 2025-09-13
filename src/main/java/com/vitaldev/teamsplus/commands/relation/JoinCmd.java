package com.vitaldev.teamsplus.commands.relation;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.teams.PlayerRank;
import com.vitaldev.teamsplus.teams.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public class JoinCmd extends SubCmd {
    
    private final TeamsPlus plugin;

    public JoinCmd(TeamsPlus teamsPlus) {
        super("join", "teamsplus.base.join", "teamplus.admin.join", Arrays.asList("joins", "j", "unite"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        ConfigHandler langHandler = this.plugin.getLangFile();

        if (args.length < 2) {
            sender.sendMessage(langHandler.getMessage("messages.invalid-sub-args")
                    .replace("{ARGS}", "<team>")
                    .replace("{COMMAND}", "/team join"));
            return;
        }

        if (!(sender instanceof Player player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }

        if (Team.hasTeam(player)) {
            player.sendMessage(langHandler.getMessage("messages.already-in-team"));
            return;
        }

        String teamName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        for (UUID teamUUID : Team.getTeamList()) {
            Team team = Team.getTeam(teamUUID);

            if (team.getTeamName().equalsIgnoreCase(teamName)) {
                if (team.isInvited(player)) {

                    if (team.getMemberCount() >= this.plugin.getConfig().getInt("teams.member-limit")) {
                        player.sendMessage(langHandler.getMessage("messages.join.maximum-members"));
                        return;
                    }

                    player.sendMessage(langHandler.getMessage("messages.join.joined")
                            .replace("{TEAM}", teamName));
                    team.addMember(player, PlayerRank.MEMBER);
                    team.removeInvite(player);
                } else {
                    player.sendMessage(langHandler.getMessage("messages.join.not-invited"));
                }
                return;
            }
        }

        player.sendMessage(langHandler.getMessage("messages.invalid-team"));

    }
}
