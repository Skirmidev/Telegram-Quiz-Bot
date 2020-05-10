package com.skirmisher.quizbot;

import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DBLoader {
    public static List<RoundBean> loadRound() throws IOException {
        Path myPath = Paths.get("src/main/resources/roundDB.csv");

        try (BufferedReader br = Files.newBufferedReader(myPath,
                StandardCharsets.UTF_8)) {

            HeaderColumnNameMappingStrategy<RoundBean> strategy
                    = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(RoundBean.class);

            CsvToBean csvToBean = new CsvToBeanBuilder(br)
                    .withType(RoundBean.class)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<RoundBean> rounds = csvToBean.parse();

            return rounds;
        }
    }

    public static List<QuizBean> loadQuiz() throws IOException {
        Path myPath = Paths.get("src/main/resources/quizDB.csv");

        try (BufferedReader br = Files.newBufferedReader(myPath,
                StandardCharsets.UTF_8)) {

            HeaderColumnNameMappingStrategy<QuizBean> strategy
                    = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(QuizBean.class);

            CsvToBean csvToBean = new CsvToBeanBuilder(br)
                    .withType(QuizBean.class)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<QuizBean> quiz = csvToBean.parse();

            return quiz;
        }
    }

    public static List<UserBean> loadUser() throws IOException {
        Path myPath = Paths.get("src/main/resources/userDB.csv");

        try (BufferedReader br = Files.newBufferedReader(myPath,
                StandardCharsets.UTF_8)) {

            HeaderColumnNameMappingStrategy<UserBean> strategy
                    = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(UserBean.class);

            CsvToBean csvToBean = new CsvToBeanBuilder(br)
                    .withType(UserBean.class)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<UserBean> users = csvToBean.parse();

            return users;
        }
    }

    public static void saveUser(List<UserBean> input) {
        try{
            Writer writer = new FileWriter("src/main/resources/userDB.csv");
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
            beanToCsv.write(input);
            writer.close();
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            e.printStackTrace();
        }
    }

    public static List<AnswerBean> loadAnswer(int round) throws IOException {
        Path myPath = Paths.get("src/main/resources/answerDB" + round + ".csv");

        try (BufferedReader br = Files.newBufferedReader(myPath,
                StandardCharsets.UTF_8)) {

            HeaderColumnNameMappingStrategy<AnswerBean> strategy
                    = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(AnswerBean.class);

            CsvToBean csvToBean = new CsvToBeanBuilder(br)
                    .withType(AnswerBean.class)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<AnswerBean> answers = csvToBean.parse();

            return answers;
        }
    }

    public static void saveAnswer(List<AnswerBean> input, int round) {
        try{
            Writer writer = new FileWriter("src/main/resources/answerDB" + round + ".csv");
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
            beanToCsv.write(input);
            writer.close();
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            e.printStackTrace();
        }
    }

    public static List<ConfigBean> loadConfig() throws IOException {
        Path myPath = Paths.get("src/main/resources/config.csv");

        try (BufferedReader br = Files.newBufferedReader(myPath,
                StandardCharsets.UTF_8)) {

            HeaderColumnNameMappingStrategy<ConfigBean> strategy
                    = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(ConfigBean.class);

            CsvToBean csvToBean = new CsvToBeanBuilder(br)
                    .withType(ConfigBean.class)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<ConfigBean> config = csvToBean.parse();

            return config;
        }
    }

    public static void saveConfig(List<ConfigBean> input) {
        try{
            Writer writer = new FileWriter("src/main/resources/config.csv");
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
            beanToCsv.write(input);
            writer.close();
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            e.printStackTrace();
        }
    }
}
