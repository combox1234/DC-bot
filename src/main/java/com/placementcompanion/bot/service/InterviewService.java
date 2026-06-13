package com.placementcompanion.bot.service;

import com.placementcompanion.bot.entity.InterviewQueue;
import com.placementcompanion.bot.repository.InterviewQueueRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InterviewService {

    private final InterviewQueueRepository repository;

    public InterviewService(InterviewQueueRepository repository) {
        this.repository = repository;
    }

    public boolean joinQueue(String discordId) {
        if (repository.existsById(discordId)) {
            return false; // Already in queue
        }
        repository.save(new InterviewQueue(discordId, LocalDateTime.now()));
        return true;
    }

    public boolean leaveQueue(String discordId) {
        if (repository.existsById(discordId)) {
            repository.deleteById(discordId);
            return true;
        }
        return false;
    }

    public boolean isInQueue(String discordId) {
        return repository.existsById(discordId);
    }
}
