package com.skirmisher.quizbot;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class RoundBean {
    @CsvBindByName(column = "roundID")
    int round;
    @CsvBindByName(column = "masterID")
    Long master; //chat ID of the quizmaster of this round
}