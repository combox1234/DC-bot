package com.placementcompanion.bot.listener;

import com.placementcompanion.bot.entity.LcSubmission;
import com.placementcompanion.bot.entity.User;
import com.placementcompanion.bot.repository.LcSubmissionRepository;
import com.placementcompanion.bot.repository.UserRepository;
import com.placementcompanion.bot.service.LeetCodeApiService;
import com.placementcompanion.bot.service.StreakService;
import com.placementcompanion.bot.service.UserService;
import com.placementcompanion.bot.service.XpService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class LeetCodeListener extends ListenerAdapter {

    private final LeetCodeApiService apiService;
    private final UserRepository userRepository;
    private final LcSubmissionRepository submissionRepository;
    private final UserService userService;
    private final XpService xpService;
    private final StreakService streakService;

    public LeetCodeListener(LeetCodeApiService apiService, UserRepository userRepository,
                            LcSubmissionRepository submissionRepository, UserService userService, XpService xpService, StreakService streakService) {
        this.apiService = apiService;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.userService = userService;
        this.xpService = xpService;
        this.streakService = streakService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("lc")) return;

        String sub = event.getSubcommandName();
        String group = event.getSubcommandGroup();

        if (group != null && group.equals("leaderboard")) {
            if ("solved".equals(sub)) handleLeaderboardSolved(event);
            else if ("rating".equals(sub)) handleLeaderboardRating(event);
            return;
        }

        switch (sub) {
            case "link": handleLink(event); break;
            case "stats": handleStats(event); break;
            case "daily": handleDaily(event); break;
            case "solved": handleSolved(event); break;
            case "streak": handleStreak(event); break;
            case "solution": handleSolution(event); break;
            case "contest": handleContest(event); break;
            case "help": handleHelp(event); break;
            default: event.reply("Unknown command.").setEphemeral(true).queue();
        }
    }

    private void handleLink(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        CompletableFuture.runAsync(() -> {
            net.dv8tion.jda.api.interactions.commands.OptionMapping userOpt = event.getOption("username");
            if (userOpt == null) {
                event.getHook().sendMessage("❌ Please provide a username! Your Discord client might be out of sync. Try restarting Discord or typing the command again.").queue();
                return;
            }
            String username = userOpt.getAsString();
            Map<String, Object> profile = apiService.fetchUserProfile(username);

            if (profile == null) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("❌ Username Not Found")
                        .setDescription("LeetCode username **" + username + "** was not found.")
                        .setColor(Color.RED).build()).queue();
                return;
            }

            String discordId = event.getUser().getId();
            User u = userService.ensureUser(discordId, event.getUser().getName());
            u.setLeetcodeHandle(username);
            userRepository.save(u);

            event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("✅ LeetCode Linked!")
                    .setDescription("Your LeetCode handle **" + username + "** has been linked successfully.")
                    .addField("Total Solved", String.valueOf(profile.get("totalSolved")), true)
                    .setColor(Color.GREEN).build()).queue();
        });
    }

    private void handleStats(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        CompletableFuture.runAsync(() -> {
            String discordId = event.getUser().getId();
            Optional<User> u = userRepository.findById(discordId);

            if (u.isEmpty() || u.get().getLeetcodeHandle() == null) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("❌ No LeetCode Handle")
                        .setDescription("Link your LeetCode account first with `/lc link [username]`")
                        .setColor(Color.RED).build()).queue();
                return;
            }

            String handle = u.get().getLeetcodeHandle();
            Map<String, Object> profile = apiService.fetchUserProfile(handle);

            if (profile == null) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("⚠️ API Error")
                        .setDescription("Could not fetch stats.")
                        .setColor(Color.RED).build()).queue();
                return;
            }

            EmbedBuilder eb = new EmbedBuilder().setTitle("📊 LeetCode Stats — " + handle).setColor(0xFFA116)
                    .addField("Total Solved", "**" + profile.get("totalSolved") + "**", true)
                    .addField("🟢 Easy", String.valueOf(profile.get("easySolved")), true)
                    .addField("🟡 Medium", String.valueOf(profile.get("mediumSolved")), true)
                    .addField("🔴 Hard", String.valueOf(profile.get("hardSolved")), true)
                    .addField("🏆 Ranking", String.valueOf(profile.get("ranking")), true);

            if (profile.get("contestRating") != null) {
                eb.addField("⚡ Contest Rating", String.valueOf(profile.get("contestRating")), true);
                eb.addField("🌍 Global Rank", String.valueOf(profile.get("globalRanking")), true);
            }
            event.getHook().sendMessageEmbeds(eb.build()).queue();
        });
    }

    private void handleDaily(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        CompletableFuture.runAsync(() -> {
            Map<String, String> daily = apiService.getDailyChallenge();

            if (daily == null) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("⚠️ API Error")
                        .setColor(Color.RED).build()).queue();
                return;
            }

            event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("💻 LeetCode Daily — " + daily.get("title"))
                    .setDescription("[🔗 Solve on LeetCode](" + daily.get("link") + ")")
                    .addField("Difficulty", daily.get("difficulty"), true)
                    .addField("Date", daily.get("date"), true)
                    .setColor(0xFFA116).build()).queue();
        });
    }

    private void handleSolved(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        CompletableFuture.runAsync(() -> {
            try {
                net.dv8tion.jda.api.interactions.commands.OptionMapping slugOpt = event.getOption("problem_slug");
                if (slugOpt == null) {
                    event.getHook().sendMessage("❌ Please provide a problem slug! Your Discord client might be out of sync. Try restarting Discord or typing the command again.").queue();
                    return;
                }
                String slug = slugOpt.getAsString();
                String discordId = event.getUser().getId();
                User u = userService.ensureUser(discordId, event.getUser().getName());

                Map<String, String> prob = apiService.validateProblemSlug(slug);
                if (prob == null) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("❌ Problem Not Found / API Error")
                            .setDescription("Could not validate slug `" + slug + "`.").setColor(Color.RED).build()).queue();
                    return;
                }

                LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
                LocalDateTime endOfDay = startOfDay.plusDays(1);
                long todayCount = submissionRepository.countTodaySolves(discordId, slug, startOfDay, endOfDay);

                if (todayCount > 0) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("⚠️ Already Logged Today")
                            .setDescription("You've already logged `" + slug + "` today.").setColor(Color.RED).build()).queue();
                    return;
                }

                LcSubmission sub = new LcSubmission(discordId, slug, prob.get("difficulty"));
                submissionRepository.save(sub);

                xpService.addXp(discordId, event.getUser().getName(), 30);
                streakService.updateStreak(discordId, event.getUser().getName());

                event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("✅ Problem Solved!")
                        .setDescription("**" + prob.get("title") + "** " + prob.get("difficulty"))
                        .addField("🎯 XP Earned", "+30 XP", true)
                        .setColor(Color.GREEN).build()).queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessage("❌ An internal error occurred: " + e.getMessage()).queue();
            }
        });
    }

    private void handleStreak(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        List<LocalDate> dates = submissionRepository.findByDiscordIdOrderBySubmittedAtDesc(discordId)
                .stream().map(sub -> sub.getSubmittedAt().toLocalDate()).distinct().toList();

        if (dates.isEmpty()) {
            event.replyEmbeds(new EmbedBuilder().setTitle("💻 LC Streak")
                    .setDescription("No LeetCode solves logged yet! Use `/lc solved` to start.")
                    .setColor(0xFFA116).build()).setEphemeral(true).queue();
            return;
        }

        int streak = 0;
        LocalDate checkDate = LocalDate.now();
        for (LocalDate d : dates) {
            if (d.equals(checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }

        event.replyEmbeds(new EmbedBuilder().setTitle("💻 LeetCode Solve Streak").setColor(0xFFA116)
                .addField("Current Streak", "**" + streak + "** days", true)
                .addField("Total Solves Logged", String.valueOf(dates.size()), true).build()).queue();
    }

    private void handleSolution(SlashCommandInteractionEvent event) {
        net.dv8tion.jda.api.interactions.commands.OptionMapping slugOpt = event.getOption("problem_slug");
        if (slugOpt == null) {
            event.reply("❌ Please provide a problem slug! Your Discord client might be out of sync. Try restarting Discord or typing the command again.").setEphemeral(true).queue();
            return;
        }
        String slug = slugOpt.getAsString();
        String title = slug.replace("-", " ");
        title = title.substring(0, 1).toUpperCase() + title.substring(1);

        EmbedBuilder eb = new EmbedBuilder().setTitle("💡 Solutions: " + title)
                .setDescription("Here are the most active places to find solutions for **" + slug + "**:")
                .setColor(0xFFA116)
                .addField("LeetCode Discuss", "[Official Solutions Tab](https://leetcode.com/problems/" + slug + "/solutions/)", false)
                .addField("YouTube", "[Video Explanations](https://www.youtube.com/results?search_query=LeetCode+" + slug + "+solution)", false)
                .addField("GitHub (Python)", "[Python Code Solutions](https://github.com/search?q=leetcode+" + slug + "+language%3Apython)", false);
        event.replyEmbeds(eb.build()).queue();
    }

    private void handleContest(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        CompletableFuture.runAsync(() -> {
            List<Map<String, Object>> contests = apiService.fetchContests();

            if (contests == null) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("⚠️ API Error").setColor(Color.RED).build()).queue();
                return;
            }

            long now = Instant.now().getEpochSecond();
            List<Map<String, Object>> upcoming = contests.stream().filter(c -> (long) c.get("startTime") > now)
                    .sorted((a, b) -> Long.compare((long) a.get("startTime"), (long) b.get("startTime"))).limit(3).toList();

            if (upcoming.isEmpty()) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("📅 No Upcoming Contests")
                        .setDescription("No upcoming LeetCode contests found.").setColor(0xFFA116).build()).queue();
                return;
            }

            EmbedBuilder eb = new EmbedBuilder().setTitle("📅 Upcoming LeetCode Contests").setColor(0xFFA116);
            for (Map<String, Object> c : upcoming) {
                LocalDateTime dt = LocalDateTime.ofEpochSecond((long) c.get("startTime"), 0, ZoneOffset.ofHoursMinutes(5, 30));
                double hours = (double) c.get("duration") / 3600.0;
                eb.addField((String) c.get("title"), "🕐 " + dt + "\n⏱️ Duration: " + hours + " hours\n🔗 [Contest Link](https://leetcode.com/contest/" + c.get("titleSlug") + "/)", false);
            }
            event.getHook().sendMessageEmbeds(eb.build()).queue();
        });
    }

    private void handleLeaderboardSolved(SlashCommandInteractionEvent event) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> rows = submissionRepository.findWeeklyLeaderboard(weekAgo, PageRequest.of(0, 10));

        EmbedBuilder eb = new EmbedBuilder().setTitle("💻 LC Leaderboard — Weekly Solves").setColor(0x9B59B6);
        if (rows.isEmpty()) {
            eb.setDescription("No solves logged this week.");
        } else {
            StringBuilder sb = new StringBuilder();
            String[] medals = {"🥇", "🥈", "🥉"};
            for (int i = 0; i < rows.size(); i++) {
                Object[] row = rows.get(i);
                String medal = i < 3 ? medals[i] : "**#" + (i + 1) + "**";
                sb.append(medal).append(" ").append(row[1]).append(" — **").append(row[2]).append("** solves\n");
            }
            eb.setDescription(sb.toString());
        }
        event.replyEmbeds(eb.build()).queue();
    }

    private void handleLeaderboardRating(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        CompletableFuture.runAsync(() -> {
            List<User> users = userRepository.findAll().stream().filter(u -> u.getLeetcodeHandle() != null).toList();

            if (users.isEmpty()) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("💻 LC Leaderboard — Contest Rating")
                        .setDescription("No users have linked their LeetCode accounts yet.").setColor(0x9B59B6).build()).queue();
                return;
            }

            List<Object[]> ratings = new java.util.ArrayList<>();
            for (User u : users) {
                Map<String, Object> p = apiService.fetchUserProfile(u.getLeetcodeHandle());
                if (p != null && p.get("contestRating") != null) {
                    ratings.add(new Object[]{u.getUsername(), p.get("contestRating")});
                }
            }

            ratings.sort((a, b) -> Double.compare((Double) b[1], (Double) a[1]));
            List<Object[]> top10 = ratings.stream().limit(10).toList();

            EmbedBuilder eb = new EmbedBuilder().setTitle("💻 LC Leaderboard — Contest Rating").setColor(0x9B59B6);
            if (top10.isEmpty()) {
                eb.setDescription("No users with contest ratings found.");
            } else {
                StringBuilder sb = new StringBuilder();
                String[] medals = {"🥇", "🥈", "🥉"};
                for (int i = 0; i < top10.size(); i++) {
                    Object[] r = top10.get(i);
                    String medal = i < 3 ? medals[i] : "**#" + (i + 1) + "**";
                    sb.append(medal).append(" ").append(r[0]).append(" — **").append(r[1]).append("** rating\n");
                }
                eb.setDescription(sb.toString());
            }
            event.getHook().sendMessageEmbeds(eb.build()).queue();
        });
    }

    private void handleHelp(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📘 LeetCode Commands Help");
        embed.setColor(Color.BLUE);
        embed.setDescription("Here are all the `/lc` commands you can use:");
        
        embed.addField("`/lc link <username>`", "Link your Discord account to your LeetCode profile.", false);
        embed.addField("`/lc stats`", "View your LeetCode statistics and global ranking.", false);
        embed.addField("`/lc daily`", "Get the link to today's LeetCode Daily Challenge.", false);
        embed.addField("`/lc solved <slug>`", "Log a problem you solved today to earn XP and maintain your streak.", false);
        embed.addField("`/lc streak`", "View your current LeetCode solving streak.", false);
        embed.addField("`/lc solution <slug>`", "Get direct links to solutions and explanations for a problem.", false);
        embed.addField("`/lc contest`", "View details for the upcoming LeetCode contests.", false);
        embed.addField("`/lc leaderboard solved`", "View the top 10 users with the most problems solved this week.", false);
        embed.addField("`/lc leaderboard rating`", "View the top 10 users by LeetCode contest rating.", false);

        event.replyEmbeds(embed.build()).queue();
    }
}
