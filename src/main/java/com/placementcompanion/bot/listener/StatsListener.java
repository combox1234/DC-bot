package com.placementcompanion.bot.listener;

import com.placementcompanion.bot.entity.QuizHistory;
import com.placementcompanion.bot.entity.User;
import com.placementcompanion.bot.repository.QuizHistoryRepository;
import com.placementcompanion.bot.repository.UserRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class StatsListener extends ListenerAdapter {

    private final UserRepository userRepository;
    private final QuizHistoryRepository quizHistoryRepository;

    public StatsListener(UserRepository userRepository, QuizHistoryRepository quizHistoryRepository) {
        this.userRepository = userRepository;
        this.quizHistoryRepository = quizHistoryRepository;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("stats")) return;

        event.deferReply(true).queue();

        // If a user mention is provided, show that user's stats; otherwise show the caller's stats
        net.dv8tion.jda.api.entities.User targetUser;
        if (event.getOption("user") != null) {
            targetUser = event.getOption("user").getAsUser();
        } else {
            targetUser = event.getUser();
        }

        String discordId = targetUser.getId();
        Optional<User> userOpt = userRepository.findById(discordId);

        if (userOpt.isEmpty()) {
            event.getHook().sendMessage("❌ " + targetUser.getName() + " has no data yet. They need to use the bot first!").queue();
            return;
        }

        User user = userOpt.get();

        // Calculate XP progress towards next level
        int currentXp = user.getXp();
        int currentLevel = user.getLevel();
        int xpForCurrentLevel = (int) Math.pow((currentLevel - 1) * 5.0, 2);
        int xpForNextLevel = (int) Math.pow(currentLevel * 5.0, 2);
        int xpInLevel = currentXp - xpForCurrentLevel;
        int xpNeeded = xpForNextLevel - xpForCurrentLevel;
        double progress = xpNeeded > 0 ? (double) xpInLevel / xpNeeded : 1.0;
        progress = Math.min(1.0, Math.max(0.0, progress));

        String progressBar = buildProgressBar(progress, 10);

        // Quiz accuracy
        List<QuizHistory> quizHistory = quizHistoryRepository.findByDiscordId(discordId);
        long totalQuizzes = quizHistory.size();
        long correctQuizzes = quizHistory.stream().filter(q -> Boolean.TRUE.equals(q.getIsCorrect())).count();
        double accuracy = totalQuizzes > 0 ? (double) correctQuizzes / totalQuizzes * 100 : 0;

        // Category breakdown
        Map<String, Long> categoryTotal = quizHistory.stream()
                .collect(Collectors.groupingBy(QuizHistory::getCategory, Collectors.counting()));
        Map<String, Long> categoryCorrect = quizHistory.stream()
                .filter(q -> Boolean.TRUE.equals(q.getIsCorrect()))
                .collect(Collectors.groupingBy(QuizHistory::getCategory, Collectors.counting()));

        StringBuilder categoryStats = new StringBuilder();
        for (String cat : categoryTotal.keySet()) {
            long total = categoryTotal.getOrDefault(cat, 0L);
            long correct = categoryCorrect.getOrDefault(cat, 0L);
            double catAccuracy = total > 0 ? (double) correct / total * 100 : 0;
            categoryStats.append(String.format("**%s**: %.0f%% (%d/%d)\n", cat.toUpperCase(), catAccuracy, correct, total));
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📊 Stats for " + targetUser.getName());
        embed.setThumbnail(targetUser.getEffectiveAvatarUrl());
        embed.setColor(Color.decode("#9B59B6"));

        embed.addField("⭐ Level", String.valueOf(currentLevel), true);
        embed.addField("✨ Total XP", String.valueOf(currentXp), true);
        embed.addField("🔥 Current Streak", String.valueOf(user.getCurrentStreak()), true);
        embed.addField("🏆 Best Streak", String.valueOf(user.getBestStreak()), true);
        embed.addField("📈 Level Progress", progressBar + " " + String.format("%.0f%%", progress * 100), false);
        embed.addField("🧠 Quiz Accuracy", String.format("%.1f%% (%d/%d)", accuracy, correctQuizzes, totalQuizzes), false);

        if (!categoryStats.isEmpty()) {
            embed.addField("📂 Category Breakdown", categoryStats.toString(), false);
        }

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    private String buildProgressBar(double progress, int segments) {
        int filled = (int) Math.round(progress * segments);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < segments; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        return bar.toString();
    }
}
