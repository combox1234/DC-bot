package com.placementcompanion.bot.listener;

import com.placementcompanion.bot.service.PistonApiService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;

@Component
public class CodeRunListener extends ListenerAdapter {

    private final PistonApiService pistonApiService;

    public CodeRunListener(PistonApiService pistonApiService) {
        this.pistonApiService = pistonApiService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("run")) return;

        // Defer reply immediately since API call might take a few seconds
        event.deferReply().queue();

        String language = event.getOption("language").getAsString();
        String code = event.getOption("code").getAsString();

        CompletableFuture.runAsync(() -> {
            try {
                PistonApiService.ExecutionOutput output = pistonApiService.executeCode(language, code);

                String result = output.getOutput();
                
                // Truncate if too long for Discord embed description (max 4096)
                if (result.length() > 4000) {
                    result = result.substring(0, 4000) + "\n... (output truncated)";
                }

                if (result.isEmpty()) {
                    result = "(No output)";
                }

                Color color = output.exitCode == 0 ? Color.decode("#2ECC71") : Color.decode("#E74C3C");
                String status = output.exitCode == 0 ? "Success" : "Error (Exit code: " + output.exitCode + ")";

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("💻 Code Execution (" + language + ")");
                embed.setColor(color);
                embed.addField("Status", status, false);
                embed.setDescription("```" + language + "\n" + result + "\n```");
                embed.setFooter("Powered by Piston API");

                event.getHook().sendMessageEmbeds(embed.build()).queue();

            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessage("❌ Failed to execute code: " + e.getMessage()).queue();
            }
        });
    }
}
