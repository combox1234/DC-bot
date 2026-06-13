package com.placementcompanion.bot.listener;

import com.placementcompanion.bot.service.InterviewService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class InterviewListener extends ListenerAdapter {

    private final InterviewService interviewService;

    public InterviewListener(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("interview")) {
            event.deferReply(true).queue();
            
            String subcommand = event.getSubcommandName();
            String userId = event.getUser().getId();

            if ("join".equals(subcommand)) {
                if (interviewService.joinQueue(userId)) {
                    event.getHook().sendMessage("✅ You have joined the mock interview matchmaking queue! I will DM you when a partner is found. Make sure your DMs are open.").queue();
                } else {
                    event.getHook().sendMessage("⚠️ You are already in the matchmaking queue! Please wait for a partner.").queue();
                }
            } else if ("leave".equals(subcommand)) {
                if (interviewService.leaveQueue(userId)) {
                    event.getHook().sendMessage("👋 You have left the mock interview matchmaking queue.").queue();
                } else {
                    event.getHook().sendMessage("⚠️ You were not in the matchmaking queue.").queue();
                }
            }
        }
    }
}
