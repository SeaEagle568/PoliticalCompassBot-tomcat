package com.libertaua.bot.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.libertaua.bot.entities.Question;
import com.libertaua.bot.enums.Axe;
import com.libertaua.bot.persistence.DBManager;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A piece of human metabolic output
 * Here are different useful functions (such as conversions)
 * As well as commonly used constants and resources
 *
 * @author seaeagle
 */
@Component
public class CommonUtils {
    //resources
    public ArrayList<Question> questionList = new ArrayList<>();
    public ArrayList<Ideology> ideologiesList = new ArrayList<>();

    public Long LAST_QUESTION;
    public int MAX_SCORE_ECON = 0;
    public int MAX_SCORE_POLI = 0;

    private String questionsFile = getResource("questions.json").getPath();
    private String ideologiesFile = getResource("ideologies.json").getPath();

    @Getter
    private String greetingFile = getResource("greeting.txt").getPath();
    @Getter
    private String devChatId = "@seaeagle_dt";

    private ObjectMapper objectMapper;
    private DBManager dbManager;
    private ImageUtils imageUtils;

    @Autowired
    public void setImageUtils(ImageUtils imageUtils) {
        this.imageUtils = imageUtils;
    }
    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Autowired
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }


    private File getResource(String name) {
        try {
            return new File(CommonUtils.class.getClassLoader().getResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method that converts a Pair of integers (result) in string for
     * DB 'results' in format '(a,b)'
     * @param results Input Pair<Integer, Integer> of result
     * @return        String in format '(a,b)' where a = pair.first, b = pair.second
     */
    public String resultsToString(Pair<Double, Double> results){
        return results.first.toString() +
                "," +
                results.second.toString();
    }
    public Pair<Double, Double> resultsToPair(String str){
        String x = str.substring(0, str.indexOf(','));
        String y = str.substring(str.indexOf(',')+1);
        return new Pair<>(Double.parseDouble(x), Double.parseDouble(y));
    }

    /**
     * Spring post-construct method that loads all resources and constants
     * DO NOT call manually
     * I avoided just making a static class because Sanya Balashov said it is anti-pattern and i trusted him
     */
    @PostConstruct
    public void initializeUtils(){
        try {
            //Reading a json an deserializing questions
            questionList = objectMapper.readValue(
                    new FileReader(questionsFile),
                    new TypeReference<>(){}
            );
            ideologiesList = objectMapper.readValue(
                    new FileReader(ideologiesFile),
                    new TypeReference<>() {
                    }
            );
        } catch (IOException e) {
            String error = "Error updating questions or ideologies";
            System.err.println(error);
            e.printStackTrace();
        }
        dbManager.saveQuestions(questionList); //updating db with questions

        LAST_QUESTION = (long) questionList.size();
        Logger.getLogger("Initializer").log(Level.INFO, "Loaded " + LAST_QUESTION + " questions, uploaded to DB");
        for (Question question : questionList){
            if (question.getAxe() == Axe.POLITICAL) MAX_SCORE_POLI += 2;
            else MAX_SCORE_ECON += 2;
        }



    }

    /**
     * A method that takes one dot and returns list of 4 ideologies, nearest to that dot
     * @param dot A Pair of doubles - result dot on coordinates
     * @return Array List of 4 ideologies
     */
    public ArrayList<Ideology> getNearestDots(Pair<Double, Double> dot){
        ArrayList<Ideology> result = new ArrayList<>();
        ArrayList<Pair<Integer, Double>> distance = new ArrayList<>();
        for (int i = 0; i < ideologiesList.size(); i++){
            distance.add(new Pair<>(i, getDistance(dot, ideologiesList.get(i))));
        }
        distance.sort(Comparator.comparing(a -> a.second));
        for (int i = 0; i < 4; i++) {
            result.add(ideologiesList.get(distance.get(i).first));
        }
        return result;
    }

    private Double getDistance(Pair<Double, Double> dot, Ideology ideology) {
        return Math.sqrt(Math.pow(dot.first - ideology.coords.first, 2) + Math.pow(dot.second - ideology.coords.second, 2));
    }

    /**
     * Method to return ArrayList with N zeros (N = number of questions)
     * Used in new TelegramUser initialization for 'answers' DB field
     * @return Returns ArrayList<Integer> full of zeros
     */
    public ArrayList<Integer> getEmptyList(){
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < LAST_QUESTION; i++){
            list.add(0);
        }
        return list;
    }



}
