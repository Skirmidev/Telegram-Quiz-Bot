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

            String returnVal = "";
            if(rs.next()){
                returnVal = rs.getString(1);
            }
            
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
        String query = "SELECT value FROM config WHERE element = 'quizactive'";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            rs.next();
            String returnVal = rs.getString(1);
            
            st.close();
            return(returnVal.equals("true"));
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        return false;
    }

    public static String getUserState(int userId){
        String query = "SELECT chatstate FROM participants WHERE userid = '" + userId + "'";

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

        return "";
    }

    public static void addNewUser(int userId, String username, int messageId, String name){
        String query = "INSERT INTO users(userid, username, activemessage, name) VALUES(?, ?, ?, ?)";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            PreparedStatement pst = con.prepareStatement(query);
            
            pst.setInt(1, userId);
            pst.setString(2, username);
            pst.setInt(3, messageId);
            pst.setString(4, name);
            pst.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addManagingUser(int userId, String username, String name){
        String query = "INSERT INTO users(userid, username, name) VALUES(?, ?, ?)";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            PreparedStatement pst = con.prepareStatement(query);
            
            pst.setInt(1, userId);
            pst.setString(2, username);
            pst.setString(3, name);
            pst.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isLastQuestionInRound(int roundId, int questionId){
        String query = "SELECT questionid FROM questions WHERE round = '" + roundId + "' ORDER BY questionid DESC LIMIT 1";

        
        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            String returnVal = "";
            if(rs.next()){
                returnVal = rs.getString(1);
            }

            st.close();
            System.out.println("Got last question in round for " + roundId + ". " + "it was: " + returnVal);

            return(Integer.parseInt(returnVal) == questionId);
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        return false;
    }

    public static boolean isLastRoundInQuiz(int roundId){
        
        String query = "SELECT round FROM rounds ORDER BY round DESC LIMIT 1";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);
            String returnVal = "";
            if(rs.next()){
                returnVal = rs.getString(1);
            }
            st.close();
            System.out.println("Got last round in quiz. it was: " + returnVal);

            return(Integer.parseInt(returnVal) == roundId);
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        return false;
    }
    
    public static boolean userExists(int userId){
        
        String query = "SELECT user FROM users WHERE userid = '"+userId+"'";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            boolean returnVal = rs.next();
            
            st.close();

            return returnVal;
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        return false;
    }

    public static User roundMasterForRound(int roundId){
        
        String query = "SELECT roundmaster FROM rounds WHERE round = '" + roundId + "'";
        int userId = 0;

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);
            if(rs.next()){
                userId = Integer.parseInt(rs.getString(1));
            }
            
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }




        String query2 = "SELECT username, name FROM users WHERE userid = '" + userId + "'";
        User u = new User();

        String username = "";
        String name = "";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query2);

            if(rs.next()){
                username = rs.getString(1);
                name = rs.getString(2);
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        u.setId(userId);
        u.setFirstName(name);
        u.setUserName(username);


        return u;
        //(name and id are main points here)
    }

    public static boolean addRound(int userId){
        String query = "INSERT INTO rounds(roundmaster) VALUES(?)";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            PreparedStatement pst = con.prepareStatement(query);
            
            pst.setInt(1, userId);
            int response = pst.executeUpdate();
            return (response != 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int addQuestion(int userId, String question){
        String query = "SELECT round FROM rounds WHERE roundmaster = '" + userId + "'";
        int roundId = 0;

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            if(rs.next()){
                roundId = Integer.parseInt(rs.getString(1));
            }

            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        //get largest question
        int largestQuestionVal = 0;
        String query2 = "SELECT questionid FROM questions WHERE round = '" + roundId + "'";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query2);

            while(rs.next()){
                int tmp = rs.getInt(1);
                if(tmp > largestQuestionVal){
                    largestQuestionVal = tmp;
                }
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }
        largestQuestionVal = largestQuestionVal+1;

        String query3 = "INSERT INTO questions(round, questionid, questiondata) VALUES(?, ?, ?)";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            PreparedStatement pst = con.prepareStatement(query3);
            
            pst.setInt(1, roundId);
            pst.setInt(2, largestQuestionVal);
            pst.setString(3, question);
            int response = pst.executeUpdate();
            return response;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String removeRound(int userId){
        String query = "SELECT round FROM rounds WHERE roundmaster = '" + userId + "'";
        int roundId = 0;

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            if(rs.next()){
                roundId = Integer.parseInt(rs.getString(1));
            }

            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        
        String query2 = "SELECT questionid, questiondata FROM questions WHERE round = '" + roundId + "'";
        String response = "";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query2);

            while(rs.next()){
                response = response + "Question " + rs.getInt(1) + ": " + rs.getString(2) + "\n";
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        
        String query3 = "DELETE FROM questions WHERE round = '" + roundId + "'";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            PreparedStatement pst = con.prepareStatement(query3);
            
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        

        return response;
    }

    public static String removeQuestion(int roundId, int questionId){
        String query = "SELECT questiondata FROM questions WHERE round = '" + roundId + "'";
        String response = "";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            if(rs.next()){
                response = response + "Question " + questionId + ": " + rs.getString(1) + "\n";
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        
        String query2 = "DELETE FROM questions WHERE round = '" + roundId + "' AND questionid = '" + questionId + "'";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            PreparedStatement pst = con.prepareStatement(query2);
            
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //TODO: recalculate question IDs

        return response;
    }

    public static String getFullQuiz(){
        String query = "SELECT round, questionid, questiondata FROM questions ORDER BY round ASC, questionid ASC";
        String response = "";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            while(rs.next()){
                response = response + "Round " + rs.getInt(1) + ", " + "Question " + rs.getInt(2) + ": " + rs.getString(3) + "\n";
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }
        return response;
    }

    public static String getRoundQuestions(int userId){
        String query = "SELECT round FROM rounds WHERE roundmaster = '" + userId + "'";
        int roundId = 0;

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            if(rs.next()){
                roundId = Integer.parseInt(rs.getString(1));
            }

            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }


        String query2 = "SELECT round, questionid, questiondata FROM questions WHERE round = '" + roundId + "' ORDER BY questionid ASC";
        String response = "";
        

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query2);

            while(rs.next()){
                response = response + "Round " + rs.getInt(1) + ", " + "Question " + rs.getInt(2) + ": " + rs.getString(3) + "\n";
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }
        return response;
    }

    public static int getRoundByMaster(int userId){
        String query = "SELECT round FROM rounds WHERE roundmaster = '" + userId + "'";
        int roundId = 0;

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            if(rs.next()){
                roundId = Integer.parseInt(rs.getString(1));
            }

            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }
        return roundId;
    }

    public static int getMasterByRound(int roundId){
        String query = "SELECT roundmaster FROM rounds WHERE round = '" + roundId + "'";
        int userId = 0;

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            if(rs.next()){
                userId = Integer.parseInt(rs.getString(1));
            }

            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }
        return userId;
    }

    public static int getParticipantMessage(int userId){
        String query = "SELECT activemessage FROM users WHERE userid = '" + userId + "'";
        int activeMessage = 0;

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            if(rs.next()){
                if(rs.getString(1) != null){
                    activeMessage = Integer.parseInt(rs.getString(1));
                }
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }
        return activeMessage;
    }

    public static void addAnswer(int roundId, int questionId, int userId, String answer){
        String query2 = "INSERT INTO answers(round, questionid, userid, answer) VALUES(?, ?, ?, ?)";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            PreparedStatement pst = con.prepareStatement(query2);
            
            pst.setInt(1, roundId);
            pst.setInt(2, questionId);
            pst.setInt(3, userId);
            pst.setString(4, answer);
            int response = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getRoundAnswers(int roundId, int userId){ //should also return the questions as a formatted string
        int currentQuestion = Integer.parseInt(configValue("currentquestion"));

        //FIRST NEED TO GET CURRENTQUESTION SO WE DON'T GRAB MORE THAN WE SHOULD
        String query = "SELECT questionid, answer FROM answers WHERE userid = '" + userId + "' AND round = '" + roundId + "' ORDER BY questionid ASC";
        String querytwo = "SELECT questionid, questiondata FROM questions WHERE round = '" + roundId + "' ORDER BY questionid ASC";
        Map<Integer, String> answers = new HashMap<>();
        Map<Integer, String> questions  = new HashMap<>();

        
        System.out.println("Got RoundAnswers for " + userId + "\n" + questions.toString() + "\n" + answers.toString() + "\n");

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            while(rs.next()){
                answers.put(rs.getInt(1), rs.getString(2));
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(querytwo);

            while(rs.next()){
                questions.put(rs.getInt(1), rs.getString(2));
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }
        String returnVal = "";

        for(Map.Entry<Integer, String> entry : questions.entrySet()){
            if(entry.getKey() <= currentQuestion){
                returnVal = returnVal + "Question " + entry.getKey() + ": " + entry.getValue() + "\n";
                returnVal = returnVal + "You answered: " + answers.get(entry.getKey()) + "\n";
            }
        }

        return returnVal;
    }

    public static String getQuestionData(int roundId, int questionId){
        String query = "SELECT questiondata FROM questions WHERE questionid = '" + questionId + "'";
        String returnVal = "";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            if(rs.next()){
                returnVal = rs.getString(1);
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        return returnVal;
    }

    public static void updateActiveMessage(int userId, int messageId){
        String query = "UPDATE users SET activemessage = '" + messageId + "' WHERE userid = '" + userId + "'";
        System.out.println("updating active message for: " + userId + " new message: " + messageId);

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            PreparedStatement pst = con.prepareStatement(query);
            
            int response = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateAnswer(int roundId, int questionId, int userId, String answer){
        String query = "UPDATE answers SET answer = '" + answer + "' WHERE userid = '" + userId + "' AND questionid = '" + questionId + "' AND round = '" + roundId + "'";
        int response = 0;
        try {
            Connection con = DriverManager.getConnection(url, user, null);
            PreparedStatement pst = con.prepareStatement(query);
            
            response = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(response == 0){
            //no rows were updated, maybe the user hasn't answered yet?
            addAnswer(roundId, questionId, userId, answer);
        }
    }

    public static void setParticipantState(int userId, String state){
        String query = "UPDATE participants SET chatstate = '" + state + "' WHERE userid = '" + userId + "'";
        try {
            Connection con = DriverManager.getConnection(url, user, null);
            PreparedStatement pst = con.prepareStatement(query);
            
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getParticipantState(int userId){
        String query = "SELECT chatstate FROM participants WHERE userid = '" + userId + "'";
        String state = "";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);
            if(rs.next()){
                state = rs.getString(1);
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        return state;
    }

    public static ArrayList<User> getAllParticipants(){
        String query = "SELECT userid FROM participants";
        ArrayList<Integer> participants = new ArrayList<>();
        ArrayList<User> users = new ArrayList<>();

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            while(rs.next()){
                participants.add(rs.getInt(1));
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        String querytwo = "SELECT username, userid, name FROM users";
        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(querytwo);

            while(rs.next()){
                User u = new User();
                u.setUserName(rs.getString(1));
                u.setId(rs.getInt(2));
                u.setFirstName(rs.getString(3));
                users.add(u);
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        return users;
    }

    public static HashMap<String, String> getEditables(int currentRound, int userId){
        HashMap<String, String> qewandeyy = new HashMap<>();

        int currentQuestion = Integer.parseInt(configValue("currentquestion"));

        String query = "SELECT questionid, answer FROM answers WHERE userid = '" + userId + "' AND round = '" + currentRound + "' ORDER BY questionid ASC";
        String querytwo = "SELECT questionid, questiondata FROM questions WHERE round = '" + currentRound + "' ORDER BY questionid ASC";
        Map<Integer, String> answers = new HashMap<>();
        Map<Integer, String> questions  = new HashMap<>();

        System.out.println("Got Editables for " + userId + "\n" + questions.toString() + "\n" + answers.toString() + "\n");

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            while(rs.next()){
                answers.put(rs.getInt(1), rs.getString(2));
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(querytwo);

            while(rs.next()){
                questions.put(rs.getInt(1), rs.getString(2));
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        for(Map.Entry<Integer, String> entry : questions.entrySet()){
            if(entry.getKey() <= currentQuestion){
                qewandeyy.put(entry.getValue(), answers.get(entry.getKey()));
            }
        }

        return qewandeyy;
    }

    public static String getAnswer(int userId, int currentRound, int questionId){
        String query = "SELECT answer FROM answers WHERE questionid = '" + questionId + "' AND round = '" + currentRound + "' AND userId = '" + userId + "'";
        String returnVal = "";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);

            if(rs.next()){
                returnVal = rs.getString(1);
            }
            
            st.close();
        } catch (SQLException ex) {
            System.out.println("Exceptioned: " + ex.getMessage());
            ex.printStackTrace();
        }

        return returnVal;
    }

    public static void removeParticipant(int userId){
        String query = "DELETE FROM participants WHERE userid = '" + userId + "'";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            PreparedStatement pst = con.prepareStatement(query);
            
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addParticipant(int userId, String state){
        String query = "INSERT INTO participants(userid, chatstate) VALUES(?, ?)";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            PreparedStatement pst = con.prepareStatement(query);
            
            pst.setInt(1, userId);
            pst.setString(2, state);
            int response = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasParticipant(int userId){
        String query = "SELECT userid FROM participants WHERE userid = '" + userId + "'";

        try {
            Connection con = DriverManager.getConnection(url, user, null);
            Statement st = con.createStatement();
            
            ResultSet rs = st.executeQuery(query);
            boolean returnVal = rs.next();
            rs.close();
            return returnVal;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
