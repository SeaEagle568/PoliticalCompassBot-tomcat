package com.libertaua.bot.control;

import com.libertaua.bot.entities.BotState;
import com.libertaua.bot.entities.TelegramUser;
import com.libertaua.bot.persistence.DBManager;
import com.libertaua.bot.service.BroadcastService;
import com.libertaua.bot.service.TelegramOutputService;
import com.libertaua.bot.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is main controller class that partially parses message
 * Delegates further parsing to handlers
 * Gets TelegramUser also
 *
 * @author seaeagle
 */
@Service
public class MainController {

    private AnswerHandler answerHandler;
    private CommandHandler commandHandler;
    private DBManager dbManager;
    private CommonUtils utils;
    private TelegramOutputService output;
    private BroadcastService broadcastService;

    @Autowired
    public void setBroadcastService(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }
    @Autowired
    public void setOutput(TelegramOutputService output) {
        this.output = output;
    }
    @Autowired
    public void setUtils(CommonUtils utils) {
        this.utils = utils;
    }
    @Autowired
    public void setAnswerHandler(AnswerHandler answerHandler) {
        this.answerHandler = answerHandler;
    }
    @Autowired
    public void setCommandHandler(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }
    @Autowired
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * A method that razgrebae update
     * If it has no text - exit
     * If it is a command - CommandHandler is to help
     * Else it is answer and we have AnswerHandler for that
     * @param update Update from Bot class
     */
    public void razgrebiUpdate(Update update) {
        if (!update.hasMessage()) return;
        Message message = update.getMessage();
        if (message.isGroupMessage() || message.isChannelMessage() || message.isSuperGroupMessage()) return;
        Chat chat = message.getChat();
        /*
        if (message.hasSticker()){
            output.debugMessage(chat.getId().toString(), message.getSticker().getFileId());
        }
        */
        TelegramUser currentUser = getUser(
                chat.getFirstName() + " " + chat.getLastName(),
                chat.getUserName(),
                chat.getId().toString(),
                String.valueOf(message.getFrom().getId()),
                message.getText()
        );
        if (Boolean.TRUE.equals(currentUser.getBroadcasting())) {
            broadcastService.broadcast(currentUser, message);
            return;
        }
        if (!message.hasText()) return;

        if (message.isCommand())
            commandHandler.parseMessage(message.getText(), currentUser);
        else
            answerHandler.parseMessage(message.getText(), currentUser);
    }

    /**
     * Method that searches user in DB and if there is no such user creates one
     * @param name String Firstname + Lastname
     * @param username String Username
     * @param chatId End user chat id
     * @param message String message
     * @return        Returns TelegramUser entity
     */
    private TelegramUser getUser(String name, String username, String chatId, String userId, String message){

        TelegramUser user;
        if (dbManager.userExists(chatId)) {
            user = dbManager.userByChatId(chatId);
            if (message != null) user.getBotState().setLastAnswer(message);
            user.setUserId(userId);
            dbManager.saveUser(user);
            return user;
        }
        BotState state = new BotState(message, null, utils.questionList.get(0), 0);
        user = new TelegramUser(
                name,
                username,
                chatId,
                userId,
                "0",
                "0,0",
                getNewID(),
                state,
                new ArrayList<>(utils.getEmptyList())
        );
        state.setUser(user);
        dbManager.saveUser(user);
        return user;
    }

    private Long getNewID() {
        return ThreadLocalRandom.current().nextLong(10000000000L, 100000000000L);
    }

}
