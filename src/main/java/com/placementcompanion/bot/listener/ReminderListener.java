package com.placementcompanion.bot.listener;

import com.placementcompanion.bot.entity.Reminder;
import com.placementcompanion.bot.service.ReminderService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ReminderListener extends ListenerAdapter {

    private final ReminderService reminderService;

    public ReminderListener(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("remind")) return;

        event.deferReply(true).queue();

        String subcommand = event.getSubcommandName();

        if ("set".equals(subcommand)) {
            handleSet(event);
        } else if ("off".equals(subcommand)) {
            handleOff(event);
        } else if ("status".equals(subcommand)) {
            handleStatus(event);
        }
    }

    private void handleSet(SlashCommandInteractionEvent event) {
        String timeStr = event.getOption("time").getAsString().trim();

        // Validate HH:mm format
        if (!timeStr.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            event.getHook().sendMessage("❌ Invalid time format! Please use **HH:mm** (24-hour format), e.g. `09:00` or `21:30`.").queue();
            return;
        }

        String userId = event.getUser().getId();
        reminderService.setReminder(userId, timeStr);
        event.getHook().sendMessage("✅ Reminder set! I will DM you every day at **" + timeStr + " IST** if you haven't completed your quiz or daily challenge.").queue();
    }

    private void handleOff(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        if (reminderService.disableReminder(userId)) {
            event.getHook().sendMessage("🔕 Your daily reminder has been turned off.").queue();
        } else {
            event.getHook().sendMessage("⚠️ You don't have an active reminder to disable.").queue();
        }
    }

    private void handleStatus(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        Optional<Reminder> reminderOpt = reminderService.getReminder(userId);
        if (reminderOpt.isPresent() && reminderOpt.get().isEnabled()) {
            event.getHook().sendMessage("🔔 Your reminder is **active** at **" + reminderOpt.get().getReminderTime() + " IST** daily.").queue();
        } else {
            event.getHook().sendMessage("🔕 You don't have an active reminder. Use `/remind set` to create one.").queue();
        }
    }
}
