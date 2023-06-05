package com.libertaua.bot.entities;

import com.libertaua.bot.enums.Phase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Scope;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Entity class representing bot_states table
 * It has tons of those nasty annotations that I dont even understand
 *
 * @author seaeagle
 */
@Entity(name="BotState")
@Table(name="bot_states")
@Scope(value = "prototype") //Idk singleton or prototype what to choose
@NoArgsConstructor
@ToString
public class BotState {

    @SequenceGenerator(
            name = "states_sequence",
            sequenceName = "states_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "states_sequence"
    )
    @Column(
            name="id",
            updatable = false,
            nullable = false
    )
    @Id
    @Getter @Setter
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name="phase", nullable = false)
    @Getter @Setter
    private Phase phase;

    @OneToOne
    @JoinColumn(name = "question_number", referencedColumnName = "id")
    @Getter @Setter
    private Question currentQuestion;

    @Column(
            name="last_answer",
            nullable = false,
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String lastAnswer;

    @Column(name="last_meme")
    @Getter @Setter
    private Long lastMeme;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @Getter @Setter
    private TelegramUser user;

    public BotState(String lastAnswer, TelegramUser user, Question firstQuestion, long lastMeme) {
        this.lastAnswer = lastAnswer;
        this.user = user;
        this.phase = Phase.PRESTART;
        this.currentQuestion = firstQuestion;
        this.lastMeme = lastMeme;
    }
}
