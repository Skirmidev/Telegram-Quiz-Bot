#!/bin/bash

chown postgres:postgres -R /database

# CREATE DATABASE 
su -c "initdb -D /database/config/" postgres

# ADJUSTMENT TO WORK WITH DOCKER
sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '\*'/g" /database/config/postgresql.conf


# START DATABASE
su -c "postgres -D /database/config/ >/database/log.log 2>&1" postgres&

# WAIT UNTIL DB STARTS
RETRIES=5
until psql -U postgres -c "select 1" > /dev/null 2>&1 || [ $RETRIES -eq 0 ]; do   
    echo "Waiting for postgres server to start, $((RETRIES)) remaining attempts..."   RETRIES=$((RETRIES-=1))   
    sleep 1 
done

# CREATE DATABASE
su -c "createdb quizbot-db" postgres

# CREATE TABLESPACE
psql -U postgres -d quizbot-db -c"CREATE TABLESPACE quizbotTables LOCATION '/database/tables';"

# CHECK IF CONFIG EXISTS
configExists=$(psql -t -U postgres -d quizbot-db -c"SELECT * FROM information_schema.tables WHERE table_name='config';")


# CREATE TABLE: CONFIG
psql -U postgres -d quizbot-db -c"CREATE TABLE IF NOT EXISTS config ( element VARCHAR ( 50 ) PRIMARY KEY UNIQUE NOT NULL, value VARCHAR ( 500 ));"

# CREATE TABLE: USERS
psql -U postgres -d quizbot-db -c"CREATE TABLE IF NOT EXISTS users ( userid integer PRIMARY KEY UNIQUE NOT NULL, username VARCHAR ( 50 ), activemessage integer, name VARCHAR ( 100 ) NOT NULL);"

# CREATE TABLE: PARTICIPANTS
psql -U postgres -d quizbot-db -c"CREATE TABLE IF NOT EXISTS participants ( userid integer REFERENCES users (userid), chatstate VARCHAR ( 50 ), registrationdate TIMESTAMP default current_timestamp);"

# CREATE TABLE: ROUNDS
psql -U postgres -d quizbot-db -c"CREATE TABLE IF NOT EXISTS rounds ( round serial UNIQUE NOT NULL, roundmaster integer UNIQUE NOT NULL REFERENCES users (userid));"

# CREATE TABLE: QUESTIONS
psql -U postgres -d quizbot-db -c"CREATE TABLE IF NOT EXISTS questions ( round integer REFERENCES rounds (round), questionid integer NOT NULL, questiondata VARCHAR (400));"

# CREATE TABLE: ANSWERS
psql -U postgres -d quizbot-db -c"CREATE TABLE IF NOT EXISTS answers ( round integer REFERENCES rounds (round), questionid integer NOT NULL, userid integer REFERENCES users (userid), answer VARCHAR (400));"

# CREATE UNIQUE QUESTION INDEX
psql -U postgres -d quizbot-db -c"CREATE UNIQUE INDEX idx_question ON questions(round, questionid);"


#if config did not exist, populate config
if test -z "$configExists" 
then
    echo "Populating new config"

    while IFS=, read -r element value; do
        psql -U postgres -d quizbot-db -c"INSERT INTO config ( element, value ) VALUES ('$element', '$value');"
    done < configValues.csv

else
    echo "Config already exists"
fi

#run telegram modbot
java -jar /telegram-quizbot.jar
