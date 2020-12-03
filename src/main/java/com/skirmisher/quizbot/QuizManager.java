package com.skirmisher.quizbot;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import java.util.List;
import java.util.Collections;
import java.util.Map;

import com.skirmisher.data.QuizDBLoader;

import java.util.ArrayList;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.User;

public class QuizManager {
    static char prefix = '/';

    public static void runControl(Context context, Update update, AvalitechQuizSystem bot){
        //if user not already in user list, add them
        if(!QuizDBLoader.userExists(update.getMessage().getFrom().getId())){
            //add user to db
            int id = update.getMessage().getFrom().getId();
            String username = "";
            if(update.getMessage().getFrom().getUserName() != null){
                username = update.getMessage().getFrom().getUserName();
            }
            String name = update.getMessage().getFrom().getFirstName() + update.getMessage().getFrom().getLastName();
            QuizDBLoader.addManagingUser(id, username, name);
        }


        if(update.getMessage().hasText() && update.getMessage().getText().charAt(0) == prefix){
            if(QuizDBLoader.quizStarted()){
                System.out.println("RunControl:quiz Started");
                switch(update.getMessage().getText()){
                    case "/controls":
                        resetController(update, bot);
                        break;
                }
            } else {
                System.out.println("RunControl:quiz Not Started");
                String[] args = update.getMessage().getText().split(" ");
                args[0] = args[0].toLowerCase();

                switch(args[0]){
                    case "/help":
                        help(update, bot);
                        break;
                    case "/addround": //to be run by that round's master
                        addRound(update, bot);
                        break;
                    case "/addquestion": //question // to be run by that round's master
                        addQuestion(update, bot, args);
                        break;
                    case "/removeround": //to be run by that round's master
                        removeRound(update, bot);
                        break;
                    case "/removequestion": //questionid
                        removeQuestion(update, bot, args);
                        break;
                    case "/showcurrent":
                        showCurrent(update, bot);
                        break;
                    case "/showmyround":
                        showMyRound(update, bot);
                        break;
                }
            }
        }
    }

    
    public static void runParticipant(Context context, Update update, AvalitechQuizSystem bot){
        String chatState = QuizDBLoader.getUserState(update.getMessage().getFrom().getId());
        String [] stateSplit = chatState.split(":");
        System.out.println("RunParticipant: " + stateSplit[0]);

        switch(stateSplit[0]){
            case "notstarted": //send a 'quiz has not started' message [deregister/twitchlink/t&cs]
                break;
            case "answering": //[roundid][questionid] //send a 'Round X question Y:' message ()along with overall round q's/answers so far) [edit previous/twitchlink]
                break;
            case "edit": //send a 'please select an answer to edit' message [button per question in round so far]
                break;
            case "editanswering": //[roundid][questionid] //send a 'please submit your new answer to X. You previously answered Y' [cancel]
                break;
            case "quizended": //"This quiz has ended, we hope you enjoyed yourself"
                break;
        }
    }

    
    public static void runUnregistered(Context context, Update update, AvalitechQuizSystem bot){
        System.out.println("RunUnregistered: ");
        //delete existing message
        if(!QuizDBLoader.quizStarted()){
            //send the galactic standard greeting
            final List<InlineKeyboardButton> keyboard = new ArrayList<InlineKeyboardButton>(3);

            InlineKeyboardButton but = new InlineKeyboardButton();
            but.setText("Register for Quiz");
            but.setCallbackData("usr:join");
            
            InlineKeyboardButton but2 = new InlineKeyboardButton();
            but2.setText("Join us on Twitch!");
            but2.setUrl("https://www.twitch.tv/irishfurries");

            InlineKeyboardButton but3 = new InlineKeyboardButton();
            but3.setText("Terms & Conditions");
            but3.setUrl("https://github.com/Skirmidev/Telegram-Quiz-Bot");

            keyboard.add(but);
            keyboard.add(but2);
            keyboard.add(but3);
            
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(Collections.singletonList(keyboard));
            
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());

            String date = QuizDBLoader.configValue("nextdate");

            String information = "Welcome to the Irishfurries Quiz Bot! The next quiz is scheduled for" + date + "\n"
            + "To join us, please register by clicking the button below. Don't forget to join us via twitch on the day!" + "\n"
            + "For mobile users, you can use the Twitch app's picture-in-picture mode to listen in while answering the quiz here in Telegram" + "\n"
            + "For desktop users, simply open the quiz in your web browser with Telegram open via web, the desktop client, or your mobile device" + "\n"
            + "Please read the terms and conditions for information on prizes and other important info!" + "\n"
            + "If you need any more assistance, contact a member of the IrishFurries mod team" + "\n"
            + "";



            message.setText(information);
            message.setReplyMarkup(inlineKeyboardMarkup);

            try{
                Message response = bot.execute(message);

                int id = update.getMessage().getFrom().getId();
                String username = "";
                if(update.getMessage().getFrom().getUserName() != null){
                    username = update.getMessage().getFrom().getUserName();
                }
                String name = update.getMessage().getFrom().getFirstName() + update.getMessage().getFrom().getLastName();

                //add user to db
                QuizDBLoader.addNewUser(id, username, response.getMessageId(), name);
            } catch ( TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            //TODO: this quiz has ended
            //bot will be offline, probably don't need to do anything
        }
    }

