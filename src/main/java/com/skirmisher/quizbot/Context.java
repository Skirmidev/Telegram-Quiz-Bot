package com.skirmisher.quizbot;
import lombok.Data;

@Data
public class Context {
    boolean blockingResult = false;
    String result = "";
}