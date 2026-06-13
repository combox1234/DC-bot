package com.placementcompanion.bot.listener;

import com.placementcompanion.bot.service.CompanyGuideService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CompanyListener extends ListenerAdapter {

    private final CompanyGuideService companyGuideService;

    public CompanyListener(CompanyGuideService companyGuideService) {
        this.companyGuideService = companyGuideService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("company")) return;

        String companyName = event.getOption("name").getAsString();
        CompanyGuideService.CompanyGuide guide = companyGuideService.getGuide(companyName);

        if (guide == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("❌ Company Not Found")
                    .setDescription("Could not find a guide for **" + companyName + "**.\nAvailable guides: " + String.join(", ", companyGuideService.getAvailableCompanies()))
                    .setColor(Color.RED)
                    .build()).queue();
            return;
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("🏢 " + guide.getName() + " Recruitment Guide")
                .setDescription(guide.getProcess())
                .setColor(Color.decode("#3498DB"));

        if (guide.getRounds() != null && !guide.getRounds().isEmpty()) {
            StringBuilder roundsText = new StringBuilder();
            for (String round : guide.getRounds()) {
                roundsText.append("• ").append(round).append("\n");
            }
            eb.addField("🔄 Interview Rounds", roundsText.toString(), false);
        }

        if (guide.getFaqs() != null && !guide.getFaqs().isEmpty()) {
            StringBuilder faqsText = new StringBuilder();
            for (Map<String, String> faq : guide.getFaqs()) {
                faqsText.append("**Q: ").append(faq.get("q")).append("**\n")
                        .append("A: ").append(faq.get("a")).append("\n\n");
            }
            eb.addField("❓ FAQs", faqsText.toString().trim(), false);
        }

        if (guide.getTips() != null && !guide.getTips().isEmpty()) {
            StringBuilder tipsText = new StringBuilder();
            for (String tip : guide.getTips()) {
                tipsText.append("💡 ").append(tip).append("\n");
            }
            eb.addField("✨ Top Tips", tipsText.toString(), false);
        }

        event.replyEmbeds(eb.build()).queue();
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("company") && event.getFocusedOption().getName().equals("name")) {
            String partial = event.getFocusedOption().getValue().toLowerCase();
            
            List<Command.Choice> options = companyGuideService.getAvailableCompanies().stream()
                    .filter(c -> c.startsWith(partial))
                    .map(c -> {
                        String displayName = c.substring(0, 1).toUpperCase() + c.substring(1);
                        return new Command.Choice(displayName, c);
                    })
                    .limit(25)
                    .collect(Collectors.toList());

            event.replyChoices(options).queue();
        }
    }
}
