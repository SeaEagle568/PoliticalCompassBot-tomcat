package com.libertaua;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Main spring boot application.
 * Registers telegram bot and puts Jackson ObjectManager to AC
 *
 * @author seaeagle
 */
@SpringBootApplication
@EnableAsync
public class PoliticalCompassBot {
    public static void main(String[] args) {
        unpackJar();
        SpringApplication application = new SpringApplication(PoliticalCompassBot.class);
        application.addListeners(new ApplicationPidFileWriter());
        application.run(args);
}

    private static void unpackJar() {
        final File parent = new File(".");
        String protocol = PoliticalCompassBot.class.getResource("PoliticalCompassBot.class").getProtocol();
        if (protocol.equals("jar")) {
            try (JarFile jar = new JarFile(new File(parent.getAbsolutePath() + "/PoliticalCompassBot.jar"))) {
                Enumeration<JarEntry> enumEntries = jar.entries();
                while (enumEntries.hasMoreElements()) {
                    JarEntry entry = enumEntries.nextElement();
                    File file = new File(parent + "/resources/" + entry.getName());
                    file.getParentFile().mkdirs();
                    if (entry.isDirectory()) {
                        file.mkdir();
                        continue;
                    }
                    FileUtils.copyInputStreamToFile(jar.getInputStream(entry), file);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

}
