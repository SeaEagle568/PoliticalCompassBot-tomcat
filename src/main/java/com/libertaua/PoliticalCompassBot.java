package com.libertaua;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.swing.*;

/**
 * Main spring boot application.
 * Registers telegram bot and puts Jackson ObjectManager to AC
 *
 * @author seaeagle
 */
@SpringBootApplication
@EnableAsync
public class PoliticalCompassBot extends SpringBootServletInitializer {
    public static void main(String[] args) {
        try {
            SpringApplication.run(PoliticalCompassBot.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

}
