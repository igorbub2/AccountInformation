## About The Project

Simple Java scraper that retrieves accounts IBAN and balance.

Currently only MBank accounts are supported.

## Build

Project is build using Maven.

You need to run command from inside the project's folder:

```
mvn clean install
```

## Run

You need to have Java 21 installed

Run this command to start the application:

```
java -jar target/AccountInformation-1.0-SNAPSHOT.jar
```

For the purpose of testing you need to copy testLoginCredentials.sample and rename it to testLoginCredentials.properties. After that you need to fill the renamed file with real life credentials

## Usage

After running the app you need to log in using login and password.
Then you should confirm 2FA in bank's mobile app and press enter.
Your account's IBAN and balance should be printed in the console.