package com.placementcompanion.bot.listener;

import com.placementcompanion.bot.entity.UserNote;
import com.placementcompanion.bot.service.NoteService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class NotesListener extends ListenerAdapter {

    private final NoteService noteService;

    public NotesListener(NoteService noteService) {
        this.noteService = noteService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("notes")) return;

        String discordId = event.getUser().getId();
        String username = event.getUser().getName();
        String subcommand = event.getSubcommandName();

        if (subcommand == null) return;

        event.deferReply(true).queue(); // Private reply since notes are personal

        switch (subcommand) {
            case "add":
                String title = event.getOption("title").getAsString();
                String content = event.getOption("content").getAsString();
                
                try {
                    noteService.addNote(discordId, username, title, content);
                    EmbedBuilder eb = new EmbedBuilder()
                            .setTitle("✅ Note Saved")
                            .setDescription("Your note **" + title + "** has been securely saved.")
                            .setColor(Color.GREEN);
                    event.getHook().sendMessageEmbeds(eb.build()).queue();
                } catch (IllegalArgumentException e) {
                    event.getHook().sendMessage("❌ " + e.getMessage()).queue();
                }
                break;

            case "list":
                List<UserNote> notes = noteService.getNotesForUser(discordId);
                if (notes.isEmpty()) {
                    event.getHook().sendMessage("You don't have any saved notes yet. Use `/notes add` to create one!").queue();
                    return;
                }

                EmbedBuilder listEb = new EmbedBuilder()
                        .setTitle("📝 Your Saved Notes")
                        .setColor(Color.decode("#9B59B6"));

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < notes.size(); i++) {
                    sb.append(i + 1).append(". **").append(notes.get(i).getTitle()).append("**\n");
                }
                listEb.setDescription(sb.toString());
                event.getHook().sendMessageEmbeds(listEb.build()).queue();
                break;

            case "view":
                String viewTitle = event.getOption("title").getAsString();
                Optional<UserNote> noteOpt = noteService.getNoteByTitle(discordId, viewTitle);

                if (noteOpt.isEmpty()) {
                    event.getHook().sendMessage("❌ Note not found: **" + viewTitle + "**").queue();
                } else {
                    UserNote note = noteOpt.get();
                    EmbedBuilder viewEb = new EmbedBuilder()
                            .setTitle("📌 " + note.getTitle())
                            .setDescription("```\n" + note.getContent() + "\n```")
                            .setColor(Color.decode("#3498DB"))
                            .setFooter("Created at: " + note.getCreatedAt().toString().substring(0, 10));
                    event.getHook().sendMessageEmbeds(viewEb.build()).queue();
                }
                break;

            case "delete":
                String deleteTitle = event.getOption("title").getAsString();
                boolean deleted = noteService.deleteNoteByTitle(discordId, deleteTitle);

                if (deleted) {
                    event.getHook().sendMessage("✅ Successfully deleted note: **" + deleteTitle + "**").queue();
                } else {
                    event.getHook().sendMessage("❌ Note not found: **" + deleteTitle + "**").queue();
                }
                break;
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("notes") && 
            ("view".equals(event.getSubcommandName()) || "delete".equals(event.getSubcommandName())) && 
            event.getFocusedOption().getName().equals("title")) {
            
            String partial = event.getFocusedOption().getValue().toLowerCase();
            String discordId = event.getUser().getId();
            
            List<Command.Choice> options = noteService.getNotesForUser(discordId).stream()
                    .map(UserNote::getTitle)
                    .filter(t -> t.toLowerCase().startsWith(partial))
                    .map(t -> new Command.Choice(t, t))
                    .limit(25)
                    .collect(Collectors.toList());

            event.replyChoices(options).queue();
        }
    }
}
