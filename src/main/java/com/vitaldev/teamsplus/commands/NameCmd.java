package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.commands.BypassCmd;
import com.vitaldev.teamsplus.features.permissions.PermissableAction;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ChatUtil;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class NameCmd extends SubCmd {

    private final TeamsPlus plugin;

    public NameCmd(TeamsPlus teamsPlus) {
        super("name", "teamsplus.base.name", "", Arrays.asList("tag", "names", "rename"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        ConfigHandler langHandler = this.plugin.getLangFile();

        if (!(sender instanceof Player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(langHandler.getMessage("messages.invalid-sub-args")
                    .replace("{ARGS}", "<player>")
                    .replace("{COMMAND}", "/team rename"));
            return;
        }

        Player player = (Player) sender;
        
        Team team = Team.getTeam(player);
        if (!team.canDo(player, PermissableAction.SETTINGS) && !BypassCmd.isBypassing(player)) {
            player.sendMessage(langHandler.getMessage("messages.permissions.denied"));
            return;
        }
        
        String name = args[1];

        if (name.equals(Team.getTeam(player).getTeamName())) {
            player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.tags.same")));
            return;
        }

        for (UUID uuid: Team.getTeamList()) {
            if (name.equalsIgnoreCase(Team.getTeam(uuid).getTeamName())) {
                player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.tags.taken")));
                return;
            }

        }

        if (ChatUtil.containsSpecialCharacters(name)) {
            player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.tags.contains-special")));
            return;
        }

        if (!ChatUtil.isStringWithinLength(name, plugin.getConfig().getInt("teams.name.minimum"), plugin.getConfig().getInt("teams.name.maximum"))) {
            player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.tags.within-length")));
            return;
        }

        com.vitaldev.teamsplus.events.TeamNameEvent nameEvent = new com.vitaldev.teamsplus.events.TeamNameEvent(player, team, team.getTeamName(), name);
        org.bukkit.Bukkit.getPluginManager().callEvent(nameEvent);
        if (nameEvent.isCancelled()) return;

        team.setTeamName(nameEvent.getNewName());

        this.plugin.getLogManager().logEvent(team, com.vitaldev.teamsplus.features.logs.LogType.RENAME, player, player.getLocation(), Map.of("new_name", name));
        team.updateHologram();
        player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.tags.changed").replace("{TEAM}", name)));
    }
}
