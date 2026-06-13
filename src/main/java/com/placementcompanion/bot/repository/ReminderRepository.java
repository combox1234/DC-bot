package com.placementcompanion.bot.repository;

import com.placementcompanion.bot.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, String> {
    List<Reminder> findByEnabledTrue();
    List<Reminder> findByReminderTimeAndEnabledTrue(String reminderTime);
}
