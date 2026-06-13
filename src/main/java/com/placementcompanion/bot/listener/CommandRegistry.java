package com.placementcompanion.bot.listener;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.springframework.stereotype.Component;

@Component
public class CommandRegistry {

    private final JDA jda;
    private final QuizListener quizListener;
    private final LeaderboardListener leaderboardListener;
    private final LeetCodeListener leetCodeListener;
    private final HelpListener helpListener;
    private final AdminListener adminListener;
    private final GitHubListener gitHubListener;
    private final CompanyListener companyListener;
    private final NotesListener notesListener;
    private final InterviewListener interviewListener;
    private final PlacementDailyListener placementDailyListener;
    private final StatsListener statsListener;
    private final ReminderListener reminderListener;

    public CommandRegistry(JDA jda,
                           QuizListener quizListener,
                           LeaderboardListener leaderboardListener,
                           LeetCodeListener leetCodeListener,
                           HelpListener helpListener,
                           AdminListener adminListener,
                           GitHubListener gitHubListener,
                           CompanyListener companyListener,
                           NotesListener notesListener,
                           InterviewListener interviewListener,
                           PlacementDailyListener placementDailyListener,
                           StatsListener statsListener,
                           ReminderListener reminderListener) {
        this.jda = jda;
        this.quizListener = quizListener;
        this.leaderboardListener = leaderboardListener;
        this.leetCodeListener = leetCodeListener;
        this.helpListener = helpListener;
        this.adminListener = adminListener;
        this.gitHubListener = gitHubListener;
        this.companyListener = companyListener;
        this.notesListener = notesListener;
        this.interviewListener = interviewListener;
        this.placementDailyListener = placementDailyListener;
        this.statsListener = statsListener;
        this.reminderListener = reminderListener;
    }

