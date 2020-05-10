package com.skirmisher.quizbot;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class AnswerBean extends DBLoader {

    @CsvBindByName(column = "questionID")
    private int questionID;
    @CsvBindByName(column = "userID")
    private Long userID;
    @CsvBindByName(column = "Answer")
    private String answer;
}