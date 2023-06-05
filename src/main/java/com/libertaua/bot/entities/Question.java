package com.libertaua.bot.entities;

import com.libertaua.bot.enums.Axe;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Scope;

import javax.persistence.*;

/**
 * Entity class representing questions table
 * It has tons of those nasty annotations that I dont even understand
 *
 * @author seaeagle
 */
@Entity(name="Question")
@Table(
        name="questions",
        uniqueConstraints = { //making columns unique
                @UniqueConstraint(name = "question_text_unique", columnNames = "text"),
                @UniqueConstraint(name = "question_number_unique", columnNames = "number"),
                @UniqueConstraint(name = "question_id_unique", columnNames = "id")
        }
)
@Scope(value = "prototype")
@ToString
@NoArgsConstructor
public class Question {

    @Id
    @Column(
            name="id",
            nullable = false,
            unique = true
    )

    @Getter @Setter
    private Long id;

    @Column(
            name="text",
            nullable = false,
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String text;

    @Column(name="number", nullable = false)
    @Getter @Setter
    private Long number;

    @Enumerated(EnumType.STRING)
    @Column(name="axe", nullable = false)
    @Getter @Setter
    private Axe axe;

    @Column(name="inverted", nullable = false)
    @Getter @Setter
    private Boolean inverted;

    public Question(String text, Long number, Axe axe, Boolean inverted) {
        this.text = text;
        this.number = number;
        this.axe = axe;
        this.id = number;
        this.inverted = inverted;
    }
}
