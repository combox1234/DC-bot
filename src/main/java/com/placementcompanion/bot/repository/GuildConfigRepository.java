package com.placementcompanion.bot.repository;

import com.placementcompanion.bot.entity.GuildConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildConfigRepository extends JpaRepository<GuildConfig, String> {
}
