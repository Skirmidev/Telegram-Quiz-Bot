package com.skirmisher.quizbot;

public class Utilities {
    public static String makeMarkdownFriendly(String input){
        String corrected = input
        .replace("_","\\_")
        .replace("*","\\*")
        .replace("[","\\[")
        .replace("]","\\]")
        .replace("(","\\(")
        .replace(")","\\)")
        .replace("~","\\~")
        .replace("`","\\`")
        .replace(">","\\>")
        .replace("#","\\#")
        .replace("+","\\+")
        .replace("-","\\-")
        .replace("=","\\=")
        .replace("|","\\|")
        .replace("{","\\{")
        .replace("}","\\}")
        .replace(".","\\.")
        .replace("!","\\!");

        return corrected;
    }
}