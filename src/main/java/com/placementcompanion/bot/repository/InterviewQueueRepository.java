package com.placementcompanion.bot.repository;

import com.placementcompanion.bot.entity.InterviewQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewQueueRepository extends JpaRepository<InterviewQueue, String> {
    List<InterviewQueue> findAllByOrderByJoinedAtAsc();
}
