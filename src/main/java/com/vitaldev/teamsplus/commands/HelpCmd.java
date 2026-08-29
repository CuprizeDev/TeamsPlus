package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.vitallibs.util.ChatUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HelpCmd extends SubCmd {

    private final TeamsPlus plugin;

    public HelpCmd(TeamsPlus teamsPlus) {
        super("help", "teamsplus.base.help", "", List.of("?"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtil.color("&cOnly players can view the interactive help menu."));
            return;
        }

        Player player = (Player) sender;
        
        Map<String, String> general = new LinkedHashMap<>();
        general.put("/team create <name>", "Create a new team");
        general.put("/team disband", "Disband your team");
        general.put("/team join <name>", "Join a team");
        general.put("/team leave", "Leave your current team");
        general.put("/team list", "View all teams");
        general.put("/team info [name]", "View team information");
        
        Map<String, String> management = new LinkedHashMap<>();
        management.put("/team invite <player>", "Invite a player to your team");
        management.put("/team invites", "View and manage pending invites");
        management.put("/team kick <player>", "Kick a member from your team");
        management.put("/team promote <player>", "Promote a member");
        management.put("/team demote <player>", "Demote a member");
        management.put("/team leader <player>", "Transfer team ownership");
        management.put("/team rename <name>", "Rename your team");
        
        Map<String, String> features = new LinkedHashMap<>();
        features.put("/team chest", "Open the main Team Chest GUI");
        features.put("/team chat", "Toggle private team chat");
        features.put("/team allychat", "Toggle ally chat");
        features.put("/team raid", "View raid history and active raids");
        features.put("/team stats", "View detailed team statistics");
        features.put("/team relation <ally|enemy|neutral>", "Manage relations");
        features.put("/team top", "View the top teams leaderboard");
        
        Map<String, String> locations = new LinkedHashMap<>();
        locations.put("/team sethome", "Set the team's main home");
        locations.put("/team home", "Teleport to the team home");
        locations.put("/team delhome", "Delete the team home");

        player.sendMessage(ChatUtil.color("&8&m----------------------------------------"));
        player.sendMessage(ChatUtil.color("         &b&lTeamsPlus &8\u00bb &f&lHelp Menu"));
        player.sendMessage(ChatUtil.color("&7&oHover over a command for details, click to insert!"));
        player.sendMessage(" ");
        
        sendCategory(player, "&3&lGeneral", general);
        sendCategory(player, "&e&lManagement", management);
        sendCategory(player, "&d&lFeatures", features);
        sendCategory(player, "&a&lLocations", locations);
        
        if (player.hasPermission("teamsplus.admin")) {
            Map<String, String> admin = new LinkedHashMap<>();
            admin.put("/team bypass", "Bypass claim and chest protections");
            admin.put("/team reload", "Reload the plugin configuration");
            admin.put("/team raid start <defender> <attacker>", "Force start a raid");
            sendCategory(player, "&c&lAdmin", admin);
        }
        
        player.sendMessage(ChatUtil.color("&8&m----------------------------------------"));
    }
    
    private void sendCategory(Player player, String categoryName, Map<String, String> commands) {
        player.sendMessage(ChatUtil.color(categoryName));
        for (Map.Entry<String, String> entry : commands.entrySet()) {
            String cmd = entry.getKey();
            String desc = entry.getValue();
            
            TextComponent comp = new TextComponent(ChatUtil.color("  &8\u00bb &b" + cmd));
            comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatUtil.color("&e" + desc + "\n&7&oClick to insert command")).create()));
            
            String suggestCmd = cmd.contains("<") ? cmd.substring(0, cmd.indexOf("<") - 1) : cmd;
            suggestCmd = suggestCmd.contains("[") ? suggestCmd.substring(0, suggestCmd.indexOf("[") - 1) : suggestCmd;
            
            comp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCmd + " "));
            player.spigot().sendMessage(comp);
        }
        player.sendMessage(" ");
    }
}
