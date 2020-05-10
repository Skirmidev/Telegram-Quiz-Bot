package com.skirmisher.quizbot;

import com.opencsv.CSVReader;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import sun.security.krb5.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class QuizbotLongPoller extends TelegramLongPollingBot {
//  TODO: usernames

    @Override
    public void onUpdateReceived(Update update) {
        //disregard if there's no message
        if (!update.hasMessage() || !update.getMessage().isUserMessage() || !update.getMessage().hasText() || update.getMessage().getText().isEmpty())
            return;

        if(getRoundMasters().contains(update.getMessage().getChatId()) || update.getMessage().getChatId().equals(config("creatorid"))){ //(CHANGE: MAKE IT WHOEVER IS ASSIGNED TO ROUND? both can do it)
            //quizmaster
            System.out.println("Quizmaster: " + update.getMessage().getChat().getUserName() + " sent message " + update.getMessage().getText());

            switch(update.getMessage().getText()){
                case "/getCurrentQuestionID":
                    getCurrentQuestionID(update);
                    break;
                case "/setCurrentQuestionID":
                    setCurrentQuestionID(update);
                    break;
                case "/advanceQuestion":
                    advanceQuestion(update);
                    break;
                case "/advanceRound":
                    advanceRound(update);
                    break;
                case "/lockNewJoins":
                    lockNewJoins(update);
                    break;
                case "/unlockNewJoins":
                    unlockNewJoins(update);
                    break;
                case "/EnableQuizActive":
                    enableQuizActive(update);
                    break;
                case "/DisableQuizActive":
                    disableQuizActive(update);
                    break;
                default:
                    unrecognisedCommand(update);
                    break;
            }

        } else {
            System.out.println("User: " + update.getMessage().getChat().getUserName() + " sent message " + update.getMessage().getText());
            switch(update.getMessage().getText()){
                case "/join":
                    join(update);
                    break;
                case "/leave":
                    leave(update);
                    break;
                case "/nextQuiz":
                    nextQuiz(update);
                    break;
                default:
                    genericResponse(update);
                    break;
            }


            //other user

            //if message is /leave
                //you have unregistered for the quiz. We're sorry to see you go.

            //if message is /join
                //you are now registered for the quiz. Questions begin at [time]

            //if quiz is inactive
                //if user has not joined
                    //next quiz at [time]. please use /join to register for the quiz
                //if user has joined
                    //next quiz at [time]. If you would no longer like to participate, please type /leave

            //if quiz is active
                //if message is /editResponse
                    //Please let me know which question you would like to change your answer to:
                    /*
                        this code should allow an inline response
                        https://github.com/MonsterDeveloper/java-telegram-bot-tutorial/blob/master/lesson-6.-inline-keyboards-and-editing-message%27s-text.md source

                        will need to put everything in an if else, where else is if there's a callbackquery

                        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                        List<InlineKeyboardButton> rowInline = new ArrayList<>();
                        rowInline.add(new InlineKeyboardButton().setText("Update message text").setCallbackData("update_msg_text"));
                        // Set the keyboard to the markup
                        rowsInline.add(rowInline);
                        // Add it to the message
                        markupInline.setKeyboard(rowsInline);
                        message.setReplyMarkup(markupInline);

                     */

                //if questionID is 0
                    //The first question has yet to be asked, please remain seated

                //if questionID is <0
                    //your answer to [question] has been recorded:
        }


        //if quiz is active
            //switch (currentlyEditing)
            //if message is /editResponse
                //ask which response to edit
            //else
                //set current answer to current message

//        if (update.hasMessage() && update.getMessage().hasText()) {
//            SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
//                    .setChatId(update.getMessage().getChatId())
//                    .setText(update.getMessage().getText() + update.getMessage().getChatId());
//            try {
//                execute(message); // Call method to send the message
//            } catch (TelegramApiException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public String getBotUsername() {
        return config("botusername");
    }


    @Override
    public String getBotToken() {
        return config("bottoken");
    }



    // QUIZMASTER COMMANDS

    public void getCurrentQuestionID(Update update){

        int currentQuestion = -1;

        currentQuestion = Integer.parseInt(config("currentquestion"));

        if(currentQuestion != -1){
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("Current questionID: " + currentQuestion);

            send(message);
        } else {
            failedToLoad(update);
        }
    }

    public void setCurrentQuestionID(Update update) {
//        int value = 5;
//
//        updateConfig("currentquestion", ""+value); //TODO: WILL NOT CURRENTLY WORK
//
//
//        SendMessage message = new SendMessage()
//                .setChatId(update.getMessage().getChatId())
//                .setText("QuestionID is now: " + value + " but no matching question found");
//
//        try {
//            List<QuizBean> questions = DBLoader.loadQuiz();
//
//            for(QuizBean bean : questions){
//                if(bean.getQuestionID() == value){
//                    message = new SendMessage()
//                            .setChatId(update.getMessage().getChatId())
//                            .setText("QuestionID: " + bean.getQuestionID() + " Round: " + bean.getRound() + " Question: " + bean.getQuestion());
//                    send(message);
//                }
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        send(message);

        SendMessage message = new SendMessage()
               .setChatId(update.getMessage().getChatId())
                .setText("FEATURE IS NOT IMPLEMENTED. LEAVE ME ALONE.");
        send(message);
    }

    public void advanceQuestion(Update update){
        if(Boolean.parseBoolean(config("quizactive"))) {

            if (Integer.parseInt(config("currentround")) == 0 ||
                    !getMasterByRound(Integer.parseInt(config("currentround"))).equals(update.getMessage().getChatId()) ||
                    Integer.parseInt(config("currentround")) != roundOfNextQuestion()) { //check current sender is also current round person
                SendMessage message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("It isn't your round. I'm afraid I can't let you do that, Dave");
                send(message);
            } else {

                Boolean lastInRound = false;
                int currentQuestion = Integer.parseInt(config("currentquestion"));
                int currentRound = Integer.parseInt(config("currentround"));
                currentQuestion = currentQuestion + 1;
                int nextQuestionID = currentQuestion + 1;
                updateConfig("currentquestion", "" + currentQuestion);

                String question = "";
                String nextQuestion = "";

                try {
                    List<QuizBean> questions = DBLoader.loadQuiz();

                    for (QuizBean bean : questions) {
                        if (bean.getQuestionID() == currentQuestion) {
                            question = bean.getQuestion();
                        }
                        if (bean.getQuestionID() == nextQuestionID) {
                            if (bean.getRound() != currentRound) {
                                lastInRound = true;
                            }
                            nextQuestion = bean.getQuestion();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ArrayList<SendMessage> messages = new ArrayList<>();

                try {
                    for (UserBean user : DBLoader.loadUser()) {
                        if(user.isJoined()) {
                            SendMessage message = new SendMessage()
                                    .setChatId(user.getChatID())
                                    .setText("Round " + currentRound + " Question " + currentQuestion + ": " + question);
                            messages.add(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sendBulk(messages);
                //load the question, send the question


                if (lastInRound) {
                    SendMessage message = new SendMessage()
                            .setChatId(update.getMessage().getChatId())
                            .setText("Thank you, your round is now complete");
                    send(message);

                    try {
                        List<RoundBean> rounds = DBLoader.loadRound();

                        for (RoundBean round : rounds) {
                            //message for next round runner
                            if (round.getRound() == currentRound + 1) {
                                message = new SendMessage()
                                        .setChatId(round.getMaster())
                                        .setText("Your round is next. When ready, enter /advanceRound to begin. Please give some time for users to finish answering the previous round.");

                                send(message);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    //tell the master what the next question is
                    SendMessage message = new SendMessage()
                            .setChatId(update.getMessage().getChatId())
                            .setText("Next question is: " + nextQuestion);
                    send(message);
                }
            }
        }
    }

    public void advanceRound(Update update){
        if(Boolean.parseBoolean(config("quizactive"))){
            int round = Integer.parseInt(config("currentround"));
            round=round+1;
            updateConfig("currentround", ""+round);

            int currentQuestion = Integer.parseInt(config("currentquestion"));
            int nextQuestion = currentQuestion+1;

            String question = "";
            try {
                List<QuizBean> questions = DBLoader.loadQuiz();

                for (QuizBean bean : questions) {
                    if (bean.getQuestionID() == nextQuestion) {
                        question = bean.getQuestion();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //tell the next quizmaster that it is their round
            //tell them what the next question is
            //tell them what button to press to advance to the next question
            SendMessage message = new SendMessage()
                    .setChatId(getMasterByRound(round))
                    .setText("Your round has begun! Please type /advanceQuestion to send the next question");
            send(message);

            message = new SendMessage()
                    .setChatId(getMasterByRound(round))
                    .setText("Next question is: " + question);
            send(message);
        } else {
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("Quiz is not active, you may not advance the round!");
            send(message);
        }
    }

    public void lockNewJoins(Update update){
        boolean newJoinsAllowed = Boolean.parseBoolean(config("newjoinsallowed"));
        if(newJoinsAllowed){
            updateConfig("newjoinsallowed", "false");

            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("New joins have been disabled");

            send(message);
        } else {
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("New joins were already disabled");

            send(message);
        }
    }

    public void unlockNewJoins(Update update){
        boolean newJoinsAllowed = Boolean.parseBoolean(config("newjoinsallowed"));
        if(!newJoinsAllowed){
            updateConfig("newjoinsallowed", "true");

            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("New joins have been enabled");

            send(message);
        } else {
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("New joins were already enabled");

            send(message);
        }
    }

    public void enableQuizActive(Update update){
        boolean quizActive = Boolean.parseBoolean(config("quizactive"));

        if(!quizActive){
            updateConfig("quizactive", "true");

            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("Quiz is now active!");

            send(message);

            informQuizBeginning();
        } else {
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("Quiz was already active.");

            send(message);
        }
    }

    public void disableQuizActive(Update update){
        boolean quizActive = Boolean.parseBoolean(config("quizactive"));
        if(quizActive){
            updateConfig("quizactive", "false");

            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("Quiz has ended!");

            send(message);
        } else {
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("Quiz wasn't active");

            send(message);
        }
    }

    public void unrecognisedCommand(Update update) {
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText("Your command was not recognised. Please input one of the following:");

        send(message);

        sendQuizmasterCommands(update);
    }

    // USER COMMANDS

    public void join(Update update) {
        if(Boolean.parseBoolean(config("newjoinsallowed")) && !Boolean.parseBoolean(config("quizactive"))){

            try {
                List<UserBean> users = DBLoader.loadUser();
                boolean newUser = true;
                boolean updated = false;

                for(UserBean user : users){
                    if(user.getChatID() == update.getMessage().getChatId()){
                        //case user already joined
                        if(user.isJoined()){
                            SendMessage message = new SendMessage()
                                    .setChatId(update.getMessage().getChatId())
                                    .setText("You are already signed up for the next quiz on " + config("nextdate"));

                            send(message);
                            newUser = false;
                        } else {
                            //case user joined and left
                            user.setJoined(true);
                            newUser = false; updated=true;
                        }
                    }
                }

                if(newUser){
                    UserBean newbie = new UserBean();
                    newbie.setChatID(update.getMessage().getChatId());
                    newbie.setJoined(true);
                    newbie.setUsername(update.getMessage().getChat().getUserName());
                    users.add(newbie);
                    updated=true;
                }

                if(updated) {
                    DBLoader.saveUser(users);
                    SendMessage message = new SendMessage()
                            .setChatId(update.getMessage().getChatId())
                            .setText("You have been signed up for the quiz on " + config("nextdate"));

                    send(message);}

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("New users are not currently permitted to join");

            send(message);
        }
    }

    public void leave(Update update) {
        try {
            List<UserBean> users = DBLoader.loadUser();
            boolean newUser = true;
            boolean updated = false;

            for(UserBean user : users){
                if(user.getChatID() == update.getMessage().getChatId()){
                    //case user already joined
                    if(user.isJoined()){

                        user.setJoined(false);
                        updated=true;
                    }
                }
            }

            if(updated) {
                DBLoader.saveUser(users);
                SendMessage message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("You are no longer part of the next quiz. If you change your mind, you can /join again before it begins");

                send(message);
            } else {
                SendMessage message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("You were not signed up for this quiz.");

                send(message);
            }

            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    public void genericResponse(Update update){

        try{
            List<UserBean> users = DBLoader.loadUser();

            boolean partOfQuiz = false;

            for(UserBean user : users){
                if(user.getChatID() == update.getMessage().getChatId()) { partOfQuiz = user.isJoined();}
            }

            if(!partOfQuiz && Boolean.parseBoolean(config("quizactive"))){
                SendMessage message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("Quiz ongoing, I'm sorry, you did not register for this quiz");

                send(message);
            } else if (partOfQuiz && Boolean.parseBoolean(config("quizactive"))) {
                int currentRound = Integer.parseInt(config("currentround"));
                int currentQuestion = Integer.parseInt(config("currentquestion"));



                if(currentRound == 0 || currentQuestion == 0){
                    SendMessage message = new SendMessage()
                            .setChatId(update.getMessage().getChatId())
                            .setText("Hold your horse sonas, No questions have been asked yet");

                    send(message);
                } else {
                    try {
                        boolean hasAnsweredPreviously = false;

                        List<AnswerBean> answers = DBLoader.loadAnswer(currentRound);

                        for(AnswerBean answer : answers){
                            if(answer.getUserID().equals(update.getMessage().getChatId()) && answer.getQuestionID() == currentQuestion){
                                hasAnsweredPreviously = true;
                                answer.setAnswer(update.getMessage().getText());
                            }
                        }

                        if(!hasAnsweredPreviously){
                            AnswerBean newAnswer = new AnswerBean();
                            newAnswer.setAnswer(update.getMessage().getText());
                            newAnswer.setQuestionID(currentQuestion);
                            newAnswer.setUserID(update.getMessage().getChatId());
                            newAnswer.setUsername(update.getMessage().getChat().getUserName());
                            answers.add(newAnswer);
                        }

                        //save new answers
                        DBLoader.saveAnswer(answers, currentRound);

                        SendMessage message = new SendMessage()
                                .setChatId(update.getMessage().getChatId())
                                .setText("Your answer for Round " + currentRound + " Question " + currentQuestion + " has been set to: " + update.getMessage().getText());

                        send(message);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("Quiz is not active, input not recognised. The following commands are available: ");

            send(message);
            sendUserCommands(update);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void nextQuiz(Update update) {
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText("Next quiz scheduled for: " + config("nextdate"));

        send(message);
    }

    public void unrecognisedUserCommand(Update update) {
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText("Your command was not recognised. Please input one of the following:");

        send(message);

        sendUserCommands(update);
    }

    // MISC METHODS

    public void sendQuizmasterCommands(Update update) {
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText(   "/getCurrentQuestionID" + "\n" +
                            //"/setCurrentQuestionID" + "\n" +
                            "/advanceQuestion" + "\n" +
                            "/advanceRound" + "\n" +
                            "/lockNewJoins" + "\n" +
                            "/unlockNewJoins" + "\n" +
                            "/EnableQuizActive" + "\n" +
                            "/DisableQuizActive");

        send(message);
    }

    public void sendUserCommands(Update update) {
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText(   "/join" + "\n" +
                            "/nextQuiz" + "\n" +
                            "/leave");

        send(message);
    }


    public void send(SendMessage message){
        try {
            System.out.println("Sending message: " + message.getText() + " To chat ID: " + message.getChatId());
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendBulk(ArrayList<SendMessage> messages){
        for(SendMessage message : messages){
            send(message);
        }
    }

    public ArrayList<Long> getParticipants(){
        ArrayList<Long> participants = new ArrayList<>();

        try {
            List<UserBean> users = DBLoader.loadUser();

            for (UserBean bean : users) {
                if (bean.joined) {
                    participants.add(bean.chatID);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(participants.isEmpty()){
            System.out.println("ERROR: NO PARTICIPANTS");
        }
        return participants;
    }

    public ArrayList<Long> getRoundMasters(){
        ArrayList<Long> masters = new ArrayList<>();

        try {
            List<RoundBean> rounds = DBLoader.loadRound();

            for (RoundBean round : rounds) {
                masters.add(round.master);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(masters.isEmpty()){
            System.out.println("ERROR: NO ROUNDMASTERS");
        }
        return masters;
    }

    public void informQuizBeginning(){
        ArrayList<Long> participants = getParticipants();

        ArrayList<SendMessage> messages = new ArrayList<SendMessage>();

        //message participants
        for(long user : participants){
            SendMessage message = new SendMessage()
                    .setChatId(user)
                    .setText("The quiz has now begun! Please join us at twitch.tv/IrishFurries");

            messages.add(message);
        }

        //message roundmasters
        try {
            List<RoundBean> rounds = DBLoader.loadRound();

            for (RoundBean round : rounds) {
                SendMessage message = new SendMessage()
                        .setChatId(round.getMaster())
                        .setText("The quiz has now begun! You are assigned to round " + round.getRound());

                messages.add(message);

                //extra message for first round runner
                if(round.getRound()==1){
                     message = new SendMessage()
                            .setChatId(round.getMaster())
                            .setText("Your round is next. When ready, enter /advanceRound to begin");

                    messages.add(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendBulk(messages);
    }

//    public void sendQuestionToUsers(int roundNumber, int questionNumber, String questionString){
//        ArrayList<Long> participants = getParticipants();
//        ArrayList<SendMessage> messages = new ArrayList<>();
//
//        for(long user : participants){
//            SendMessage message = new SendMessage()
//                    .setChatId(user)
//                    .setText("Round " + roundNumber + " Question " + questionNumber + ": " + questionString);
//
//            messages.add(message);
//        }
//
//        sendBulk(messages);
//    }

    public void failedToLoad(Update update){
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText("Failed to load from file");

        send(message);
    }

    public String config(String element){
        try {
            List<ConfigBean> config = DBLoader.loadConfig();

            for(ConfigBean bean : config){
                if(bean.getElement().equals(element)){
                    return bean.getValue();
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("FAILEDTOLOAD: " + element);
        return "FAILEDTOLOAD: " + element;
    }

    public void updateConfig(String element, String value){
        try {
            List<ConfigBean> config = DBLoader.loadConfig();

            for(ConfigBean bean : config){
                if(bean.getElement().equals(element)){
                    bean.setValue(value);
                }
            }

            DBLoader.saveConfig(config);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public Long getMasterByRound(int round){
        try {
            List<RoundBean> config = DBLoader.loadRound();

            for(RoundBean bean : config){
                if(bean.getRound() == round){
                    return bean.getMaster();
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("FAILEDTOLOAD: masterbyround");
        return -1L;
    }

    public int roundOfNextQuestion(){
        //get current question
        int currentQuestion = Integer.parseInt(config("currentquestion"));
        int nextRound = -1;

        try {
            List<QuizBean> questions = DBLoader.loadQuiz();

            for (QuizBean bean : questions) {
                if (bean.getQuestionID() == currentQuestion+1) {
                    nextRound = bean.getRound();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return nextRound;
    }
}

