# Telegram-Quiz-Bot
A telegram quiz bot built for the IrishFurries Telegram chat to host online table quiz events

## Configuration:
Please refer to the samples in `src/main/resources/Samples` provided.

 the config.csv must have the following data:  
 botusername - the username of your bot  
 bottoken - the token provided by godfatherbot
 
 other values should be self explanatory. hopefully.

*Configured files should be placed in `src/main/resources`*

### Additional Rounds
Create an appropriately numbered answer csv for the round answers
    
Add the questions to the quizDB.csv file  
update the roundDB to include the round number and assign it to a user via their chatID  

    
*Configured files should be placed in `src/main/resources`*

## Building the bot:
`mvn clean install` run in the root directory.

## Running the bot

java -jar ./target/telegram-quiz-bot-0.0.1.jar

### DISCLAIMER
    Lotta cleanup left to do before this is released to the public, this is some raw alpha stuff right here