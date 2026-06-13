package com.placementcompanion.bot.listener;

import com.placementcompanion.bot.entity.QuizHistory;
import com.placementcompanion.bot.model.Question;
import com.placementcompanion.bot.repository.QuizHistoryRepository;
import com.placementcompanion.bot.service.QuestionService;
import com.placementcompanion.bot.service.XpService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class QuizListener extends ListenerAdapter {

    private final QuestionService questionService;
    private final XpService xpService;
    private final QuizHistoryRepository quizHistoryRepo;

    // In-memory cache to map message ID to Question ID for button clicks
    private final Map<String, Question> activeQuizzes = new HashMap<>();

    public QuizListener(QuestionService questionService, XpService xpService, QuizHistoryRepository quizHistoryRepo) {
        this.questionService = questionService;
        this.xpService = xpService;
        this.quizHistoryRepo = quizHistoryRepo;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("quiz")) {
            String subcommand = event.getSubcommandName();
            if ("stats".equals(subcommand)) {
                handleQuizStats(event);
                return;
            }
            // Default: start a quiz (the existing "start" subcommand or bare /quiz)
            String category = "any";
            if (event.getOption("category") != null) {
                category = event.getOption("category").getAsString();
            }
            sendQuestionToInteraction(event, category);
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] parts = event.getComponentId().split("_");
        
        if (event.getComponentId().equals("quiz_stop")) {
            event.editComponents(java.util.Collections.emptyList()).queue(); // Remove buttons
            event.getChannel().sendMessage("🛑 Quiz session ended. Great job!").queue();
            return;
        }
        
        if (parts[0].equals("quiznext")) {
            String category = parts[1];
            event.editComponents(java.util.Collections.emptyList()).queue(); // Remove buttons from old message
            sendQuestionToChannel(event.getChannel(), category);
            return;
        }

        if (parts.length != 3 || !parts[0].equals("quiz")) return;

        int questionId = Integer.parseInt(parts[1]);
        int selectedOption = Integer.parseInt(parts[2]);

        Question q = activeQuizzes.get(event.getMessageId());
        if (q == null || q.getId() != questionId) {
            event.reply("❌ This quiz has expired.").setEphemeral(true).queue();
            return;
        }

        boolean isCorrect = (selectedOption == q.getAnswer());
        String userId = event.getUser().getId();
        String username = event.getUser().getName();

        // Save History
        quizHistoryRepo.save(new QuizHistory(userId, questionId, q.getCategory(), isCorrect));

        EmbedBuilder embed = new EmbedBuilder();
        if (isCorrect) {
            boolean leveledUp = xpService.addXp(userId, username, 15);
            embed.setTitle("✅ Correct! (+15 XP)");
            embed.setColor(Color.decode("#2ECC71"));
            embed.setDescription(q.getExplanation());
            if (leveledUp) {
                embed.appendDescription("\n\n🎉 **LEVEL UP!**");
            }
        } else {
            embed.setTitle("❌ Incorrect");
            embed.setColor(Color.decode("#E74C3C"));
            embed.setDescription("**Correct Answer:** Option " + (char)('A' + q.getAnswer()) + "\n\n" + q.getExplanation());
        }

        // Swap buttons for Next and Stop
        event.editMessageEmbeds(embed.build()).setActionRow(
            Button.success("quiznext_" + q.getCategory(), "⏭️ Next Question"),
            Button.danger("quiz_stop", "🛑 Stop")
        ).queue();

        activeQuizzes.remove(event.getMessageId());
    }

    private void sendQuestionToInteraction(SlashCommandInteractionEvent event, String category) {
        Question q = questionService.getRandomQuestion(category);
        if (q == null) {
            event.reply("❌ No questions found for category: " + category).setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(buildQuestionEmbed(q, category))
             .addActionRow(buildQuestionButtons(q))
             .queue(hook -> hook.retrieveOriginal().queue(msg -> activeQuizzes.put(msg.getId(), q)));
    }

    private void sendQuestionToChannel(net.dv8tion.jda.api.entities.channel.middleman.MessageChannel channel, String category) {
        Question q = questionService.getRandomQuestion(category);
        if (q == null) {
            channel.sendMessage("❌ No questions found for category: " + category).queue();
            return;
        }
        channel.sendMessageEmbeds(buildQuestionEmbed(q, category))
               .addActionRow(buildQuestionButtons(q))
               .queue(msg -> activeQuizzes.put(msg.getId(), q));
    }

    private net.dv8tion.jda.api.entities.MessageEmbed buildQuestionEmbed(Question q, String category) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🧠 " + category.toUpperCase() + " Quiz");
        embed.setDescription("**" + q.getQuestion() + "**");
        embed.setColor(Color.decode("#3498DB"));
        for (int i = 0; i < q.getOptions().size(); i++) {
            embed.addField("Option " + (char)('A' + i), q.getOptions().get(i), false);
        }
        return embed.build();
    }

    private java.util.List<Button> buildQuestionButtons(Question q) {
        return java.util.Arrays.asList(
            Button.primary("quiz_" + q.getId() + "_0", "A"),
            Button.primary("quiz_" + q.getId() + "_1", "B"),
            Button.primary("quiz_" + q.getId() + "_2", "C"),
            Button.primary("quiz_" + q.getId() + "_3", "D")
        );
    }

    private void handleQuizStats(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        String userId = event.getUser().getId();
        List<QuizHistory> history = quizHistoryRepo.findByDiscordId(userId);

        if (history.isEmpty()) {
            event.getHook().sendMessage("📊 You haven't taken any quizzes yet! Use `/quiz` to start.").queue();
            return;
        }

        long total = history.size();
        long correct = history.stream().filter(q -> Boolean.TRUE.equals(q.getIsCorrect())).count();
        double globalAccuracy = (double) correct / total * 100;

        // Group by category
        Map<String, Long> categoryTotal = history.stream()
                .collect(Collectors.groupingBy(QuizHistory::getCategory, Collectors.counting()));
        Map<String, Long> categoryCorrect = history.stream()
                .filter(q -> Boolean.TRUE.equals(q.getIsCorrect()))
                .collect(Collectors.groupingBy(QuizHistory::getCategory, Collectors.counting()));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📊 Quiz Statistics for " + event.getUser().getName());
        embed.setColor(Color.decode("#3498DB"));
        embed.addField("🎯 Overall Accuracy", String.format("%.1f%% (%d/%d)", globalAccuracy, correct, total), false);

        StringBuilder breakdown = new StringBuilder();
        for (String cat : categoryTotal.keySet()) {
            long catTotal = categoryTotal.get(cat);
            long catCorrect = categoryCorrect.getOrDefault(cat, 0L);
            double catAcc = (double) catCorrect / catTotal * 100;
            String bar = buildAccuracyBar(catAcc);
            breakdown.append(String.format("**%s**: %s %.0f%% (%d/%d)\n", cat.toUpperCase(), bar, catAcc, catCorrect, catTotal));
        }
        embed.addField("📂 Category Breakdown", breakdown.toString(), false);
        embed.setFooter("Keep practicing to improve your accuracy!");

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    private String buildAccuracyBar(double percent) {
        int filled = (int) Math.round(percent / 10.0);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        return bar.toString();
    }
}
