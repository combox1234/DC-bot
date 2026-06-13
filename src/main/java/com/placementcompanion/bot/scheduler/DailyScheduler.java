package com.placementcompanion.bot.scheduler;

import com.placementcompanion.bot.service.LeetCodeApiService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DailyScheduler {

    private final JDA jda;
    private final LeetCodeApiService leetCodeApiService;

    // Optional: Inject Channel ID if configured
    @Value("${discord.channel.leetcode:}")
    private String leetcodeChannelId;

    public DailyScheduler(JDA jda, LeetCodeApiService leetCodeApiService) {
        this.jda = jda;
        this.leetCodeApiService = leetCodeApiService;
    }

    // Run every day at 9:00 AM (CRON: Second Minute Hour Day Month Weekday)
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void postDailyLeetCode() {
        if (leetcodeChannelId == null || leetcodeChannelId.isEmpty()) return;

        TextChannel channel = jda.getTextChannelById(leetcodeChannelId);
        if (channel == null) return;

        Map<String, String> daily = leetCodeApiService.getDailyChallenge();
        if (daily == null) return;

        String msg = "🌅 **Good Morning! Here is today's LeetCode Daily Challenge:**\n\n" +
                     "**" + daily.get("title") + "** (" + daily.get("difficulty") + ")\n" +
                     daily.get("link") + "\n\n" +
                     "Use `/leetcode submit` when you are done!";
        
        channel.sendMessage(msg).queue();
    }
}
