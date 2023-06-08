package com.libertaua.bot.service;

import com.libertaua.bot.Bot;
import com.libertaua.bot.entities.TelegramUser;
import com.libertaua.bot.enums.Answer;
import com.libertaua.bot.enums.Button;
import com.libertaua.bot.enums.Util;
import com.libertaua.bot.util.CommonUtils;
import com.libertaua.bot.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Service class somewhere near 'View' layer
 * Responsible for printing everything to end User in Telegram
 *
 * @author seaeagle
 */
@Service
public class TelegramOutputService {

    @Value("${bot.resources.social-url}")
    private String googleFormUrl;

    //dependencies
    private Bot bot;
    private CommonUtils utils;

    @Autowired
    public void setUtils(CommonUtils utils) {
        this.utils = utils;
    }
    @Autowired
    public void setBot(Bot bot) {
        this.bot = bot;
    }

    /**
     * Method that prints greeting to user
     * Greeting is loaded from file
     * @param chatId End user chat id
     */
    public void printGreeting(String chatId) {
        String greeting = "Ой-йой сталася дурня, зачекайте трошки...";
        try {
            greeting = new String(utils.getGreetingFile().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        printWithMarkup(greeting, chatId, startQuizMarkup());

    }


    public void copyMessage(String chatIdFrom, String chatIdTo, Message message) throws TelegramApiException {
        ForwardMessage copyMessage = ForwardMessage.builder()
                .chatId(chatIdTo)
                .fromChatId(chatIdFrom)
                .messageId(message.getMessageId())
                .build();
        bot.execute(copyMessage);
    }

    /**
     * A method that prints some text that is supposed to be Quiz question to user
     * Uses answer keyboard markup
     * @param text String message text
     * @param chatId End user chat id
     * @param isFirst Boolean if it is the first question (No need to make BACK button)
     */
    public void askQuestion(String text, String chatId, Boolean isFirst){
        if (isFirst) printWithMarkup(text,chatId, quizFirstKeyboard());
        else printWithMarkup(text, chatId, quizKeyboardMarkup());
    }

    /**
     * A method that sends results (Image + Text) to user
     *
     * @param chatId End user chat id
     * @param image Image(compass with dot) file from CommonUtils
     */
    public void sendAchievments(String chatId, Pair<File, Integer> image) {
        if (image == null) return;
        String message = "";
        sendImage(chatId, "", image.first, false);
        switch (image.second) {
                case 0 -> printWithMarkup("Вітаємо, ви досягли меж квадранту!\nВаш результат: крайній праволіберал. Серйозно обирали, чи заради мємів, але <i>ачівка</i> є: <b>капіталібертарій</b>", chatId, resultsKeyboard(message));
                case 1 -> printWithMarkup("Вітаємо, ви досягли меж квадранту!\nВаш результат: крайній ліволіберал. Серйозно обирали, чи заради мємів, але <i>ачівка</i> є: <b>анкомрад</b>", chatId, resultsKeyboard(message));
                case 2 -> printWithMarkup("Вітаємо, ви досягли меж квадранту!\nВаш результат: крайній авторитарно-правий. Серйозно обирали, чи заради мємів, але <i>ачівка</i> є: <b>трейдердьякон</b>", chatId, resultsKeyboard(message));
                case 3 -> printWithMarkup("Вітаємо, ви досягли меж квадранту!\nВаш результат: крайній авторитарно-лівий. Серйозно обирали, чи заради мємів, але <i>ачівка</i> є: <b>гулаггенсек</b>", chatId, resultsKeyboard(message));
                case 4 -> printWithMarkup("Ви відповіли \"Важко відповісти\" на всі запитання!\nА вас і правда не цікавить політика", chatId, resultsKeyboard(message));
                case 5 -> printWithMarkup("Вітаємо, ви досягли центру координат!\nВи більш-менш <b>радикальний центрист</b>, здатні смажити не тільки стейки", chatId, resultsKeyboard(message));
        }
    }

    /**
     * Sends image to user with text if needed
     * @param chatId chatid of TG user
     * @param image file with compass
     * @param text aux text
     */
    public void sendCompass(String chatId, File image, @Nullable String text){
        sendImage(chatId, "", image, false);
        if (text != null) printWithMarkup(text, chatId, resultsKeyboard(""));
    }

    /**
     * A method to ask last question (about seriousness)
     * @param currentUser Telegram user that send a message
     */
    public void askSerious(TelegramUser currentUser) {
        askQuestion("Ви серйозно проходили тест?", currentUser.getChatId(), false);
    }

    /**
     * Simple method for debugging purposes
     * @param chatId chatid of user
     * @param message debug message
     */
    public void debugMessage(String chatId, String message){
        printWithMarkup("Debug message:\n\n" + message, chatId, new ReplyKeyboardRemove(false));
    }

    /**
     * Prints message with results markup if %isAnswer% or with same markup else
     * @param chatId chat id of TG user
     * @param message message to send
     * @param isAnswer if new answer replykeyboard needed
     * @param reply what was last reply
     */
    public void printMessage(String chatId, String message, boolean isAnswer, String reply){
        if (isAnswer)  printWithMarkup(message, chatId, resultsKeyboard(reply));
        else printWithMarkup(message, chatId, new ReplyKeyboardRemove(false));
    }

    /**
     * Forwards article from channel
     * Can also send sticker or message
     * @param currentUser current TelegramUser
     */
    public void printArticle(TelegramUser currentUser) {
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setFromChatId("@liberta_ua");
        forwardMessage.setChatId(currentUser.getChatId());
        forwardMessage.setMessageId(345);
        SendMessage placeholder = new SendMessage();
        placeholder.setChatId(currentUser.getChatId());
        placeholder.setReplyMarkup(resultsKeyboard(currentUser.getBotState().getLastAnswer()));
        placeholder.setText("Можете обговорити статтю в нашому чаті, ми відкриті до діалогу!");
        //InputFile file = new InputFile();
        //placeholder.setSticker(new InputFile("CAACAgIAAxkBAAIJjmCmWTvJtq2vX6BVL7dlREbGKYdvAAIGAAMFzsItx-ODuRERKzwfBA"));
        try {
            bot.execute(forwardMessage);
            bot.execute(placeholder);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends an image to user
     * @param chatId chat id of tg user
     * @param message message with image
     * @param image image file
     * @param asFile true if to send as file
     */
    public void sendImage(String chatId, String message, File image, boolean asFile){
        if (asFile){
            Thread thread = new Thread(() -> sendFile(chatId, message, image));
            thread.start();
            return;
        }
        SendPhoto photo = new SendPhoto();
        photo.setPhoto(new InputFile(image));
        photo.setChatId(chatId);
        photo.setReplyMarkup(resultsKeyboard(message));
        try {
            bot.execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        assert (image.delete());
    }

    private void sendFile(String chatId, String message, File image){
        SendDocument photo = new SendDocument();
        photo.setDocument(new InputFile(image));
        photo.setChatId(chatId);
        photo.setReplyMarkup(resultsKeyboard(message));
        try {
            bot.execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        assert (image.delete());
    }
    /**
     * Simple method to print some text with buttons to user
     * @param text String with message to send
     * @param chatId End user chat id
     * @param markup Keyboard (or Remove (or Inline)) markup
     */
    private void printWithMarkup(String text, String chatId, ReplyKeyboard markup) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(chatId);
        message.setReplyMarkup(markup);
        message.enableHtml(true);
        message.setParseMode("HTML");
        try {
            bot.execute(message);
        } catch (TelegramApiException ignored) {}
    }

    private ReplyKeyboardMarkup startQuizMarkup() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        result.setKeyboard(
                List.of(
                        oneButtonRow(Util.LETSGO)
                )
        );
        return result;
    }

    private ReplyKeyboard requestResultsKeyboard() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        result.setKeyboard(
                List.of(
                        oneButtonRow(Util.RESULTS)
                )
        );
        return result;
    }

    /**
     * Get new markup based on previous,
     * shuffles it so that last clicked button goes down
     * If clicked on memes, get MORE_MEMES on top
     * Chat after article also goes on top
     * @param reply Last answer of user
     * @return needed markup
     */
    private ReplyKeyboardMarkup resultsKeyboard(String reply) {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        LinkedList<KeyboardRow> buttons = new LinkedList<>(Arrays.asList(
                oneButtonRow(Util.IDEOLOGIES),
                oneButtonRow(Util.ARTICLE),
                oneButtonRow(Util.TRUE),
                oneButtonRow(Util.MEMES),
                oneButtonRow(Util.CHAT),
                oneButtonRow(Util.RESTART)
        ));
        Button button = Button.getButton(reply);
        if (button.equals(Util.MEMES) || button.equals(Util.MEMES2)){
            buttons.remove(oneButtonRow(Util.MEMES));
            buttons.remove(oneButtonRow(Util.MEMES2));
            buttons.addFirst(oneButtonRow(Util.MEMES2));
        }
        else if (button.equals(Util.ARTICLE)){
            buttons.remove(oneButtonRow(Util.CHAT));
            buttons.remove(oneButtonRow(Util.ARTICLE));
            buttons.addFirst(oneButtonRow(Util.CHAT));
            buttons.addLast(oneButtonRow(Util.ARTICLE));
        }
        else if (buttons.contains(oneButtonRow(button))){
            buttons.remove(oneButtonRow(button));
            buttons.addLast(oneButtonRow(button));
        }
        result.setKeyboard(buttons);
        return result;
    }

    /**
     * Simple method that returns IMMUTABLE List of buttons for quiz
     * @return List<KeyboardRow> with 5 buttons, one button per row all Answer type
     */
    private List<KeyboardRow> quizBasicButtonList(){
        return List.of(
                oneButtonRow(Answer.STRONG_AGREE),
                oneButtonRow(Answer.WEAK_AGREE),
                oneButtonRow(Answer.DONT_KNOW),
                oneButtonRow(Answer.WEAK_DISAGREE),
                oneButtonRow(Answer.STRONG_DISAGREE)
        );
    }



    /**
     * Simple method that converts list of standard quiz buttons to KeyboardMarkup
     * @return KeyboardMarkup with 5 buttons
     */
    private ReplyKeyboardMarkup quizFirstKeyboard() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        result.setKeyboard(quizBasicButtonList());
        return result;
    }

    /**
     * Simple method that converts list of standard quiz buttons + BACK button to KeyboardMarkup
     * @return KeyboardMarkup with 6 buttons
     */
    private ReplyKeyboardMarkup quizKeyboardMarkup() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        List<KeyboardRow> buttonList = new ArrayList<>(quizBasicButtonList());
        buttonList.add(oneButtonRow(Util.BACK));
        result.setKeyboard(buttonList);
        return result;
    }

    private KeyboardRow oneButtonRow(Button button) {
        KeyboardRow result = new KeyboardRow();
        result.add(button.getText());
        return result;
    }

}