    @PostConstruct
    public void registerCommands() {
        if (jda == null) return;

        // Register event listeners
        jda.addEventListener(quizListener);
        jda.addEventListener(leaderboardListener);
        jda.addEventListener(leetCodeListener);
        jda.addEventListener(helpListener);
        jda.addEventListener(adminListener);
        jda.addEventListener(gitHubListener);
        jda.addEventListener(companyListener);
        jda.addEventListener(notesListener);
        jda.addEventListener(interviewListener);
        jda.addEventListener(placementDailyListener);
        jda.addEventListener(statsListener);
        jda.addEventListener(reminderListener);

        // Push slash commands to Discord
        jda.updateCommands().addCommands(
            // ===== /setup (admin) =====
            Commands.slash("setup", "Administrator configuration commands")
                .addSubcommands(
                    new SubcommandData("daily", "Set the channel for Daily DSA Broadcasts")
                        .addOption(OptionType.CHANNEL, "channel", "The channel to send broadcasts to", true),
                    new SubcommandData("test_daily", "Manually trigger the Daily DSA Broadcast")
                ),

            // ===== /admin (new admin management) =====
            Commands.slash("admin", "Admin management commands")
                .addSubcommands(
                    new SubcommandData("add-question", "Add a new quiz question")
                        .addOption(OptionType.STRING, "category", "Category (dsa, aptitude, python, aiml, verbal)", true)
                        .addOption(OptionType.STRING, "question", "The question text", true)
                        .addOption(OptionType.STRING, "options", "4 options separated by | (e.g. A|B|C|D)", true)
                        .addOption(OptionType.INTEGER, "answer", "Correct answer index (0=A, 1=B, 2=C, 3=D)", true)
                        .addOption(OptionType.STRING, "explanation", "Explanation for the correct answer", true),
                    new SubcommandData("remove-question", "Remove a quiz question by ID")
                        .addOption(OptionType.INTEGER, "id", "The question ID to remove", true),
                    new SubcommandData("reset-user", "Reset a user's XP, level, and streaks")
                        .addOption(OptionType.USER, "user", "The user to reset", true),
                    new SubcommandData("stats", "View server-wide engagement statistics")
                ),

            // ===== /github =====
            Commands.slash("github", "GitHub integration commands")
                .addSubcommands(
                    new SubcommandData("link", "Link your GitHub account")
                        .addOption(OptionType.STRING, "username", "Your GitHub username", true),
                    new SubcommandData("stats", "View your GitHub stats"),
                    new SubcommandData("activity", "View your recent public GitHub activity"),
                    new SubcommandData("commits", "View recent commits for a repository")
                        .addOption(OptionType.STRING, "owner", "Repository owner", true)
                        .addOption(OptionType.STRING, "repo", "Repository name", true),
                    new SubcommandData("structure", "View the file structure of a repository")
                        .addOption(OptionType.STRING, "owner", "Repository owner", true)
                        .addOption(OptionType.STRING, "repo", "Repository name", true)
                        .addOption(OptionType.STRING, "path", "Path in the repository (optional)", false),
                    new SubcommandData("file", "View a file inside a repository")
                        .addOption(OptionType.STRING, "owner", "Repository owner", true)
                        .addOption(OptionType.STRING, "repo", "Repository name", true)
                        .addOption(OptionType.STRING, "path", "File path", true)
                ),

            // ===== /company =====
            Commands.slash("company", "View recruitment guides and FAQs for top companies")
                .addOption(OptionType.STRING, "name", "The name of the company", true, true),

            // ===== /notes =====
            Commands.slash("notes", "Manage your personal quick notes")
                .addSubcommands(
                    new SubcommandData("add", "Add a new personal note")
                        .addOption(OptionType.STRING, "title", "Title of the note", true)
                        .addOption(OptionType.STRING, "content", "Content of the note", true),
                    new SubcommandData("list", "List your saved notes"),
                    new SubcommandData("view", "View a specific note")
                        .addOption(OptionType.STRING, "title", "Title of the note", true, true),
                    new SubcommandData("delete", "Delete a specific note")
                        .addOption(OptionType.STRING, "title", "Title of the note to delete", true, true)
                ),

            // ===== /interview (Feature 1) =====
            Commands.slash("interview", "Mock interview matchmaking")
                .addSubcommands(
                    new SubcommandData("join", "Join the mock interview matchmaking queue"),
                    new SubcommandData("leave", "Leave the mock interview matchmaking queue")
                ),

            // ===== /daily (Feature 2) =====
            Commands.slash("daily", "View today's placement daily challenge"),

            // ===== /stats (Feature 3) =====
            Commands.slash("stats", "View your or another user's stats")
                .addOptions(new OptionData(OptionType.USER, "user", "The user to view stats for (optional)", false)),

            // ===== /remind (Feature 4) =====
            Commands.slash("remind", "Manage your daily study reminders")
                .addSubcommands(
                    new SubcommandData("set", "Set a daily reminder time")
                        .addOption(OptionType.STRING, "time", "Time in HH:mm 24-hour format (IST), e.g. 21:00", true),
                    new SubcommandData("off", "Disable your daily reminder"),
                    new SubcommandData("status", "Check your current reminder status")
                ),

            // ===== /quiz (Feature 5 — updated with stats subcommand) =====
            Commands.slash("quiz", "Start an interactive quiz session")
                .addSubcommands(
                    new SubcommandData("start", "Start a quiz")
                        .addOptions(new OptionData(OptionType.STRING, "category", "Choose a category")
                            .addChoice("DSA", "dsa")
                            .addChoice("Aptitude", "aptitude")
                            .addChoice("Python", "python")
                            .addChoice("AI/ML", "aiml")
                            .addChoice("Verbal", "verbal")),
                    new SubcommandData("stats", "View your quiz accuracy per category")
                ),

            // ===== /help =====
            Commands.slash("help", "View a list of all available commands"),

            // ===== /leaderboard =====
            Commands.slash("leaderboard", "View the top users")
                .addOptions(new OptionData(OptionType.STRING, "period", "Time period")
                    .addChoice("All Time", "all")
                    .addChoice("This Week", "week")
                    .addChoice("This Month", "month")),

            // ===== /streak =====
            Commands.slash("streak", "View your current daily streak"),

            // ===== /lc =====
            Commands.slash("lc", "LeetCode commands")
                .addSubcommandGroups(
                    new SubcommandGroupData("leaderboard", "LeetCode leaderboards").addSubcommands(
                        new SubcommandData("solved", "Top 10 by LeetCode solves this week"),
                        new SubcommandData("rating", "Top 10 by LeetCode contest rating")
                    )
                )
                .addSubcommands(
                    new SubcommandData("link", "Link your LeetCode account")
                            .addOption(OptionType.STRING, "username", "Your LeetCode username", true),
                    new SubcommandData("stats", "View your LeetCode stats"),
                    new SubcommandData("daily", "View today's LeetCode daily challenge"),
                    new SubcommandData("solved", "Log a solved LeetCode problem")
                            .addOption(OptionType.STRING, "problem_slug", "The problem slug (e.g., 'two-sum')", true),
                    new SubcommandData("streak", "View your LeetCode solve streak"),
                    new SubcommandData("solution", "Get solution resources for a LeetCode problem")
                            .addOption(OptionType.STRING, "problem_slug", "The URL slug of the problem (e.g., two-sum)", true),
                    new SubcommandData("contest", "View upcoming LeetCode contests"),
                    new SubcommandData("help", "View all LeetCode commands")
                )
        ).queue();

        System.out.println("Slash commands pushed to Discord!");
    }
}
