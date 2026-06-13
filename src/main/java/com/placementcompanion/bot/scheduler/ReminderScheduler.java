package com.placementcompanion.bot.scheduler;

import com.placementcompanion.bot.entity.ChallengeCompletion;
import com.placementcompanion.bot.entity.QuizHistory;
import com.placementcompanion.bot.entity.Reminder;
import com.placementcompanion.bot.repository.ChallengeCompletionRepository;
import com.placementcompanion.bot.repository.QuizHistoryRepository;
import com.placementcompanion.bot.repository.ReminderRepository;
import net.dv8tion.jda.api.JDA;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReminderScheduler {

    private final ReminderRepository reminderRepository;
    private final QuizHistoryRepository quizHistoryRepository;
    private final ChallengeCompletionRepository challengeCompletionRepository;
    private final JDA jda;

    public ReminderScheduler(ReminderRepository reminderRepository,
                             QuizHistoryRepository quizHistoryRepository,
                             ChallengeCompletionRepository challengeCompletionRepository,
                             JDA jda) {
        this.reminderRepository = reminderRepository;
        this.quizHistoryRepository = quizHistoryRepository;
        this.challengeCompletionRepository = challengeCompletionRepository;
        this.jda = jda;
    }

    @Scheduled(cron = "0 * * * * *") // Every minute
    public void checkReminders() {
        String currentTime = LocalTime.now(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        List<Reminder> reminders = reminderRepository.findByReminderTimeAndEnabledTrue(currentTime);

        for (Reminder reminder : reminders) {
            String discordId = reminder.getDiscordId();

            // Check if user has done a quiz today
            List<QuizHistory> todayQuizzes = quizHistoryRepository.findByDiscordId(discordId);
            boolean doneQuizToday = todayQuizzes.stream()
                    .anyMatch(q -> q.getAnsweredAt() != null &&
                            q.getAnsweredAt().toLocalDate().equals(LocalDate.now()));

            // Check if user has done a daily challenge today
            boolean doneChallengeToday = challengeCompletionRepository
                    .findByDiscordIdAndCompletedDate(discordId, LocalDate.now())
                    .isPresent();

            if (!doneQuizToday || !doneChallengeToday) {
                sendReminderDM(discordId, doneQuizToday, doneChallengeToday);
            }
        }
    }

    private void sendReminderDM(String userId, boolean doneQuiz, boolean doneChallenge) {
        jda.retrieveUserById(userId).queue(user -> {
            StringBuilder msg = new StringBuilder("⏰ **Placement Companion Reminder!**\n\nYou still have things to do today:\n");
            if (!doneQuiz) {
                msg.append("• 🧠 Take a **Quiz** (`/quiz`)\n");
            }
            if (!doneChallenge) {
                msg.append("• 📅 Complete the **Daily Challenge** (`/daily`)\n");
            }
            msg.append("\nKeep your streak alive! 🔥");

            user.openPrivateChannel().queue(
                    pc -> pc.sendMessage(msg.toString()).queue(),
                    error -> System.err.println("Could not DM user " + userId + ": " + error.getMessage())
            );
        }, error -> System.err.println("Could not retrieve user " + userId));
    }
}
