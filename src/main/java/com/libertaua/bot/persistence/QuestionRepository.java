package com.libertaua.bot.persistence;

import com.libertaua.bot.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Personal repository for Question entity
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
}
