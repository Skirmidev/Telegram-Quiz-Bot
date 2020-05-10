package com.skirmisher.quizbot;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class UserBean {
    @CsvBindByName(column = "username")
    String username;
    @CsvBindByName(column = "chatID")
    long chatID;
    @CsvBindByName(column = "enabled")
    boolean joined;
}
