package com.placementcompanion.bot.service;

import com.placementcompanion.bot.entity.Reminder;
import com.placementcompanion.bot.repository.ReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ReminderService {

    private final ReminderRepository repository;

    public ReminderService(ReminderRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void setReminder(String discordId, String time) {
        Optional<Reminder> existing = repository.findById(discordId);
        if (existing.isPresent()) {
            Reminder r = existing.get();
            r.setReminderTime(time);
            r.setEnabled(true);
            repository.save(r);
        } else {
            repository.save(new Reminder(discordId, time, true));
        }
    }

    @Transactional
    public boolean disableReminder(String discordId) {
        Optional<Reminder> existing = repository.findById(discordId);
        if (existing.isPresent()) {
            Reminder r = existing.get();
            r.setEnabled(false);
            repository.save(r);
            return true;
        }
        return false;
    }

    public Optional<Reminder> getReminder(String discordId) {
        return repository.findById(discordId);
    }
}
