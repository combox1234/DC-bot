package com.placementcompanion.bot.service;

import com.placementcompanion.bot.entity.User;
import com.placementcompanion.bot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class StreakService {

    private final UserRepository userRepository;
    private final XpService xpService;

    public StreakService(UserRepository userRepository, XpService xpService) {
        this.userRepository = userRepository;
        this.xpService = xpService;
    }

    @Transactional
    public String updateStreak(String discordId, String username) {
        User user = xpService.getOrCreateUser(discordId, username);
        
        LocalDateTime lastActivity = user.getLastActivity();
        LocalDate today = LocalDate.now();

        if (lastActivity == null) {
            user.setCurrentStreak(1);
            user.setBestStreak(1);
            user.setLastActivity(LocalDateTime.now());
            userRepository.save(user);
            return "🔥 Streak started! Day 1!";
        }

        LocalDate lastDate = lastActivity.toLocalDate();
        
        if (lastDate.equals(today)) {
            return "🔥 You already contributed to your streak today!";
        } else if (lastDate.equals(today.minusDays(1))) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
            if (user.getCurrentStreak() > user.getBestStreak()) {
                user.setBestStreak(user.getCurrentStreak());
            }
            user.setLastActivity(LocalDateTime.now());
            userRepository.save(user);
            return "🔥 Streak increased to " + user.getCurrentStreak() + "!";
        } else {
            user.setCurrentStreak(1);
            user.setLastActivity(LocalDateTime.now());
            userRepository.save(user);
            return "💔 Streak lost! Starting over at Day 1.";
        }
    }
}