    //
    // CONTROL
    //
    public static void resetController(Update update, AvalitechQuizSystem bot){
        System.out.println("QuizManager: resetController");
        //delete existing controller
        if(QuizDBLoader.configValue("controllermessage").equals("")){
            //no existing controller
        } else {
            DeleteMessage delete = new DeleteMessage(update.getMessage().getChatId().toString(), Integer.parseInt(QuizDBLoader.configValue("controllermessage")));

            try{
                bot.execute(delete);
            } catch ( TelegramApiException e) {
                e.printStackTrace();
            }
        }

        int currentRound = Integer.parseInt(QuizDBLoader.configValue("currentround"));
        int currentQuestion = Integer.parseInt(QuizDBLoader.configValue("currentquestion"));
        boolean lastQuestionInRound = QuizDBLoader.isLastQuestionInRound(currentRound, currentQuestion);
        boolean lastRoundInQuiz = QuizDBLoader.isLastRoundInQuiz(currentRound);
        User currentRoundMaster = QuizDBLoader.roundMasterForRound(currentRound);
        User nextRoundMaster = QuizDBLoader.roundMasterForRound(currentRound+1);


        //spawn new controller
        final List<InlineKeyboardButton> keyboard = new ArrayList<InlineKeyboardButton>(3);

        InlineKeyboardButton but = new InlineKeyboardButton();

        String information = "Quiz Controller"+ "\n"
        + "Current Round: " + currentRound + " - " + currentRoundMaster
        + "Current Question: " + QuizDBLoader.configValue("currentquestion") + "\n";

        if(lastQuestionInRound){
            if(lastRoundInQuiz){
                but.setText("End Quiz");
                but.setCallbackData("runtime:endQuiz");
            } else {
                but.setText("Next Round");
                but.setCallbackData("runtime:nextRound:" + (currentRound+1));
                information = information + "Next Round: " + (currentRound+1) + " - " + nextRoundMaster;
            }
        } else {
            but.setText("Next Question");
            but.setCallbackData("runtime:nextQuestion:" + (currentQuestion+1));
        }

        keyboard.add(but);
        
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Collections.singletonList(keyboard));
        
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());

        message.setText(information);
        message.setReplyMarkup(inlineKeyboardMarkup);
        
        try{
            Message response = bot.execute(message);

            QuizDBLoader.updateConfigValue("controllermessage",response.getMessageId().toString());
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void help(Update update, AvalitechQuizSystem bot){
        System.out.println("QuizManager: help");
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());

        String txt = "";

        txt = "The following commands are available:" + "\n" + 
        "/help" + "\n" + 
        "/addRound - adds a round for you" + "\n" + 
        "/removeRound - removes your round" + "\n" + 
        "/addQuestion [question] - adds the question to your round" + "\n" + 
        "/removeQuestion [question] - removes a question from your round" + "\n" + 
        "/showCurrent - shows the entire quiz in it's current form" + "\n" + 
        "/showMyRound - shows your round and it's questions" + "\n" + 
        "For further help, just ask Skirmisher" + "\n" + 
        "";

        message.setText(txt);

        try{
            bot.execute(message);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    public static void addRound(Update update, AvalitechQuizSystem bot){
        //if this user doesn't already have a round, add one
        System.out.println("QuizManager: addRound");

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        boolean success = QuizDBLoader.addRound(update.getMessage().getFrom().getId());
        String txt = "";
        if(success){
            txt = "Succesfully added a round for you";
        } else {
            txt = "Failed to add a round - do you already have one?";
        }
        message.setText(txt);
        message.setReplyToMessageId(update.getMessage().getMessageId());

        try{
            bot.execute(message);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    public static void addQuestion(Update update, AvalitechQuizSystem bot, String[] args){
        System.out.println("QuizManager: addQuestion");
        //check if user has a round
        //if true, add this question to that round

        String question = "";
        for(int i = 1; i < args.length; i++){
            question = question + args[i] + " ";
        }

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        int questionId = QuizDBLoader.addQuestion(update.getMessage().getFrom().getId(), question);

        String txt = "";
        if(questionId == 0){
            txt = "Failed to add question - do you have a round?";
        } else {
            txt = "Succesfully added question";
        }

        message.setText(txt);
        message.setReplyToMessageId(update.getMessage().getMessageId());

        try{
            bot.execute(message);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    public static void removeRound(Update update, AvalitechQuizSystem bot){
        System.out.println("QuizManager: removeRound");
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        String txt = QuizDBLoader.removeRound(update.getMessage().getFrom().getId());
        
        message.setText(txt);
        message.setReplyToMessageId(update.getMessage().getMessageId());

        try{
            bot.execute(message);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    public static void removeQuestion(Update update, AvalitechQuizSystem bot, String[] args){
        System.out.println("QuizManager: removeQuestion");
        if(args.length > 1){
            int questionId = Integer.parseInt(args[1]);

            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());
            String txt = QuizDBLoader.removeQuestion(update.getMessage().getFrom().getId(), questionId);
            
            message.setText(txt);
            message.setReplyToMessageId(update.getMessage().getMessageId());

            try{
                bot.execute(message);
            } catch ( TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("removeQuestion: INVALID INPUT");
        }
        
    }
    
    public static void showCurrent(Update update, AvalitechQuizSystem bot){
        System.out.println("QuizManager: showCurrent");
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        String txt = QuizDBLoader.getFullQuiz();
        
        message.setText(txt);
        message.setReplyToMessageId(update.getMessage().getMessageId());

        try{
            bot.execute(message);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    public static void showMyRound(Update update, AvalitechQuizSystem bot){
        System.out.println("QuizManager: showMyRound");
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        String txt = QuizDBLoader.getRoundQuestions(update.getMessage().getFrom().getId());
        
        message.setText(txt);
        message.setReplyToMessageId(update.getMessage().getMessageId());

        try{
            bot.execute(message);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
    }


    /////////////////
    // PARTICIPANT //
    /////////////////
    public static void notStarted(Update update, AvalitechQuizSystem bot){
        System.out.println("QuizManager: Participant: notStarted");
        //delete existing message

        DeleteMessage deleteControl = new DeleteMessage(update.getMessage().getChatId().toString(), QuizDBLoader.getParticipantMessage(update.getMessage().getFrom().getId()));
        DeleteMessage deleteUser = new DeleteMessage(update.getMessage().getChatId().toString(), update.getMessage().getMessageId());

        try{
            bot.execute(deleteControl);
            bot.execute(deleteUser);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }



        //send a 'quiz has not started' message [deregister/twitchlink/t&cs]
        final List<InlineKeyboardButton> keyboard = new ArrayList<InlineKeyboardButton>(3);

        InlineKeyboardButton but = new InlineKeyboardButton();
        but.setText("Leave Quiz");
        but.setCallbackData("usr:leave");
        
        InlineKeyboardButton but2 = new InlineKeyboardButton();
        but2.setText("Join us on Twitch!");
        but2.setUrl("https://www.twitch.tv/irishfurries");

        InlineKeyboardButton but3 = new InlineKeyboardButton();
        but3.setText("Terms & Conditions");
        but3.setUrl("https://github.com/Skirmidev/Telegram-Quiz-Bot");

        keyboard.add(but);
        keyboard.add(but2);
        keyboard.add(but3);
        
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Collections.singletonList(keyboard));
        
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());

        String date = QuizDBLoader.configValue("nextdate");

        String information = "Welcome to the Irishfurries Quiz Bot! The next quiz is scheduled for" + date + "\n"
        + "The quiz has not yet begun, you'll receive a message when it starts! Don't forget to join us via twitch on the day!" + "\n"
        + "For mobile users, you can use the Twitch app's picture-in-picture mode to listen in while answering the quiz here in Telegram" + "\n"
        + "For desktop users, simply open the quiz in your web browser with Telegram open via web, the desktop client, or your mobile device" + "\n"
        + "Please read the terms and conditions for information on prizes and other important info!" + "\n"
        + "If you need any more assistance, contact a member of the IrishFurries mod team" + "\n"
        + "";

        message.setText(information);
        message.setReplyMarkup(inlineKeyboardMarkup);

        try{
            Message response = bot.execute(message);

            QuizDBLoader.updateActiveMessage(update.getMessage().getFrom().getId(), response.getMessageId());
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    public static void answering(Update update, AvalitechQuizSystem bot){
        System.out.println("QuizManager: Participant: answering");
        //delete existing message

        DeleteMessage deleteControl = new DeleteMessage(update.getMessage().getChatId().toString(), QuizDBLoader.getParticipantMessage(update.getMessage().getFrom().getId()));
        DeleteMessage deleteUser = new DeleteMessage(update.getMessage().getChatId().toString(), update.getMessage().getMessageId());

        try{
            bot.execute(deleteControl);
            bot.execute(deleteUser);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }


        final List<InlineKeyboardButton> keyboard = new ArrayList<InlineKeyboardButton>(3);
        String answer = update.getMessage().getText();
        int round = Integer.parseInt(QuizDBLoader.configValue("currentround"));
        int question = Integer.parseInt(QuizDBLoader.configValue("currentquestion"));

        QuizDBLoader.addAnswer(round, question, update.getMessage().getFrom().getId(), answer);

        String answersSoFar = QuizDBLoader.getRoundAnswers(round, update.getMessage().getFrom().getId());

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText(answersSoFar);

        InlineKeyboardButton but = new InlineKeyboardButton();
        but.setText("Edit Previous Answer");
        but.setCallbackData("usr:edit");
        
        InlineKeyboardButton but2 = new InlineKeyboardButton();
        but2.setText("Join us on Twitch!");
        but2.setUrl("https://www.twitch.tv/irishfurries");

        InlineKeyboardButton but3 = new InlineKeyboardButton();
        but3.setText("Terms & Conditions");
        but3.setUrl("https://github.com/Skirmidev/Telegram-Quiz-Bot");

        keyboard.add(but);
        keyboard.add(but2);
        keyboard.add(but3);
        
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Collections.singletonList(keyboard));
        message.setReplyMarkup(inlineKeyboardMarkup);


        try{
            Message response = bot.execute(message);

            QuizDBLoader.updateActiveMessage(update.getMessage().getFrom().getId(), response.getMessageId());
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void Awaiting(Update update, AvalitechQuizSystem bot){
        System.out.println("QuizManager: Participant: awaiting");
        //delete existing message
        DeleteMessage deleteUser = new DeleteMessage(update.getMessage().getChatId().toString(), update.getMessage().getMessageId());

        try{
            bot.execute(deleteUser);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
        //do nothing but delete messages - user should await next question or edit a previous answer
    }

    public static void editSelect(Update update, AvalitechQuizSystem bot){
        System.out.println("QuizManager: Participant: editSelect");
        //delete existing message
        DeleteMessage deleteUser = new DeleteMessage(update.getMessage().getChatId().toString(), update.getMessage().getMessageId());

        try{
            bot.execute(deleteUser);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
        //do nothing but delete messages - user must press a callback button
    }
    
    public static void edit(Update update, AvalitechQuizSystem bot, String [] args){
        System.out.println("QuizManager: Participant: edit");
        //delete existing message

        DeleteMessage deleteControl = new DeleteMessage(update.getMessage().getChatId().toString(), QuizDBLoader.getParticipantMessage(update.getMessage().getFrom().getId()));
        DeleteMessage deleteUser = new DeleteMessage(update.getMessage().getChatId().toString(), update.getMessage().getMessageId());

        try{
            bot.execute(deleteControl);
            bot.execute(deleteUser);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
        final List<InlineKeyboardButton> keyboard = new ArrayList<InlineKeyboardButton>(3);
        //send a 'please select an answer to edit' message [button per question in round so far]
        String answer = update.getMessage().getText();
        int round = Integer.parseInt(args[1]);
        int question = Integer.parseInt(args[2]);

        QuizDBLoader.updateAnswer(round, question, update.getMessage().getFrom().getId(), answer);

        String answersSoFar = QuizDBLoader.getRoundAnswers(round, update.getMessage().getFrom().getId());

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText(answersSoFar);

        InlineKeyboardButton but = new InlineKeyboardButton();
        but.setText("Edit Previous Answer");
        but.setCallbackData("usr:edit");
        
        InlineKeyboardButton but2 = new InlineKeyboardButton();
        but2.setText("Join us on Twitch!");
        but2.setUrl("https://www.twitch.tv/irishfurries");

        InlineKeyboardButton but3 = new InlineKeyboardButton();
        but3.setText("Terms & Conditions");
        but3.setUrl("https://github.com/Skirmidev/Telegram-Quiz-Bot");

        keyboard.add(but);
        keyboard.add(but2);
        keyboard.add(but3);
        
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Collections.singletonList(keyboard));
        message.setReplyMarkup(inlineKeyboardMarkup);

        try{
            Message response = bot.execute(message);

            QuizDBLoader.updateActiveMessage(update.getMessage().getFrom().getId(), response.getMessageId());
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    public static void quizEnded(Update update, AvalitechQuizSystem bot){
        System.out.println("QuizManager: Participant: quizEnded");
        //delete existing message
        DeleteMessage deleteUser = new DeleteMessage(update.getMessage().getChatId().toString(), update.getMessage().getMessageId());

        try{
            bot.execute(deleteUser);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
        //"This quiz has ended, we hope you enjoyed yourself"
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("This quiz has ended, we hope you enjoyed yourself");

        try{
            Message response = bot.execute(message);

            QuizDBLoader.updateActiveMessage(update.getMessage().getFrom().getId(), response.getMessageId());
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
