package com.placementcompanion.bot.service;

import com.placementcompanion.bot.entity.User;
import com.placementcompanion.bot.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public synchronized User ensureUser(String discordId, String username) {
        Optional<User> u = userRepository.findById(discordId);
        if (u.isPresent()) {
            return u.get();
        }
        
        User newUser = new User(discordId, username);
        try {
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            // Concurrent insert occurred
            return userRepository.findById(discordId).orElseThrow();
        }
    }
}
