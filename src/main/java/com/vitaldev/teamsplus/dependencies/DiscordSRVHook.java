package com.vitaldev.teamsplus.dependencies;

import com.vitaldev.teamsplus.TeamsPlus;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;

// Optional DiscordSRV integration.
// <p>
// This class references DiscordSRV classes directly, so it must only be
// constructed when DiscordSRV is confirmed present on the classpath.
// {@link DependencyManager} guards construction with a try/catch.
public class DiscordSRVHook {

    private final TeamsPlus plugin;

    public DiscordSRVHook(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    // Sends an embed to a named DiscordSRV channel.
// @param channel      the DiscordSRV game-channel name (from DiscordSRV config)
// @param embedBuilder the embed to send
    public void sendEmbed(String channel, EmbedBuilder embedBuilder) {
        TextChannel textChannel = DiscordSRV.getPlugin()
                .getDestinationTextChannelForGameChannelName(channel);
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }
}
