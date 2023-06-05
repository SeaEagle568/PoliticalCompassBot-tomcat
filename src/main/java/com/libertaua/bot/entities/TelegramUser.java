package com.libertaua.bot.entities;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.context.annotation.Scope;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Entity class representing telegram_users table
 * It has tons of those nasty annotations that I dont even understand
 *
 * @author seaeagle
 */
@Entity(name="TelegramUser")
@Table(
        name="telegram_users",
        uniqueConstraints = { //It says that chat_id column should be unique
                @UniqueConstraint(name = "users_chatid_unique", columnNames = "chat_id")
        }
)
@TypeDef(
        name = "list-array",  //This kek allows to use Lists in DB (based)
        typeClass = ListArrayType.class
)
@Scope(value = "prototype") //Do i really need prototype?
@ToString
@NoArgsConstructor
public class TelegramUser {

    @SequenceGenerator(
            name = "users_sequence",
            sequenceName = "users_sequence",
            allocationSize = 1
    )

    @Id
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "users_sequence"
    )
    @Column(
            name="id",
            updatable = false,
            nullable = false
    )

    @Getter @Setter
    private Long id;

    @Column(
            name="name",
            nullable = false,
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String name;

    @Column(
            name="username",
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String username;

    @Column(
            name="chat_id",
            nullable = false,
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String chatId;

    @Column(
            name="user_id",
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String userId;

    @Column(
            name="seriously",
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String seriously;

    @Column(
            name="result",
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String result; //Format is '(a,b)'

    @Column(name="social_data_id")
    @Getter @Setter
    private Long socialDataId;


    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "bot_state_id", referencedColumnName = "id")
    @Getter @Setter
    private BotState botState;

    @Column(
            name="answer_list",
            columnDefinition = "integer[]"
    )
    @Type(type="list-array")
    @Getter @Setter
    private List<Integer> answers;

    @Column(
            name="answer_time",
            columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    @Getter @Setter
    private OffsetDateTime answerTime;

    @Column(name="permissions")
    @Getter @Setter
    private Long permissions;

    @Column(name = "broadcasting")
    @Getter @Setter
    private Boolean broadcasting;

    //If only Lombok had @SomeArgsConstructor......
    public TelegramUser(String name,
                        String username,
                        String chatId,
                        String userId,
                        String seriously,
                        String result,
                        Long socialDataId,
                        BotState botState,
                        List<Integer> answers) {

        this.name = name;
        this.username = username;
        this.chatId = chatId;
        this.userId = userId;
        this.seriously = seriously;
        this.result = result;
        this.socialDataId = socialDataId;
        this.botState = botState;
        this.answers = answers;
    }
}
