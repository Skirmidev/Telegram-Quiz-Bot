package com.skirmisher.quizbot;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class QuizBean {
    @CsvBindByName(column = "questionID")
    int questionID;
    @CsvBindByName(column = "round")
    int round;
    @CsvBindByName(column = "question")
    String question;
}
