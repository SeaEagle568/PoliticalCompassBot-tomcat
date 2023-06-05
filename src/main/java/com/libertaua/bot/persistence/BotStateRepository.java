package com.libertaua.bot.persistence;

import com.libertaua.bot.entities.BotState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Personal repository for BotState entity
 */
@Repository
public interface BotStateRepository extends JpaRepository<BotState, Long> {
}
