package com.skirmisher.quizbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.ArrayList;
import java.util.List;

import com.skirmisher.data.*;

public class AvalitechQuizSystem extends TelegramLongPollingBot {
    boolean debug = false;

    Long groupId = 0l;
    Long modChatId = 0l;
    List<Long> admins = new ArrayList<>();

    //issue is not caused by wrong values present. :/
    // public ObibitalWeaponsPlatform(DefaultBotOptions options) {
    //     super(options);
    // }

    @Override
    public void onUpdateReceived(final Update update) {
        Context context = new Context();

        //
        // if(group){
        //     if(message){
        //         /controls
        //             deletes existing controls widget
        //             shows current master, question, next question, remaining questions in round etc.
        //             shows button for [next question] (callback only works for roundmaster of that question)
        //     } else if (callback){
        //         nextquestion 
        //     }
        // } else { //user 
        //     if(participant){
        //         strsplit[chatstate]
        //         switch(chatstate[1]){
        //             case notstarted
        //                 ->quiz has not started, quiz will begin at time. [deregister/twitchlink/t&cs]
        //             case answer
        //                 input will assign answer, respond with 'you answered X to question Y'
        //             case edit
        //                 show list of editables (questions, answers, buttons to select what to edit)
        //             case editanswer
        //                 input will assign answer to round [2] question [3], then return to answer state for latest question
        //             case quizended
        //                 the quiz has ended, we hope you enjoyed it!
        //         }
        //     } else { //not registered
        //         ->send welcome message
        //     }
        // }
        //ALL USER CALLBACKS should delete the message associated with the callback, keeps things clean
        //ALL USER MESSAGES should be deleted, keeps things clean
        //IF ROUND ADVANCES while in edit mode, need to delete the editmode message. question advances are a user problem (go back and edit)
        //

        if(update.hasCallbackQuery()){
            String callback = update.getCallbackQuery().getData();
            String [] callSplit = callback.split(":");
            if(callSplit[0].equals("usr") || callSplit[0].equals("u")) {
                // Group //
                CallbackManager.manageUserCallback(context, update, this, callSplit);
            } else if (callSplit[0].equals("setup") || callSplit[0].equals("s")) {
                // Admin //
                CallbackManager.manageSetupCallback(context, update, this, callSplit);
            } else if (callSplit[0].equals("runtime") || callSplit[0].equals("r")) {
                // Admin //
                CallbackManager.manageRuntimeCallback(context, update, this, callSplit);
            }
        } else if (update.hasMessage()) {
            if(update.getMessage().getChatId().equals(groupId)) {
                //message in the group, process as normal
                QuizManager.runControl(context, update, this);
            } else if (QuizDBLoader.hasParticipant(update.getMessage().getChatId())){
                //message from a participant, process appropriately
                QuizManager.runParticipant(context, update, this);
            } else {
                //message from an unregistered user, delete any existing message, and otherwise add them to users and send a new welcome message
                QuizManager.runUnregistered(context, update, this);
            }

        } else {
            /////////////////
            // Unsupported //
            /////////////////
            System.out.println("unrecognised update");
        }
    }

    @Override
    public String getBotUsername() {
        return QuizDBLoader.configValue("botusername");
    }


    @Override
    public String getBotToken() {
        return QuizDBLoader.configValue("bottoken");
    }
    
    public Long getModChatId() {
        return modChatId;
    }
    
    public Long getGroupChatId() {
        return groupId;
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

    public void reloadConfig(){
        groupId = Long.parseLong(QuizDBLoader.configValue("groupId"));
        modChatId = Long.parseLong(DBLoader.configValue("modChatId"));
        //admins=DBLoader.getAdmins();
    }
}

