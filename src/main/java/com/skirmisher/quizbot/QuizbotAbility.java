package com.skirmisher.quizbot;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

public class QuizbotAbility extends AbilityBot {
    public QuizbotAbility(){
        super(botToken(), botUsername());
    }

    //715180082 my creator id
    @Override
    public int creatorId() {
        return 715180082;
    }

    public Ability sayHelloWorld() {
        return Ability
                .builder()
                .name("hello")
                .info("says hello world!")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> silent.send("Your chatID is: " + ctx.chatId(), ctx.chatId()))
                .build();
    }


    public static String botUsername() {
        return "IrishFurries_Tablequiz_bot";
    }

    public static String botToken() {
        String token = "";
        try {

            File tokenFile = new File("apitoken.key");
            Scanner tokenReader = new Scanner(tokenFile);
            token = tokenReader.nextLine();
            tokenReader.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        System.out.println("Running bot with token: " + token);
        return token;
    }
}