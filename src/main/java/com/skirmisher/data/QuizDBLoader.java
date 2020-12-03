package com.skirmisher.data;

import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import com.skirmisher.data.beans.*;
import java.time.*;
import java.util.Map;
import java.util.HashMap;
import org.telegram.telegrambots.meta.api.objects.InputFile;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import org.telegram.telegrambots.meta.api.objects.User;

public class QuizDBLoader {

    static String url = "jdbc:postgresql://localhost:5432/quizbot-db";
    static String user = "postgres";

    public static String configValue(String value){
        String query = "SELECT value FROM config WHERE element = '" + value + "'";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            rs.next();
            String returnVal = rs.getString(1);
            
            st.close();
            return returnVal;
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        return "FAILEDTOLOAD: " + value;
    }

    public static void updateConfigValue(String elementToUpdate, String value){
        String query = "UPDATE config SET value = ? WHERE element = ?";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            PreparedStatement pst = con.prepareStatement(query);
            
            pst.setString(1, value);
            pst.setString(2, elementToUpdate);
            pst.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean quizStarted(){
        return false;
    }

    public static boolean beforeQuizStart(){
        return false;
    }

    public static String getUserState(int userId){
        return "";
    }

    public static void addNewUser(int userId, String username, int messageId, String name){

    }

    public static boolean isLastQuestionInRound(){
        return false;
    }

    public static boolean isLastRoundInQuiz(){
        return false;
    }

    public static User roundMasterForRound(int round){
        return new User();
        //(name and id are main points here)
    }

    public static boolean addRound(int userId){
        return false;
    }

    public static int addQuestion(int userId, String question){
        return 0;
    }

    public static String removeRound(int userId){
        return "";
    }

    public static String removeQuestion(int roundId, int questionId){
        return "";
    }

    public static String getFullQuiz(){
        return "";
    }

    public static String getRoundQuestions(int userId){
        return "";
    }

    public static int getRoundByMaster(int userId){
        return 0;
    }

    public static int getMasterByRound(int rouindId){
        return 0;
    }

    public static int getParticipantMessage(int userId){
        return 0;
    }

    public static void addAnswer(int roundId, int questionId, int userId, String answer){

    }

    public static String getRoundAnswers(int roundId, int userId){ //should also return the questions as a formatted string
        return "";
    }

    public static String getQuestionData(int roundId, int questionId){
        return "";
    }

    public static void updateActiveMessage(int userId, int messageId){
        
    }

    public static void updateAnswer(int roundId, int questionId, int userId, String answer){
        
    }

    public static void setParticipantState(int userId, String state){
        
    }

    public static String getParticipantState(int userId){
        return "";
    }

    public static ArrayList<User> getAllParticipants(){
        return new ArrayList<User>();
    }

    public static ArrayList<String> getEditables(int currentRound, int userId){
        return new ArrayList<String>();
    }

    public static String getAnswer(int userId, int currentRound, int questionId){
        return "";
    }

    public static void removeParticipant(int userId){
        
    }

    public static void addParticipant(int userId, String state){
        
    }
}
