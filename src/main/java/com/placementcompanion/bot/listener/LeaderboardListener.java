package com.placementcompanion.bot.listener;

import com.placementcompanion.bot.entity.User;
import com.placementcompanion.bot.repository.UserRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.List;

@Component
public class LeaderboardListener extends ListenerAdapter {

    private final UserRepository userRepository;

    public LeaderboardListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("leaderboard")) {
            List<User> topUsers = userRepository.findAll(Sort.by(Sort.Direction.DESC, "xp"));
            
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("🏆 Global Leaderboard");
            embed.setColor(Color.decode("#F1C40F"));

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(topUsers.size(), 10); i++) {
                User u = topUsers.get(i);
                String medal = (i == 0) ? "🥇" : (i == 1) ? "🥈" : (i == 2) ? "🥉" : "🔹";
                sb.append(medal).append(" **").append(u.getUsername()).append("** - Lvl ")
                  .append(u.getLevel()).append(" (").append(u.getXp()).append(" XP)\n");
            }

            if (sb.length() == 0) sb.append("No users found.");
            
            embed.setDescription(sb.toString());
            event.replyEmbeds(embed.build()).queue();
        } else if (event.getName().equals("streak")) {
            User u = userRepository.findById(event.getUser().getId()).orElse(null);
            if (u == null) {
                event.reply("You haven't started a streak yet!").setEphemeral(true).queue();
                return;
            }
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("🔥 Streak Status: " + event.getUser().getName());
            embed.setColor(Color.decode("#E67E22"));
            embed.addField("Current Streak", u.getCurrentStreak() + " days", true);
            embed.addField("Best Streak", u.getBestStreak() + " days", true);
            event.replyEmbeds(embed.build()).queue();
        }
    }
}
