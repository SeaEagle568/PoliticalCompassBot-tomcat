package com.libertaua.bot.persistence;

import com.libertaua.bot.entities.BotState;
import com.libertaua.bot.entities.Question;
import com.libertaua.bot.entities.TelegramUser;
import com.libertaua.bot.enums.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Main DB interface for comfortable access to all repositories
 * Maybe I should refactor this and make something more standard-pattern-friendly
 *
 * @author seaeagle
 */
@Repository
public class DBManager {
    private TelegramUserRepository userRepository;
    private BotStateRepository botStateRepository;
    private QuestionRepository questionRepository;

    @Autowired
    public void setUserRepository(TelegramUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Autowired
    public void setBotStateRepository(BotStateRepository botStateRepository) {
        this.botStateRepository = botStateRepository;
    }
    @Autowired
    public void setQuestionRepository(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public TelegramUser userByChatId(String chatId) {
        return userRepository.findByChatId(chatId);
    }
    public boolean userExists(String chatId){
        return userRepository.existsByChatId(chatId);
    }
    public void saveUser(TelegramUser user){
        saveState(user.getBotState()); //Saving state first (It can be optimized)
        userRepository.save(user);
    }
    public void saveQuestions(List<Question> question){
        questionRepository.saveAll(question);
    }
    public void saveState(BotState state){
        botStateRepository.save(state);
    }
    public List<String> getAllChatIds() {
        return userRepository.findAllChatID();
    }

    /**
     * Method forwarding user up to the next stage
     * The next stage after RESULTS is GREETING so it is kinda cyclic
     * @param state Current state that needs to evolve
     */
    public void nextPhase(BotState state){
        switch (state.getPhase()) {
            case PRESTART, RESULTS -> state.setPhase(Phase.GREETING);
            case GREETING -> state.setPhase(Phase.TESTING);
            case TESTING -> state.setPhase(Phase.SOCIAL);
            case SOCIAL -> state.setPhase(Phase.RESULTS);
        }
        saveState(state);
    }


}
