package com.placementcompanion.bot.service;

import com.placementcompanion.bot.entity.GuildConfig;
import com.placementcompanion.bot.repository.GuildConfigRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class DailyBroadcastService {

    private final JDA jda;
    private final GuildConfigRepository guildConfigRepository;
    private final LeetCodeApiService leetCodeApiService;

    public DailyBroadcastService(JDA jda, GuildConfigRepository guildConfigRepository, LeetCodeApiService leetCodeApiService) {
        this.jda = jda;
        this.guildConfigRepository = guildConfigRepository;
        this.leetCodeApiService = leetCodeApiService;
    }

    // Runs every day at 9:00 AM server time (Cron: Second Minute Hour Day Month DayOfWeek)
    @Scheduled(cron = "0 0 9 * * *")
    public void broadcastDailyChallenge() {
        System.out.println("Running Daily DSA Broadcast...");
        Map<String, String> challenge = leetCodeApiService.getDailyChallenge();
        if (challenge == null) {
            System.err.println("Failed to fetch daily challenge for broadcast.");
            return;
        }

        String title = challenge.get("title");
        String slug = challenge.get("link");
        if (slug != null && slug.startsWith("https://leetcode.com/problems/")) {
            slug = slug.replace("https://leetcode.com/problems/", "").replace("/", "");
        }
        String difficulty = challenge.get("difficulty");
        String date = challenge.get("date");
        String url = challenge.get("link");

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🔥 LeetCode Daily Challenge: " + title, url);
        embed.setColor(getDifficultyColor(difficulty));
        embed.setDescription("Good morning! Here is your DSA challenge for today (" + date + ").");
        embed.addField("Difficulty", difficulty, true);
        embed.addField("Link", "[Click here to solve](" + url + ")", true);
        embed.setFooter("Type /lc solved " + slug + " after you finish to earn XP!");

        List<GuildConfig> configs = guildConfigRepository.findAll();
        for (GuildConfig config : configs) {
            if (config.getDailyBroadcastChannelId() == null) continue;

            Guild guild = jda.getGuildById(config.getGuildId());
            if (guild == null) continue;

            TextChannel channel = guild.getTextChannelById(config.getDailyBroadcastChannelId());
            if (channel != null) {
                channel.sendMessageEmbeds(embed.build()).queue(message -> {
                    // Create a discussion thread attached to the message
                    message.createThreadChannel("Discussion: " + title).queue(thread -> {
                        thread.sendMessage("Discuss your approach for **" + title + "** here! What time complexity did you get?").queue();
                    });
                });
            }
        }
    }

    private Color getDifficultyColor(String difficulty) {
        if (difficulty == null) return Color.GRAY;
        return switch (difficulty.toLowerCase()) {
            case "easy" -> Color.GREEN;
            case "medium" -> Color.ORANGE;
            case "hard" -> Color.RED;
            default -> Color.GRAY;
        };
    }
}
