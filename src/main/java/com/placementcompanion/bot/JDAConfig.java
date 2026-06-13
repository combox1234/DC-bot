package com.placementcompanion.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;

@Configuration
public class JDAConfig {

    @Value("${discord.bot.token}")
    private String botToken;

    @Bean
    public JDA jda() throws InterruptedException {
        if (botToken == null || botToken.isEmpty()) {
            System.err.println("WARNING: DISCORD_TOKEN is not set in application.yml or environment variables!");
            return null;
        }

        JDA jda = JDABuilder.createDefault(botToken, EnumSet.allOf(GatewayIntent.class))
                .build();
        
        jda.awaitReady();
        System.out.println("Java Placement Companion Bot is Ready! Logged in as: " + jda.getSelfUser().getName());
        return jda;
    }
}
