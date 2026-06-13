package com.placementcompanion.bot.scheduler;

import com.placementcompanion.bot.entity.InterviewQueue;
import com.placementcompanion.bot.repository.InterviewQueueRepository;
import net.dv8tion.jda.api.JDA;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InterviewScheduler {

    private final InterviewQueueRepository repository;
    private final JDA jda;

    public InterviewScheduler(InterviewQueueRepository repository, JDA jda) {
        this.repository = repository;
        this.jda = jda;
    }

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void matchUsers() {
        List<InterviewQueue> queue = repository.findAllByOrderByJoinedAtAsc();

        // Need at least 2 users to form a match
        while (queue.size() >= 2) {
            InterviewQueue user1 = queue.remove(0);
            InterviewQueue user2 = queue.remove(0);

            repository.deleteById(user1.getDiscordId());
            repository.deleteById(user2.getDiscordId());

            notifyMatch(user1.getDiscordId(), user2.getDiscordId());
        }
    }

    private void notifyMatch(String user1Id, String user2Id) {
        jda.retrieveUserById(user1Id).queue(u1 -> {
            jda.retrieveUserById(user2Id).queue(u2 -> {
                String messageTo1 = "🎉 **Mock Interview Match!** You have been matched with " + u2.getAsMention() + " (`" + u2.getName() + "`). Reach out to them to start your interview practice!";
                String messageTo2 = "🎉 **Mock Interview Match!** You have been matched with " + u1.getAsMention() + " (`" + u1.getName() + "`). Reach out to them to start your interview practice!";

                u1.openPrivateChannel().queue(pc -> pc.sendMessage(messageTo1).queue());
                u2.openPrivateChannel().queue(pc -> pc.sendMessage(messageTo2).queue());
            }, error -> {
                // If user2 couldn't be retrieved, put user1 back in queue
                System.err.println("Could not retrieve user2 " + user2Id);
            });
        }, error -> {
            System.err.println("Could not retrieve user1 " + user1Id);
        });
    }
}
