package com.skirmisher.quizbot;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import com.skirmisher.data.QuizDBLoader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import java.util.ArrayList;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class CallbackManager {

    static Pattern p_userId = Pattern.compile("id: [0-9]+");
    static Matcher m_userId;


    public static Context manageUserCallback(Context context, Update update, AvalitechQuizSystem bot, String[] callback){
        System.out.println("CallbackManager:: manageUserCallback:: " + callback[1]);
        switch (callback[1]) {
            case "join":
                //feedback(context, update, bot, callback);
                join(context, update, bot);
                break;
            
            case "leave":
                //peacefur(context, update, bot);
                leave(context, update, bot);
                break;

            case "edit": //roundid questionid, remember to include a cancel button
                //peacefur(context, update, bot);
                edit(context, update, bot, callback);
                break;
            
            case "selectEdit": //roundid questionid
                //peacefur(context, update, bot);
                selectEdit(context, update, bot);
                break;
        }
        return context;
    }

    
    public static Context manageSetupCallback(Context context, Update update, AvalitechQuizSystem bot, String[] callback){
        System.out.println("CallbackManager:: manageSetupCallback:: " + callback[1]);
        // switch (callback[1]) {
        //     case "nextQuestion":
        //         //feedback(context, update, bot, callback);
        //         break;
            
        //     case "startQuiz":
        //         //peacefur(context, update, bot);
        //         break;

        //     case "nextRound": //round quizmaster
        //         //peacefur(context, update, bot);
        //         break;

        //     case "yesStart":
        //         break;

        //     case "notYet":
        //         break;
        // }
        return context;
    }

    public static Context manageRuntimeCallback(Context context, Update update, AvalitechQuizSystem bot, String[] callback){
        System.out.println("CallbackManager:: manageRuntimeCallback:: " + callback[1]);
        switch (callback[1]) {
            case "nextQuestion":
                //feedback(context, update, bot, callback);
                nextQuestion(context, update, bot);
                break;
            
            case "nextRound":
                //feedback(context, update, bot, callback);
                nextRound(context, update, bot, callback);
                break; 
            
            case "endQuiz":
                //feedback(context, update, bot, callback);
                endQuiz(context, update, bot);
                break;
        }
        return context;
    }

    //
    //
    //
    public static void join(Context context, Update update, AvalitechQuizSystem bot){
        int userId = update.getCallbackQuery().getFrom().getId();
        //add to participants DB
        QuizDBLoader.addParticipant(userId,"notStarted");
        //delete their existing message, replace with one indicating they're in the quiz
        DeleteMessage deleteControl = new DeleteMessage(userId+"", update.getCallbackQuery().getMessage().getMessageId());

        try{
            bot.execute(deleteControl);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }

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
        message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());

        String date = QuizDBLoader.configValue("nextdate");

        String information = "Welcome to the Irishfurries Quiz Bot! The next quiz is scheduled for" + date + "\n\n"
        + "The quiz has not yet begun, you'll receive a message when it starts! Don't forget to join us via twitch on the day!" + "\n\n"
        + "For mobile users, you can use the Twitch app's picture-in-picture mode to listen in while answering the quiz here in Telegram" + "\n\n"
        + "For desktop users, simply open the quiz in your web browser with Telegram open via web, the desktop client, or your mobile device" + "\n\n"
        + "Please read the terms and conditions for information on prizes and other important info!" + "\n\n"
        + "If you need any more assistance, contact a member of the IrishFurries mod team" + "\n\n"
        + "";

        message.setText(information);
        message.setReplyMarkup(inlineKeyboardMarkup);

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(update.getCallbackQuery().getId());
        answer.setShowAlert(true);
        answer.setText("You have succesfully registered for the quiz!");

        try{
            bot.execute(answer);
            Message response = bot.execute(message);
            System.out.println("We have sent a new message - " + response);
            System.out.println("We will now attempt to update the active message");
            QuizDBLoader.updateActiveMessage(update.getCallbackQuery().getFrom().getId(), response.getMessageId());
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void leave(Context context, Update update, AvalitechQuizSystem bot){
        int userId = update.getCallbackQuery().getFrom().getId();
        //remove from participantsDB
        QuizDBLoader.removeParticipant(userId);
        //delete their existing message, replace with original one
        DeleteMessage deleteControl = new DeleteMessage(userId+"", update.getCallbackQuery().getMessage().getMessageId());

        try{
            bot.execute(deleteControl);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }

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
        message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());

        String date = QuizDBLoader.configValue("nextdate");

        String information = "Welcome to the Irishfurries Quiz Bot! The next quiz is scheduled for" + date + "\n\n"
        + "To join us, please register by clicking the button below. Don't forget to join us via twitch on the day!" + "\n\n"
        + "For mobile users, you can use the Twitch app's picture-in-picture mode to listen in while answering the quiz here in Telegram" + "\n\n"
        + "For desktop users, simply open the quiz in your web browser with Telegram open via web, the desktop client, or your mobile device" + "\n\n"
        + "Please read the terms and conditions for information on prizes and other important info!" + "\n\n"
        + "If you need any more assistance, contact a member of the IrishFurries mod team" + "\n\n"
        + "";



        message.setText(information);
        message.setReplyMarkup(inlineKeyboardMarkup);

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(update.getCallbackQuery().getId());
        answer.setShowAlert(true);
        answer.setText("You are no longer registered for the quiz");

        try{
            bot.execute(answer);
            Message response = bot.execute(message);

            QuizDBLoader.updateActiveMessage(userId, response.getMessageId());
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void edit(Context context, Update update, AvalitechQuizSystem bot, String[] callback){
        //delete their existing control message
        int userId = update.getCallbackQuery().getFrom().getId();
        DeleteMessage deleteControl = new DeleteMessage(userId+"", update.getCallbackQuery().getMessage().getMessageId());

        try{
            bot.execute(deleteControl);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }

        final List<InlineKeyboardButton> keyboard = new ArrayList<InlineKeyboardButton>(1);
        InlineKeyboardButton but = new InlineKeyboardButton();
        but.setText("Cancel Editing");
        but.setCallbackData("usr:canceledit");
        keyboard.add(but);

        int currentRound = Integer.parseInt(QuizDBLoader.configValue("currentround"));
        String questionData = QuizDBLoader.getQuestionData(currentRound, Integer.parseInt(callback[2]));

        SendMessage message = new SendMessage();
        message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        String txt = "Please input your new answer for the question: " + questionData + "\n" + 
        "You previously answered with: " + QuizDBLoader.getAnswer(userId, currentRound, Integer.parseInt(callback[2]));
        message.setText(txt);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Collections.singletonList(keyboard));
        message.setReplyMarkup(inlineKeyboardMarkup);
        

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(update.getCallbackQuery().getId());
        answer.setShowAlert(false);

        try{
            bot.execute(answer);
            Message response = bot.execute(message);

            QuizDBLoader.updateActiveMessage(userId, response.getMessageId());
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
        
        //send a new one indicating they can now input their new answer
        //should include the question and original answer
        //one control button, canceledit, will do the same as the one in selectEdit
        //set their state to edit

        QuizDBLoader.setParticipantState(userId, "edit");
    }

    public static void selectEdit(Context context, Update update, AvalitechQuizSystem bot){
        //delete their existing message
        //delete the control message
        int currentRound = Integer.parseInt(QuizDBLoader.configValue("currentround"));
        int userId = update.getCallbackQuery().getFrom().getId();

        
        HashMap<String, String> editables = QuizDBLoader.getEditables(currentRound, userId);

        if(editables.size() > 0){
            DeleteMessage deleteControl = new DeleteMessage(userId+"", QuizDBLoader.getParticipantMessage(userId));

            try{
                bot.execute(deleteControl);
            } catch ( TelegramApiException e) {
                e.printStackTrace();
            }
            //replace with one showing all the questions asked this round and asking which they'd like to edit
            final List<InlineKeyboardButton> keyboard = new ArrayList<InlineKeyboardButton>(editables.size()+1);

            String response = "";

            for(Map.Entry<String, String> editab : editables.entrySet()){

            }
            for(int i = 0; i < editables.size(); i++){
                int j = i+1;
                response = response + "Question " + j + ": " + editables.get(i) + "\n";
                response = response + "Your Answer: " + editables.get(i) + "\n"; // TODO: a map should work as a datatype here

                InlineKeyboardButton but = new InlineKeyboardButton();
                but.setText(j+"");
                but.setCallbackData("usr:edit:" + j);
                keyboard.add(but);
            }
            response = response + "Please select which question you would like to edit your answer for: ";
            //should be a button for each editable, callback should send round and question
            //be sure to include a cancel button

            InlineKeyboardButton but = new InlineKeyboardButton();
            but.setText("Cancel Editing");
            but.setCallbackData("usr:canceledit");

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(Collections.singletonList(keyboard));

            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());
            message.setText(response);
            message.setReplyMarkup(inlineKeyboardMarkup);
            

            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(update.getCallbackQuery().getId());
            answer.setShowAlert(false);
            answer.setText("Please select which answer you would like to replace");

            try{
                bot.execute(answer);
                Message responseMes = bot.execute(message);
    
                QuizDBLoader.updateActiveMessage(userId, responseMes.getMessageId());
            } catch ( TelegramApiException e) {
                e.printStackTrace();
            }

            QuizDBLoader.setParticipantState(userId, "editSelect");
            //set their state to editSelect
        } else {
            //send a callbackqueryresponse saying there's answers this round, somehow
        }
    }

    public static void cancelEdit(Context context, Update update, AvalitechQuizSystem bot){
        //delete the control message
        int currentRound = Integer.parseInt(QuizDBLoader.configValue("currentround"));
        int userId = update.getCallbackQuery().getFrom().getId();
        DeleteMessage deleteControl = new DeleteMessage(userId+"", QuizDBLoader.getParticipantMessage(userId));
        
        try{
            bot.execute(deleteControl);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }
        
        //replace with one showing all the questions asked this round and their answers
        final List<InlineKeyboardButton> keyboard = new ArrayList<InlineKeyboardButton>(3);
        SendMessage message = new SendMessage();
        message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        String answersSoFar = QuizDBLoader.getRoundAnswers(currentRound, userId);
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

        

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(update.getCallbackQuery().getId());
        answer.setShowAlert(false);
        answer.setText("Editing cancelled");

        try{
            bot.execute(answer);
            Message response = bot.execute(message);

            QuizDBLoader.updateActiveMessage(userId, response.getMessageId());
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }


        //set their state to awaiting
        QuizDBLoader.setParticipantState(userId, "awaiting");
    }

    //
    //
    //
    public static void nextQuestion(Context context, Update update, AvalitechQuizSystem bot){
        //check if sender is current round controller
        //update the current question
        //go through list of all participants, for foreach
            //if awaiting, delete existing message, send new message with latest question

        //delete controller message, send new one with updated values

        //check if sender is current round controller
        int currentRound = Integer.parseInt(QuizDBLoader.configValue("currentround"));
        int currentQuestion = Integer.parseInt(QuizDBLoader.configValue("currentquestion"));
        int nextQuestion = currentQuestion+1;
        int expectedNextRoundMaster = QuizDBLoader.getMasterByRound(currentRound);

        System.out.println("Expected next round: " + expectedNextRoundMaster + " message sent by " + update.getCallbackQuery().getFrom().getId());
        if(update.getCallbackQuery().getFrom().getId() == expectedNextRoundMaster){
            //update the current question
            QuizDBLoader.updateConfigValue("currentquestion", ""+nextQuestion);
            String nextQuestionData = QuizDBLoader.getQuestionData(currentRound, nextQuestion);

            //go through list of all participants, for foreach
            ArrayList<User> users = QuizDBLoader.getAllParticipants();
            for(User u : users){
                String state = QuizDBLoader.getParticipantState(u.getId());
                //later we could use this info to not pull awy from anyone editing their answers. for now, let em suffer the mild inconvenience.

                QuizDBLoader.setParticipantState(u.getId(), "answering");

                System.out.println("Gonna try delete message for: " + u.getId() + " - " + u.getFirstName());

                int participantMessage = QuizDBLoader.getParticipantMessage(u.getId());
                if(participantMessage != 0){
                    DeleteMessage deleteControl = new DeleteMessage(u.getId().toString(), participantMessage);
                    try{
                        bot.execute(deleteControl);
                    } catch ( TelegramApiException e) {
                        e.printStackTrace();
                    }
                }

                final List<InlineKeyboardButton> keyboard = new ArrayList<InlineKeyboardButton>(2);
                SendMessage message = new SendMessage();
                message.setChatId(u.getId().toString());
                String answersSoFar = QuizDBLoader.getRoundAnswers(currentRound, u.getId());
                message.setText(answersSoFar + "\n" + " Question " + nextQuestion + ": " + "\n" + nextQuestionData + "\n" + "Please submit your answer now...");
                
                InlineKeyboardButton but2 = new InlineKeyboardButton();
                but2.setText("Join us on Twitch!");
                but2.setUrl("https://www.twitch.tv/irishfurries");

                InlineKeyboardButton but3 = new InlineKeyboardButton();
                but3.setText("Terms & Conditions");
                but3.setUrl("https://github.com/Skirmidev/Telegram-Quiz-Bot");

                keyboard.add(but2);
                keyboard.add(but3);

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.setKeyboard(Collections.singletonList(keyboard));

                message.setReplyMarkup(inlineKeyboardMarkup);

                

                try{
                    Message response = bot.execute(message);

                    QuizDBLoader.updateActiveMessage(u.getId(), response.getMessageId());
                } catch ( TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(update.getCallbackQuery().getId());
            answer.setShowAlert(false);
            answer.setText("Updated to next question");

            QuizManager.resetController(bot);

            try{
                bot.execute(answer);
            } catch (TelegramApiException e){
                e.printStackTrace();
            }
        } else {
            
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(update.getCallbackQuery().getId());
            answer.setShowAlert(true);
            answer.setText("Only the next question master should press this button");
            try{
                bot.execute(answer);
            } catch (TelegramApiException e){
                e.printStackTrace();
            }
        }
        //if not the expected clicker, do nothing
    }

    public static void nextRound(Context context, Update update, AvalitechQuizSystem bot, String[] callback){
        //check if sender is next round controller
        int expectedNextRoundMaster = QuizDBLoader.getMasterByRound(Integer.parseInt(callback[2]));
        if(update.getCallbackQuery().getFrom().getId() == expectedNextRoundMaster){
            //update the current question
            QuizDBLoader.updateConfigValue("currentquestion", "1");
            //update the current round
            QuizDBLoader.updateConfigValue("currentround", callback[2]);
            //go through list of all participants, for foreach
            ArrayList<User> users = QuizDBLoader.getAllParticipants();
            for(User u : users){
                QuizDBLoader.setParticipantState(u.getId(), "answering");

                //delete the existing message
                DeleteMessage deleteControl = new DeleteMessage(u.getId().toString(), QuizDBLoader.getParticipantMessage(u.getId()));
        
                try{
                    bot.execute(deleteControl);
                } catch ( TelegramApiException e) {
                    e.printStackTrace();
                }

                final List<InlineKeyboardButton> keyboard = new ArrayList<InlineKeyboardButton>(2);
                SendMessage message = new SendMessage();
                message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
                message.setText("Round " + callback[2] + " has begun! The first question is: " + "\n" + QuizDBLoader.getQuestionData(Integer.parseInt(callback[2]), 1) + "\n" + "Please submit your answer now...");
                
                InlineKeyboardButton but2 = new InlineKeyboardButton();
                but2.setText("Join us on Twitch!");
                but2.setUrl("https://www.twitch.tv/irishfurries");

                InlineKeyboardButton but3 = new InlineKeyboardButton();
                but3.setText("Terms & Conditions");
                but3.setUrl("https://github.com/Skirmidev/Telegram-Quiz-Bot");

                keyboard.add(but2);
                keyboard.add(but3);

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.setKeyboard(Collections.singletonList(keyboard));

                message.setReplyMarkup(inlineKeyboardMarkup);

                try{
                    Message response = bot.execute(message);

                    QuizDBLoader.updateActiveMessage(u.getId(), response.getMessageId());
                } catch ( TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(update.getCallbackQuery().getId());
            answer.setShowAlert(false);
            answer.setText("Updated to next round");
            
            QuizManager.resetController(bot);

            try{
                bot.execute(answer);
            } catch (TelegramApiException e){
                e.printStackTrace();
            }
    
            //delete controller message, send new one with updated values
        } else {
            
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(update.getCallbackQuery().getId());
            answer.setShowAlert(true);
            answer.setText("Only the next question master should press this button");
            try{
                bot.execute(answer);
            } catch (TelegramApiException e){
                e.printStackTrace();
            }
        }
        //if not the expected clicker, do nothing
    }

    public static void endQuiz(Context context, Update update, AvalitechQuizSystem bot){
        //set all participants to 'quizended' state
        ArrayList<User> users = QuizDBLoader.getAllParticipants();
        for(User u : users){
            QuizDBLoader.setParticipantState(u.getId(), "quizended");

            //delete the message
            DeleteMessage deleteControl = new DeleteMessage(u.getId().toString(), QuizDBLoader.getParticipantMessage(u.getId()));
    
            try{
                bot.execute(deleteControl);
            } catch ( TelegramApiException e) {
                e.printStackTrace();
            }

            SendMessage message = new SendMessage();
            message.setChatId(u.getId().toString());

            message.setText("The quiz has ended, thank you for your participation");
            
            try{
                Message response = bot.execute(message);

                QuizDBLoader.updateConfigValue("controllermessage",response.getMessageId().toString());
            } catch ( TelegramApiException e) {
                e.printStackTrace();
            }
            //
        }

        //Delete the controller in the group chat to prevent spam
        DeleteMessage deleteControl = new DeleteMessage(update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId());
        try{
            bot.execute(deleteControl);
        } catch ( TelegramApiException e) {
            e.printStackTrace();
        }

        
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(update.getCallbackQuery().getId());
        answer.setShowAlert(false);
        answer.setText("Quiz has ended");

        try{
            bot.execute(answer);
        } catch (TelegramApiException e){
            e.printStackTrace();
        }

    }

    // public static void yesStart(Context context, Update update, AvalitechQuizSystem bot, String[] callback){

    // }

    // public static void notYet(Context context, Update update, AvalitechQuizSystem bot, String[] callback){

    // }

    // //
    // //
    // //
    // public static void join(Context context, Update update, AvalitechQuizSystem bot, String[] callback){

    // }
}