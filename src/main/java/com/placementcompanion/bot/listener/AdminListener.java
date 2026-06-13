package com.placementcompanion.bot.listener;

import com.placementcompanion.bot.entity.GuildConfig;
import com.placementcompanion.bot.entity.User;
import com.placementcompanion.bot.repository.GuildConfigRepository;
import com.placementcompanion.bot.repository.QuizHistoryRepository;
import com.placementcompanion.bot.repository.UserRepository;
import com.placementcompanion.bot.service.DailyBroadcastService;
import com.placementcompanion.bot.service.QuestionService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class AdminListener extends ListenerAdapter {

    private final GuildConfigRepository guildConfigRepository;
    private final DailyBroadcastService dailyBroadcastService;
    private final QuestionService questionService;
    private final UserRepository userRepository;
    private final QuizHistoryRepository quizHistoryRepository;

    public AdminListener(GuildConfigRepository guildConfigRepository,
                         DailyBroadcastService dailyBroadcastService,
                         QuestionService questionService,
                         UserRepository userRepository,
                         QuizHistoryRepository quizHistoryRepository) {
        this.guildConfigRepository = guildConfigRepository;
        this.dailyBroadcastService = dailyBroadcastService;
        this.questionService = questionService;
        this.userRepository = userRepository;
        this.quizHistoryRepository = quizHistoryRepository;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("setup") && !event.getName().equals("admin")) return;

        // IMMEDIATELY defer reply — Discord gives only 3 seconds!
        event.deferReply(true).queue();

        try {
            if (event.getGuild() == null || event.getMember() == null) {
                event.getHook().sendMessage("❌ This command can only be used inside a server, not in DMs.").queue();
                return;
            }

            if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                event.getHook().sendMessage("❌ You need **Manage Server** permissions to use this command.").queue();
                return;
            }

            if (event.getName().equals("setup")) {
                handleSetupCommand(event);
            } else if (event.getName().equals("admin")) {
                handleAdminCommand(event);
            }
        } catch (Exception e) {
            System.err.println("[AdminListener] EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            event.getHook().sendMessage("❌ An error occurred: " + e.getMessage()).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        if (componentId.startsWith("admin_reset_confirm_")) {
            String targetUserId = componentId.replace("admin_reset_confirm_", "");
            Optional<User> userOpt = userRepository.findById(targetUserId);
            if (userOpt.isEmpty()) {
                event.reply("❌ User not found in database.").setEphemeral(true).queue();
                return;
            }
            User user = userOpt.get();
            user.setXp(0);
            user.setLevel(1);
            user.setCurrentStreak(0);
            user.setBestStreak(0);
            userRepository.save(user);

            event.editComponents(java.util.Collections.emptyList()).queue();
            event.getHook().sendMessage("✅ User **" + user.getUsername() + "** has been reset (XP=0, Level=1, Streaks=0).").queue();
        } else if (componentId.startsWith("admin_reset_cancel")) {
            event.editComponents(java.util.Collections.emptyList()).queue();
            event.getHook().sendMessage("❌ Reset cancelled.").queue();
        }
    }

    // ===== /setup commands (existing) =====
    private void handleSetupCommand(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        if ("daily".equals(subcommand)) {
            handleSetupDaily(event);
        } else if ("test_daily".equals(subcommand)) {
            handleTestDaily(event);
        } else {
            event.getHook().sendMessage("Unknown subcommand.").queue();
        }
    }

    // ===== /admin commands (new) =====
    private void handleAdminCommand(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        if ("add-question".equals(subcommand)) {
            handleAddQuestion(event);
        } else if ("remove-question".equals(subcommand)) {
            handleRemoveQuestion(event);
        } else if ("reset-user".equals(subcommand)) {
            handleResetUser(event);
        } else if ("stats".equals(subcommand)) {
            handleServerStats(event);
        } else {
            event.getHook().sendMessage("Unknown admin subcommand.").queue();
        }
    }

    private void handleAddQuestion(SlashCommandInteractionEvent event) {
        String category = event.getOption("category").getAsString();
        String questionText = event.getOption("question").getAsString();
        String optionsStr = event.getOption("options").getAsString(); // "A|B|C|D"
        int answer = (int) event.getOption("answer").getAsLong();
        String explanation = event.getOption("explanation").getAsString();

        List<String> options = Arrays.asList(optionsStr.split("\\|"));
        if (options.size() != 4) {
            event.getHook().sendMessage("❌ Please provide exactly 4 options separated by `|`.\nExample: `Option A|Option B|Option C|Option D`").queue();
            return;
        }
        if (answer < 0 || answer > 3) {
            event.getHook().sendMessage("❌ Answer must be 0 (A), 1 (B), 2 (C), or 3 (D).").queue();
            return;
        }

        boolean success = questionService.addQuestion(category, questionText, options, answer, explanation);
        if (success) {
            event.getHook().sendMessage("✅ Question added successfully! Total questions: " + questionService.getTotalQuestionCount()).queue();
        } else {
            event.getHook().sendMessage("❌ Failed to save the question.").queue();
        }
    }

    private void handleRemoveQuestion(SlashCommandInteractionEvent event) {
        int questionId = (int) event.getOption("id").getAsLong();
        boolean removed = questionService.removeQuestion(questionId);
        if (removed) {
            event.getHook().sendMessage("✅ Question #" + questionId + " removed. Remaining questions: " + questionService.getTotalQuestionCount()).queue();
        } else {
            event.getHook().sendMessage("❌ No question found with ID #" + questionId + ".").queue();
        }
    }

    private void handleResetUser(SlashCommandInteractionEvent event) {
        net.dv8tion.jda.api.entities.User target = event.getOption("user").getAsUser();
        String targetId = target.getId();

        Optional<User> userOpt = userRepository.findById(targetId);
        if (userOpt.isEmpty()) {
            event.getHook().sendMessage("❌ User **" + target.getName() + "** has no data in the system.").queue();
            return;
        }

        User user = userOpt.get();
        event.getHook().sendMessage("⚠️ Are you sure you want to reset **" + user.getUsername() + "**? (XP: " + user.getXp() + ", Level: " + user.getLevel() + ", Best Streak: " + user.getBestStreak() + ")")
                .addActionRow(
                        Button.danger("admin_reset_confirm_" + targetId, "✅ Confirm Reset"),
                        Button.secondary("admin_reset_cancel", "❌ Cancel")
                ).queue();
    }

    private void handleServerStats(SlashCommandInteractionEvent event) {
        long totalUsers = userRepository.count();
        long totalQuizzes = quizHistoryRepository.count();
        int totalQuestions = questionService.getTotalQuestionCount();

        // Calculate total XP across all users
        List<User> allUsers = userRepository.findAll();
        long totalXp = allUsers.stream().mapToLong(User::getXp).sum();
        int highestLevel = allUsers.stream().mapToInt(User::getLevel).max().orElse(0);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📈 Server Statistics");
        embed.setColor(Color.decode("#E67E22"));
        embed.addField("👥 Total Users", String.valueOf(totalUsers), true);
        embed.addField("🧠 Quizzes Taken", String.valueOf(totalQuizzes), true);
        embed.addField("📚 Question Bank", String.valueOf(totalQuestions), true);
        embed.addField("✨ Total XP Awarded", String.valueOf(totalXp), true);
        embed.addField("⭐ Highest Level", String.valueOf(highestLevel), true);

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    // ===== Setup helpers (existing) =====
    private void handleSetupDaily(SlashCommandInteractionEvent event) {
        var channelOption = event.getOption("channel");
        if (channelOption == null) {
            event.getHook().sendMessage("❌ Please provide a channel.").queue();
            return;
        }

        String guildId = event.getGuild().getId();
        var channel = channelOption.getAsChannel();

        GuildConfig config = guildConfigRepository.findById(guildId).orElse(new GuildConfig(guildId, null));
        config.setDailyBroadcastChannelId(channel.getId());
        guildConfigRepository.save(config);

        event.getHook().sendMessage("✅ Daily DSA Broadcasts will now be sent to " + channel.getAsMention()).queue();
    }

    private void handleTestDaily(SlashCommandInteractionEvent event) {
        try {
            dailyBroadcastService.broadcastDailyChallenge();
            event.getHook().sendMessage("✅ Broadcast triggered!").queue();
        } catch (Exception e) {
            event.getHook().sendMessage("❌ Error triggering broadcast: " + e.getMessage()).queue();
        }
    }
}
