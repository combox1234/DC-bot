package com.placementcompanion.bot.service;

import com.placementcompanion.bot.entity.User;
import com.placementcompanion.bot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class XpService {

    private final UserRepository userRepository;

    public XpService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getOrCreateUser(String discordId, String username) {
        Optional<User> userOpt = userRepository.findById(discordId);
        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
            User newUser = new User(discordId, username);
            return userRepository.save(newUser);
        }
    }

    @Transactional
    public boolean addXp(String discordId, String username, int amount) {
        User user = getOrCreateUser(discordId, username);
        user.setXp(user.getXp() + amount);
        
        int oldLevel = user.getLevel();
        int newLevel = calculateLevel(user.getXp());
        
        if (newLevel > oldLevel) {
            user.setLevel(newLevel);
            userRepository.save(user);
            return true; // Leveled up
        }
        userRepository.save(user);
        return false;
    }

    private int calculateLevel(int xp) {
        return Math.max(1, (int) (Math.sqrt(xp) / 5) + 1);
    }
}
