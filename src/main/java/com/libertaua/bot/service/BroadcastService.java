package com.libertaua.bot.service;


import com.libertaua.bot.entities.TelegramUser;
import com.libertaua.bot.persistence.DBManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Service
public class BroadcastService {

    private DBManager dbManager;
    private TelegramOutputService output;

    @Autowired
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    @Autowired
    public void setOutput(TelegramOutputService output) {
        this.output = output;
    }

    public void broadcast(TelegramUser fromUser, Message message) {
        fromUser.setBroadcasting(false);
        dbManager.saveUser(fromUser);
        List<String> users = dbManager.getAllChatIds();
        new Thread(() -> {
            for (String chatId : users) {
                try {
                    output.copyMessage(fromUser.getChatId(), chatId, message);
                    Thread.sleep(34);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                } catch (TelegramApiException ignored) {
                }
            }
            output.printMessage(fromUser.getChatId(), "Broadcast finished successfully", false, null);

        }).start();
    }

    public void welcomeBroadcast(TelegramUser user) {
        output.printMessage(user.getChatId(), "Відправте повідомлення для бродкасту:", false, null);
        user.setBroadcasting(true);
    }
}
