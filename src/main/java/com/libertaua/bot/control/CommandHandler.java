package com.libertaua.bot.control;

import com.libertaua.bot.entities.TelegramUser;
import com.libertaua.bot.enums.Phase;
import com.libertaua.bot.persistence.DBManager;
import com.libertaua.bot.service.BroadcastService;
import com.libertaua.bot.service.TelegramOutputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Little handler that deals with telegram commands
 *
 * @author seaeagle
 */
@Service
public class CommandHandler {

    @Value("${bot.name}")
    private String botUsername;

    private DBManager dbManager;
    private TelegramOutputService output;
    private QuizController quizController;
    private BroadcastService broadcastService;

    @Autowired
    public void setBroadcastService(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }
    @Autowired
    public void setQuizController(QuizController quizController) {
        this.quizController = quizController;
    }
    @Autowired
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }
    @Autowired
    public void setOutput(TelegramOutputService output) {
        this.output = output;
    }

    /**
     * Method called by MainController
     * Now only checks /start
     * @param message String input message
     * @param currentUser Telegram user who send message
     */
    public void parseMessage(String message, TelegramUser currentUser) {
        if (isStartCommand(message)){
            startGreeting(currentUser);
        }
        else if (isBroadcastCommand(message)) {
            parseBroadcast(currentUser);
        }
    }

    private void parseBroadcast(TelegramUser currentUser) {
        if (currentUser.getPermissions() == null || (currentUser.getPermissions() & 1) == 0) return;
        broadcastService.welcomeBroadcast(currentUser);
        dbManager.saveUser(currentUser);
    }


    /**
     * Only if user on PRESTART sends greetings
     * @param currentUser Telegram user who send message
     */
    private void startGreeting(TelegramUser currentUser) {
        currentUser.getBotState().setPhase(Phase.PRESTART);
        dbManager.saveUser(currentUser);
        quizController.restartTest(currentUser);
    }

    private boolean isStartCommand(String message) {
        return  (message.equalsIgnoreCase("/start")
                || message.equalsIgnoreCase("/start@" + botUsername)
                || message.equalsIgnoreCase("/restart")
                || message.equalsIgnoreCase("/restart@" + botUsername));
    }

    private boolean isBroadcastCommand(String message) {
        return (message.equals("/broadcast") || message.equals("/broadcast@" + botUsername));
    }
}
