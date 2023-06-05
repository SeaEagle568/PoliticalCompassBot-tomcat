package com.libertaua.bot;

import com.libertaua.bot.control.MainController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PostConstruct;

/**
 * Telegram Long Polling bot class.
 * Registers in Telegram, delegates all updates to MainController
 *
 * @author seaeagle
 */
@Component
public class Bot extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    private MainController controller;

    @Autowired
    public void setController(MainController controller) {
        this.controller = controller;
    }

    @PostConstruct
    private void OnStart(){
        System.out.println("[" + java.time.LocalDateTime.now() + "]"
                + " Bot successfully started"
        );
    }

    @Override
    public void onUpdateReceived(Update update) {
       controller.razgrebiUpdate(update);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


}
