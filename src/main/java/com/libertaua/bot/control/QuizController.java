package com.libertaua.bot.control;

import com.libertaua.bot.entities.Question;
import com.libertaua.bot.entities.TelegramUser;
import com.libertaua.bot.enums.Axe;
import com.libertaua.bot.enums.Phase;
import com.libertaua.bot.persistence.DBManager;
import com.libertaua.bot.service.TelegramOutputService;
import com.libertaua.bot.util.CommonUtils;
import com.libertaua.bot.util.Ideology;
import com.libertaua.bot.util.ImageUtils;
import com.libertaua.bot.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;

/**
 * Class that is responsible for getting user through quiz
 * Strange architecture but ok (name change required)
 *
 * @author seaeagle
 */
@Service
public class QuizController {

    private TelegramOutputService output;
    private DBManager dbManager;
    private CommonUtils utils;
    private ImageUtils imageUtils;

    @Autowired
    public void setImageUtils(ImageUtils imageUtils) {
        this.imageUtils = imageUtils;
    }
    @Autowired
    public void setUtils(CommonUtils utils) { this.utils = utils; }
    @Autowired
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }
    @Autowired
    public void setOutput(TelegramOutputService output) {
        this.output = output;
    }

    /**
     * Simple method that starts main quiz from the beginning
     * @param currentUser TelegramUser who sent a message
     */
    public void startQuiz(TelegramUser currentUser) {
        currentUser.getBotState().setCurrentQuestion(utils.questionList.get(0)); //Set current question to first
        output.askQuestion(
                currentQuestionText(currentQuestion(currentUser)),
                currentUser.getChatId(),
                true
        );
        dbManager.saveUser(currentUser);
    }

    /**
     * Method that asks user next question
     * Used when user answer with any except a BACK button
     * @param currentUser TelegramUser who sent a message
     */
    public void askNext(TelegramUser currentUser) {

        Question question = currentQuestion(currentUser);
        //Get next question from the list
        int questionIndex = Math.toIntExact(question.getNumber());
        //So here we have 0-base index meet 1-base index, and +1 is implicit
        Question nextQuestion = utils.questionList.get(questionIndex); ///+1
        output.askQuestion(
                currentQuestionText(nextQuestion),
                currentUser.getChatId(),
                false
        );
        currentUser.getBotState().setCurrentQuestion(nextQuestion);
        dbManager.saveUser(currentUser);
    }

    /**
     * Method that asks user previous question
     * Used when user answer with a BACK button
     * @param currentUser TelegramUser who sent a message
     */
    public void askPrevious(TelegramUser currentUser) {
        Question question = currentQuestion(currentUser);
        int questionIndex = Math.toIntExact(question.getNumber());
        if (questionIndex == 1) return; //NO PREVIOUS QUESTION
        //Get previous question from the list
        //Same problem as above, 0-base index and 1-base, so to go back we need (-2)
        Question nextQuestion = utils.questionList.get(questionIndex - 2); /// -1
        output.askQuestion(
                currentQuestionText(nextQuestion),
                currentUser.getChatId(),
                (questionIndex == 2)
        );
        currentUser.getBotState().setCurrentQuestion(nextQuestion);
        dbManager.saveUser(currentUser);
    }

    /**
     * Obviously a method that is responsible for calculating and printing results
     * @param currentUser TelegramUser who sent a message
     */
    public void showResults(TelegramUser currentUser, Boolean trueCompass) {
        currentUser.setAnswerTime(OffsetDateTime.now());
        Pair<Integer, Integer> results = countResult(currentUser);
        boolean allZeros = checkNormie(currentUser);
        Pair<Double, Double> finalResults = new Pair<>(
                (100 * (double) (utils.MAX_SCORE_ECON + results.first) / (double) (2 * utils.MAX_SCORE_ECON)),
                (100 * (double) (utils.MAX_SCORE_POLI + results.second) / (double) (2 * utils.MAX_SCORE_POLI))
        );
        updateResults(currentUser, finalResults);
        output.sendCompass(currentUser.getChatId(),
                imageUtils.getResultsImage(currentUser.getUserId(), imageUtils.getUserPic(currentUser), finalResults, trueCompass),
                trueCompass ? null : "Такі координати визначив компас. Куди рухатись далі — обираєте ви."
                );
        if (!trueCompass) {
            output.sendAchievments(currentUser.getChatId(), imageUtils.getAchievment(currentUser.getUserId(), finalResults, allZeros));
        }

        dbManager.saveUser(currentUser);
    }

    /**
     * Responsible for sending all messages when Ideologies button clicked
     * @param currentUser Telegram user
     */
    public void showIdeologies(TelegramUser currentUser) {
        output.printMessage(currentUser.getChatId(),
                getIdeologies(utils.resultsToPair(currentUser.getResult())),
                false,
                null
        );
        output.sendImage(currentUser.getChatId(),
                currentUser.getBotState().getLastAnswer(),
                imageUtils.ideologies_pic,
                true
        );
        dbManager.saveUser(currentUser);
    }

    /**
     * Sends next meme according to DB field
     * @param currentUser TelegramUser
     */
    public void sendNextMeme(TelegramUser currentUser) {
        Long currentMeme = currentUser.getBotState().getLastMeme();
        if (currentMeme == null) currentMeme = 0L;
        if (currentMeme >= imageUtils.memes.size()) {
            output.printMessage(currentUser.getChatId(), "Вітаю! Ви продивились всі меми які в нас тільки були. Ви або справді любите меми з різнокольровими квадратиками <i>або намагаєтесь закрити нескінченну тлінність вашого буття різнокольровими квадратиками</i>\n" +
                    "\n" +
                    "поділяться своїми здобутками та вперед до нових вершин!", true, currentUser.getBotState().getLastAnswer());
            return;
        }
        output.sendImage(currentUser.getChatId(), currentUser.getBotState().getLastAnswer(), imageUtils.memes.get(Math.toIntExact(currentMeme)), currentMeme == imageUtils.memes.size() - 1);
        currentMeme++;
        currentUser.getBotState().setLastMeme(currentMeme);
        dbManager.saveUser(currentUser);

    }

    /**
     * Restarts test to the greeting point
     * @param currentUser needed TelegramUser
     */
    public void restartTest(TelegramUser currentUser) {
        //currentUser.getBotState().setLastMeme(0L);
        output.printGreeting(currentUser.getChatId());
        currentUser.getBotState().setPhase(Phase.GREETING);
        dbManager.saveUser(currentUser);
    }

    private String getAdMessage() {
        return "Сподіваємося, вам спободався наш тест і ви задумалися над деякими фундаментальними питаннями, можливо вперше. Щоб закріпити результат знань подивіться це відео від нашої Ліберті Берегині - це найкраще, що ви знайдете на Ютубі! Зустрінемось в коментарях \uD83D\uDE09  \n" +
                "https://youtu.be/lgPYXZT5_XY";
    }

    /**
     * Get string with 4 nearest political ideologies
     * @return String text ready to send
     */
    private String textResults() {

        StringBuilder text = new StringBuilder("WIP : ТУТ БУДУТЬ РЕЗУЛЬТАТИ");//\n\nОсь чотири політичні ідеології які можуть вам підійти:\n\n");
        /*
        text.append("<b>").append(ideologies.get(0).name).append("</b>\n");
        for (int i = 1; i < ideologies.size(); i++){
            text.append("<i>").append(ideologies.get(i).name).append("</i>\n");
        }
        */
        return text.toString();
    }

    /**
     * Get string with closest ideologies based on results
     * @param results pair of doubles in format (0, 100)
     * @return
     */
    private String getIdeologies(Pair<Double, Double> results){
        ArrayList<Ideology> ideologies = utils.getNearestDots(results);
        StringBuilder text = new StringBuilder("Ось три політичні ідеології які можуть вам підійти:\n\n");
        text.append("<b>").append(ideologies.get(0).name).append("</b>\n");
        for (int i = 1; i < ideologies.size()-1; i++){
            text.append("<i>").append(ideologies.get(i).name).append("</i>\n");
        }
        text.append("\n\nбонус: <i>майже точне</i> зображення політичних систем на компасі");
        return text.toString();
    }

    private Pair<Integer, Integer> countResult(TelegramUser currentUser) {
        Integer sumEconomical = 0;
        Integer sumPolitical = 0;
        for (int i = 0; i < utils.LAST_QUESTION; i++) {
            if (utils.questionList.get(i).getAxe() == Axe.POLITICAL)
                sumPolitical += currentUser.getAnswers().get(i);
            else
                sumEconomical += currentUser.getAnswers().get(i);
        }
        return new Pair<>(sumEconomical, sumPolitical);
    }
    private boolean checkNormie(TelegramUser currentUser) {
        for (int i = 0; i < utils.LAST_QUESTION; i++) {
            if (currentUser.getAnswers().get(i) != 0) return false;
        }
        return true;
    }
    private void updateResults(TelegramUser currentUser, Pair<Double, Double> results){
        currentUser.setResult(utils.resultsToString(results));
    }

    private Question currentQuestion(TelegramUser currentUser) {
        return currentUser.getBotState().getCurrentQuestion();
    }

    /**
     * Decor question text before printing
     * @param question current Question
     * @return         String in format:
     *
     * Тема : %тема%
     * Запитання %номер%
     *
     * %Власне запитання%
     */
    private String currentQuestionText(Question question) {
        return "Запитання " + question.getNumber()
                + ":\n\n" + question.getText();
    }



}
