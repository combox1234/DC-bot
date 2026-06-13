package com.placementcompanion.bot.listener;

import com.placementcompanion.bot.entity.User;
import com.placementcompanion.bot.repository.UserRepository;
import com.placementcompanion.bot.service.GitHubApiService;
import com.placementcompanion.bot.service.UserService;
import com.placementcompanion.bot.service.XpService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class GitHubListener extends ListenerAdapter {

    private final GitHubApiService apiService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final XpService xpService;

    public GitHubListener(GitHubApiService apiService, UserRepository userRepository, UserService userService, XpService xpService) {
        this.apiService = apiService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.xpService = xpService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("github")) return;

        String sub = event.getSubcommandName();
        if (sub == null) return;

        switch (sub) {
            case "link": handleLink(event); break;
            case "stats": handleStats(event); break;
            case "activity": handleActivity(event); break;
            case "commits": handleCommits(event); break;
            case "structure": handleStructure(event); break;
            case "file": handleFile(event); break;
            default: event.reply("Unknown subcommand.").setEphemeral(true).queue();
        }
    }

    private void handleLink(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        CompletableFuture.runAsync(() -> {
            try {
                var userOpt = event.getOption("username");
                if (userOpt == null) {
                    event.getHook().sendMessage("❌ Please provide a username.").queue();
                    return;
                }
                String username = userOpt.getAsString();
                Map<String, Object> profile = apiService.fetchProfile(username);

                if (profile == null) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("❌ Username Not Found")
                            .setDescription("GitHub username **" + username + "** was not found.")
                            .setColor(Color.RED).build()).queue();
                    return;
                }

                String discordId = event.getUser().getId();
                User u = userService.ensureUser(discordId, event.getUser().getName());
                
                boolean isFirstLink = (u.getGithubHandle() == null);
                u.setGithubHandle(username);
                userRepository.save(u);

                EmbedBuilder eb = new EmbedBuilder().setTitle("✅ GitHub Linked!")
                        .setDescription("Your GitHub handle **" + username + "** has been linked successfully.")
                        .setThumbnail((String) profile.get("avatar_url"))
                        .addField("Public Repos", String.valueOf(profile.get("public_repos")), true)
                        .addField("Followers", String.valueOf(profile.get("followers")), true)
                        .setColor(Color.decode("#2ecc71"));

                if (isFirstLink) {
                    xpService.addXp(discordId, event.getUser().getName(), 50);
                    eb.addField("🎉 Reward", "+50 XP for linking GitHub!", false);
                }

                event.getHook().sendMessageEmbeds(eb.build()).queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessage("❌ Error linking GitHub: " + e.getMessage()).queue();
            }
        });
    }

    private void handleStats(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        CompletableFuture.runAsync(() -> {
            try {
                String discordId = event.getUser().getId();
                Optional<User> u = userRepository.findById(discordId);

                if (u.isEmpty() || u.get().getGithubHandle() == null) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("❌ No GitHub Handle")
                            .setDescription("Link your GitHub account first with `/github link [username]`")
                            .setColor(Color.RED).build()).queue();
                    return;
                }

                String handle = u.get().getGithubHandle();
                Map<String, Object> profile = apiService.fetchProfile(handle);

                if (profile == null) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("⚠️ API Error")
                            .setDescription("Could not fetch stats from GitHub API.")
                            .setColor(Color.RED).build()).queue();
                    return;
                }

                String name = profile.get("name") != null ? (String) profile.get("name") : handle;
                
                EmbedBuilder eb = new EmbedBuilder().setTitle("📊 GitHub Stats — " + name)
                        .setThumbnail((String) profile.get("avatar_url"))
                        .setColor(Color.decode("#333333"))
                        .addField("Public Repos", String.valueOf(profile.get("public_repos")), true)
                        .addField("Followers", String.valueOf(profile.get("followers")), true)
                        .addField("Following", String.valueOf(profile.get("following")), true)
                        .addField("Profile", "[View on GitHub](https://github.com/" + handle + ")", false);

                event.getHook().sendMessageEmbeds(eb.build()).queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessage("❌ Error fetching stats: " + e.getMessage()).queue();
            }
        });
    }

    private void handleActivity(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        CompletableFuture.runAsync(() -> {
            try {
                String discordId = event.getUser().getId();
                Optional<User> u = userRepository.findById(discordId);

                if (u.isEmpty() || u.get().getGithubHandle() == null) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("❌ No GitHub Handle")
                            .setDescription("Link your GitHub account first with `/github link [username]`")
                            .setColor(Color.RED).build()).queue();
                    return;
                }

                String handle = u.get().getGithubHandle();
                List<Map<String, Object>> events = apiService.fetchRecentActivity(handle);

                if (events == null || events.isEmpty()) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("📈 Recent Activity — " + handle)
                            .setDescription("No recent public activity found.")
                            .setColor(Color.decode("#333333")).build()).queue();
                    return;
                }

                EmbedBuilder eb = new EmbedBuilder().setTitle("📈 Recent Activity — " + handle)
                        .setColor(Color.decode("#333333"));

                StringBuilder sb = new StringBuilder();
                for (Map<String, Object> ev : events) {
                    String type = (String) ev.get("type");
                    String repoName = "Unknown";
                    if (ev.get("repo") instanceof Map) {
                        repoName = (String) ((Map<?, ?>) ev.get("repo")).get("name");
                    }
                    
                    String action = switch (type) {
                        case "PushEvent" -> "Push to";
                        case "PullRequestEvent" -> "Pull Request in";
                        case "IssuesEvent" -> "Issue in";
                        case "CreateEvent" -> "Created";
                        case "DeleteEvent" -> "Deleted in";
                        case "WatchEvent" -> "Starred";
                        case "ForkEvent" -> "Forked";
                        default -> "Activity in";
                    };

                    sb.append("• **").append(action).append("** `").append(repoName).append("`\n");
                }

                eb.setDescription(sb.toString());
                event.getHook().sendMessageEmbeds(eb.build()).queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessage("❌ Error fetching activity: " + e.getMessage()).queue();
            }
        });
    }

    private void handleCommits(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        CompletableFuture.runAsync(() -> {
            try {
                String owner = event.getOption("owner").getAsString();
                String repo = event.getOption("repo").getAsString();
                
                List<Map<String, Object>> commits = apiService.fetchCommits(owner, repo);
                if (commits == null || commits.isEmpty()) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("❌ Commits Not Found")
                            .setDescription("Could not fetch commits for **" + owner + "/" + repo + "**.")
                            .setColor(Color.RED).build()).queue();
                    return;
                }

                EmbedBuilder eb = new EmbedBuilder().setTitle("📌 Recent Commits — " + owner + "/" + repo)
                        .setColor(Color.decode("#333333"));
                
                StringBuilder sb = new StringBuilder();
                for (Map<String, Object> commitObj : commits) {
                    Map<String, Object> commitData = (Map<String, Object>) commitObj.get("commit");
                    String message = (String) commitData.get("message");
                    if (message.length() > 50) message = message.substring(0, 47) + "...";
                    String sha = ((String) commitObj.get("sha")).substring(0, 7);
                    String url = (String) commitObj.get("html_url");
                    sb.append("• [`").append(sha).append("`](").append(url).append(") ").append(message).append("\n");
                }
                
                eb.setDescription(sb.toString());
                event.getHook().sendMessageEmbeds(eb.build()).queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessage("❌ Error fetching commits: " + e.getMessage()).queue();
            }
        });
    }

    private void handleStructure(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        CompletableFuture.runAsync(() -> {
            try {
                String owner = event.getOption("owner").getAsString();
                String repo = event.getOption("repo").getAsString();
                var pathOpt = event.getOption("path");
                String path = pathOpt != null ? pathOpt.getAsString() : "";

                List<Map<String, Object>> contents = apiService.fetchRepoContents(owner, repo, path);
                if (contents == null || contents.isEmpty()) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("❌ Structure Not Found")
                            .setDescription("Could not fetch structure for **" + owner + "/" + repo + "** at path `" + (path.isEmpty() ? "/" : path) + "`.")
                            .setColor(Color.RED).build()).queue();
                    return;
                }

                String displayPath = path.isEmpty() ? "/" : "/" + path;
                EmbedBuilder eb = new EmbedBuilder().setTitle("📂 Repository Structure — " + owner + "/" + repo)
                        .setDescription("Path: `" + displayPath + "`\n\n")
                        .setColor(Color.decode("#333333"));

                StringBuilder sb = new StringBuilder(eb.getDescriptionBuilder().toString());
                for (Map<String, Object> item : contents) {
                    String name = (String) item.get("name");
                    String type = (String) item.get("type");
                    String icon = type.equals("dir") ? "📁" : "📄";
                    sb.append(icon).append(" `").append(name).append("`\n");
                }
                
                if (sb.length() > 4096) {
                    sb.setLength(4093);
                    sb.append("...");
                }
                eb.setDescription(sb.toString());
                event.getHook().sendMessageEmbeds(eb.build()).queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessage("❌ Error fetching structure: " + e.getMessage()).queue();
            }
        });
    }

    private void handleFile(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        CompletableFuture.runAsync(() -> {
            try {
                String owner = event.getOption("owner").getAsString();
                String repo = event.getOption("repo").getAsString();
                String path = event.getOption("path").getAsString();

                Map<String, Object> fileData = apiService.fetchFileContent(owner, repo, path);
                if (fileData == null || !fileData.containsKey("content")) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder().setTitle("❌ File Not Found")
                            .setDescription("Could not fetch file **" + path + "** in **" + owner + "/" + repo + "**.")
                            .setColor(Color.RED).build()).queue();
                    return;
                }

                String base64Content = (String) fileData.get("content");
                // GitHub sends base64 encoded with newlines, so we must remove or use MimeDecoder
                byte[] decodedBytes = java.util.Base64.getMimeDecoder().decode(base64Content);
                String content = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);

                String htmlUrl = (String) fileData.get("html_url");
                EmbedBuilder eb = new EmbedBuilder().setTitle("📄 " + path)
                        .setAuthor(owner + "/" + repo, htmlUrl, null)
                        .setColor(Color.decode("#333333"));

                // determine extension for code block formatting
                String ext = "";
                int dotIndex = path.lastIndexOf('.');
                if (dotIndex > 0) ext = path.substring(dotIndex + 1);

                String codeBlock = "```" + ext + "\n" + content + "\n```";
                if (codeBlock.length() > 4096) {
                    // Truncate
                    content = content.substring(0, 4000 - ext.length());
                    codeBlock = "```" + ext + "\n" + content + "\n\n... (truncated due to Discord limits)\n```";
                    eb.setDescription(codeBlock);
                    eb.addField("Full File", "[View on GitHub](" + htmlUrl + ")", false);
                } else {
                    eb.setDescription(codeBlock);
                }

                event.getHook().sendMessageEmbeds(eb.build()).queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessage("❌ Error fetching file: " + e.getMessage()).queue();
            }
        });
    }
}
