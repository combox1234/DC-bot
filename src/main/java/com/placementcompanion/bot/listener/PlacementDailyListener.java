package com.placementcompanion.bot.listener;

import com.placementcompanion.bot.entity.ChallengeCompletion;
import com.placementcompanion.bot.repository.ChallengeCompletionRepository;
import com.placementcompanion.bot.service.PlacementDailyService;
import com.placementcompanion.bot.service.XpService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.LocalDate;

@Component
public class PlacementDailyListener extends ListenerAdapter {

    private final PlacementDailyService dailyService;
    private final ChallengeCompletionRepository completionRepo;
    private final XpService xpService;

    public PlacementDailyListener(PlacementDailyService dailyService, ChallengeCompletionRepository completionRepo, XpService xpService) {
        this.dailyService = dailyService;
        this.completionRepo = completionRepo;
        this.xpService = xpService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("daily")) {
            event.deferReply().queue();
            
            PlacementDailyService.DailyChallengeNode challenge = dailyService.getChallengeForToday();
            if (challenge == null) {
                event.getHook().sendMessage("❌ No daily challenge available today!").queue();
                return;
            }

            String userId = event.getUser().getId();
            boolean alreadyCompleted = completionRepo.existsByDiscordIdAndChallengeId(userId, challenge.getId());

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("📅 Daily Challenge: " + challenge.getTitle());
            embed.setDescription("**Difficulty:** " + challenge.getDifficulty() + " | **Category:** " + challenge.getCategory() + "\n\n" + challenge.getDescription());
            embed.setColor(Color.decode("#F1C40F"));
            embed.setFooter("Reward: +" + challenge.getXp_reward() + " XP");

            if (alreadyCompleted) {
                embed.appendDescription("\n\n✅ **You have already completed today's challenge!**");
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            } else {
                event.getHook().sendMessageEmbeds(embed.build())
                     .addActionRow(Button.success("daily_complete_" + challenge.getId() + "_" + challenge.getXp_reward(), "✅ Mark Complete"))
                     .queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] parts = event.getComponentId().split("_");
        if (parts.length == 4 && parts[0].equals("daily") && parts[1].equals("complete")) {
            String challengeId = parts[2];
            int xpReward = Integer.parseInt(parts[3]);
            String userId = event.getUser().getId();
            String username = event.getUser().getName();

            if (completionRepo.existsByDiscordIdAndChallengeId(userId, challengeId)) {
                event.reply("⚠️ You have already completed this challenge!").setEphemeral(true).queue();
                return;
            }

            completionRepo.save(new ChallengeCompletion(userId, challengeId, LocalDate.now()));
            boolean leveledUp = xpService.addXp(userId, username, xpReward);

            String replyMsg = "🎉 Challenge marked as complete! You earned **" + xpReward + " XP**.";
            if (leveledUp) {
                replyMsg += "\n\n🎊 **LEVEL UP!**";
            }

            event.editComponents(java.util.Collections.emptyList()).queue();
            event.getChannel().sendMessage(event.getUser().getAsMention() + " " + replyMsg).queue();
        }
    }
}
