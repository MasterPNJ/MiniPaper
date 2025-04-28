# MiniPaper
 A phone application for paper mini-games

## Features

Random Tap: tap 3 post-its as fast as you can in 15 seconds.

Shake It Up: shake the phone to earn points in 10 seconds.

Volume Master: maintain a target dB level for 1 sec.

Turn It: turn the phone at a random angle to score points in 10 seconds.

Flappy Paper: fly a paper aeroplane through obstacles.

Leaderboard: displays the best score for each player.

Statistics: read the best scores per mini-game.

## Requirements

Android Studio

Compile Sdk Version: 34 (API 34 ("UpsideDownCake"; Android 14.0))

Gradle Version: 8.5

Kotlin, Java 8

Active Firebase account to configure the Realtime Database

## Permissions

The application requests:

RECORD_AUDIO to capture the microphone volume (Volume Master)

WRITE_EXTERNAL_STORAGE (for temporary audio)

These permissions are declared in AndroidManifest.xml and requested at runtime.

## Installation and execution

Clone this repository:

git clone https://github.com/MasterPNJ/MiniPaper.git

cd MiniPaper

Open the project in Android Studio :File > Open and select the root folder.

Sync Gradle

Check that the google-services.json file is in app/.

Compile and run on an emulator or a physical USB device.

### Or download the apk release

 ## Preview model
 
![Mini-Paper (1)](https://github.com/user-attachments/assets/949e2ff8-107a-4e6f-8e42-2b92828dbe56)
