package com.vitaldev.teamsplus.dependencies;

import com.vitaldev.teamsplus.TeamsPlus;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;

public class DiscordSRVHook {

    private TeamsPlus plugin;
    private static boolean isEnabled;

    public DiscordSRVHook(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void initiateDiscordSRV() {
        isEnabled = plugin.getServer().getPluginManager().isPluginEnabled("DiscordSRV");
    }

    public static void sendEmbed(String channel, EmbedBuilder embedBuilder) {
        TextChannel textChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channel);
        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

}
