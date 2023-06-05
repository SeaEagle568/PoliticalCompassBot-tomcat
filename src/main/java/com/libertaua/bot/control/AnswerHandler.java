package com.libertaua.bot.control;

import com.libertaua.bot.entities.TelegramUser;
import com.libertaua.bot.enums.Answer;
import com.libertaua.bot.enums.Button;
import com.libertaua.bot.enums.Util;
import com.libertaua.bot.persistence.DBManager;
import com.libertaua.bot.service.TelegramOutputService;
import com.libertaua.bot.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handler class that is responsible for button answers
 *
 * @author seaeagle
 */
@Service
public class  AnswerHandler {

    private TelegramOutputService output;
    private DBManager dbManager;
    private QuizController quizController;
    private CommonUtils utils;

    @Autowired
    public void setUtils(CommonUtils utils) {
        this.utils = utils;
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
     * Parsing message method called from MainController
     * @param message String input message
     * @param currentUser Telegram user who send message
     */
    public void parseMessage(String message, TelegramUser currentUser) {
        switch (currentUser.getBotState().getPhase()) {
            case PRESTART -> quizController.restartTest(currentUser);
            case GREETING -> greetingAnswer(message, currentUser);
            case TESTING -> handleQuizAnswer(message, currentUser);
            case SOCIAL -> {
                handleLastAnswer(message, currentUser);
            }
            case RESULTS -> handleResultsAnswer(message, currentUser);
        }

    }

    private void handleResultsAnswer(String message, TelegramUser currentUser) {
        Button button = Button.getButton(message);
        //output.debugMessage(currentUser.getChatId(), message + " " + button.getText());
        if (button.equals(Util.IDEOLOGIES)){
            quizController.showIdeologies(currentUser);
            return;
        }
        if (button.equals(Util.ARTICLE)){
            output.printArticle(currentUser);
            return;
        }
        if (button.equals(Util.RESTART)){
            quizController.restartTest(currentUser);
            return;
        }
        if (button.equals(Util.MEMES) || button.equals(Util.MEMES2)){
            quizController.sendNextMeme(currentUser);
            return;
        }
        if (button.equals(Util.CHAT)){
            output.printMessage(currentUser.getChatId(), "питання, пропозиції, постіронія\n\nhttps://t.me/joinchat/UsgLOMLbkyvE8Lve", true, currentUser.getBotState().getLastAnswer());
            return;
        }
        if (button.equals(Util.TRUE)){
            quizController.showResults(currentUser, true);
        }

    }

    private void handleLastAnswer(String message, TelegramUser currentUser) {
        Button button = Button.getButton(message);
        //If BACK print previous question
        if (button.equals(Util.BACK) &&
                currentUser.getBotState().getCurrentQuestion().getNumber() != 1) {

            goBack(currentUser);
            return;
        }
        //No button found, random text -> ignore
        if (button.getButtonType().equals("UTIL")) return;

        //Else add results
        assert button instanceof Answer;
        currentUser.setSeriously(((Answer) button).name());
        sendResults(currentUser);
    }

    private void greetingAnswer(String message, TelegramUser currentUser) {
        if (!message.equals(Util.LETSGO.getText())) return;
        startTest(currentUser);
    }

    /**
     * Method that understands what button while test was clicked
     * And recounts result delegating the other to QuizController
     * @param message String input message
     * @param currentUser Telegram user who send message
     */
    private void handleQuizAnswer(String message, TelegramUser currentUser) {
        Button button = Button.getButton(message);
        //If BACK print previous question
        if (button.equals(Util.BACK) &&
                currentUser.getBotState().getCurrentQuestion().getNumber() != 1) {

            goBack(currentUser);
            return;
        }
        //No button found, random text -> ignore
        if (button.getButtonType().equals("UTIL")) return;

        //Else go to next question
        assert button instanceof Answer;
        goForward((Answer) button, currentUser);
    }


    /**
     * Annihilates results from last question, then asks quizController to do something
     * @param currentUser Telegram user who send message
     */
    private void goBack(TelegramUser currentUser){
        quizController.askPrevious(currentUser);
    }
    /**
     * Counts results and goes to next question
     * @param button Answer button that was pressed
     * @param currentUser Telegram user who send message
     */
    private void goForward(Answer button, TelegramUser currentUser){
        int questionNumber = Math.toIntExact(currentUser.getBotState().getCurrentQuestion().getNumber());
        boolean inverted = currentUser.getBotState().getCurrentQuestion().getInverted();

        Integer buttonValue = button.getValue(inverted);
        currentUser.getAnswers().set(questionNumber-1, buttonValue);

        if (questionNumber == utils.LAST_QUESTION.intValue()) { //if last show results
            askSerious(currentUser);
        }
        else quizController.askNext(currentUser); //else go next
    }

    private void startTest(TelegramUser currentUser) {
        dbManager.nextPhase(currentUser.getBotState());
        quizController.startQuiz(currentUser);
    }

    private void sendResults(TelegramUser currentUser){
        dbManager.nextPhase(currentUser.getBotState());
        quizController.showResults(currentUser, false);
    }

    private void askSerious(TelegramUser currentUser){
        dbManager.nextPhase(currentUser.getBotState());
        output.askSerious(currentUser);

    }

}
