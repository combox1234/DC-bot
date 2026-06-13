package com.placementcompanion.bot.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.awt.Color;

@Component
public class HelpListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("help")) return;

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🤖 Placement Companion Bot - Help");
        embed.setColor(Color.decode("#2ECC71"));
        embed.setDescription("Here is a list of all available commands to help you prepare for placements!");

        // Quiz & Study
        embed.addField("🧠 Quiz & Study", """
                `/quiz start <category>` — Start a quiz (DSA, Aptitude, Python, AI/ML, Verbal)
                `/quiz stats` — View your quiz accuracy per category
                `/daily` — View today's placement daily challenge
                `/company <name>` — View recruitment guides for top companies""", false);

        // User Stats & Progress
        embed.addField("📊 Stats & Progress", """
                `/stats [@user]` — View your or someone else's stats
                `/leaderboard <period>` — View the top users by XP
                `/streak` — View your current daily streak""", false);

        // LeetCode
        embed.addField("💻 LeetCode", """
                `/lc link <username>` — Link your LeetCode account
                `/lc stats` — View your LeetCode stats
                `/lc daily` — View today's LeetCode daily challenge
                `/lc help` — View all LeetCode commands""", false);

        // GitHub
        embed.addField("🐙 GitHub", """
                `/github link <username>` — Link your GitHub account
                `/github stats` — View your GitHub stats
                `/github activity` — View your recent activity""", false);

        // Utilities
        embed.addField("🛠️ Utilities", """
                `/notes add/list/view/delete` — Manage personal quick notes
                `/remind set <time>` — Set a daily study reminder (HH:mm IST)
                `/remind off` — Disable your reminder
                `/remind status` — Check your reminder""", false);

        // Social
        embed.addField("🎤 Mock Interviews", """
                `/interview join` — Join the mock interview matchmaking queue
                `/interview leave` — Leave the matchmaking queue""", false);

        // Admin
        embed.addField("⚙️ Admin (Manage Server)", """
                `/setup daily <channel>` — Set daily broadcast channel
                `/admin add-question` — Add a quiz question
                `/admin remove-question` — Remove a quiz question
                `/admin reset-user` — Reset a user's progress
                `/admin stats` — View server engagement stats""", false);

        embed.setFooter("Keep grinding and good luck with your placements! 🚀");
        event.replyEmbeds(embed.build()).queue();
    }
}
