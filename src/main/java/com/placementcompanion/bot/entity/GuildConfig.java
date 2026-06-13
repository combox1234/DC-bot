package com.placementcompanion.bot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "guild_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuildConfig {

    @Id
    private String guildId;

    private String dailyBroadcastChannelId;
}
