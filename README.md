# TSE - Emotional Recognition

This repo contains code for a smart-watch app, which allows wearers to track their emotional status based on current health data.

The app consists of three main feature tracks.

## ML Based Emotional Classification

We are using the kotlin-smile machine learning package to perform classifications on the watch
itself. While there are serious limitations in the provided compute power of the device, we have
found that a relatively simple machine learning approach can be used to classify the target
reliably.

To see the documentations of kotlin smile, check out this documentation:
https://haifengl.github.io/api/kotlin/index.html

## Emotional Intervention Modules

Users are prompted to engage in Emotional Interventions when their emotional status is predicted to
be negative. Currently, the user can be encouraged to start one of the following interventions:
* Breathing Exercises
* Playing their preferred music playlist
* Calling a friends/family member

## Notifications and Complications

Whenever a negative emotion is detected, the user is notified through a pop-up notification, that
a negative emotional state has been detected and they are recommended to engage in the intervention

Additionally, a complication which can be set up on the watchface will mirror the current emotional
state of the wearer.

## Installation

To install this app, first set-up your Samsung Galaxy Watch in Debug Mode and connect it to your
Installation of Android Studio. Watch this tutorial to learn how to set up your watch correctly:
https://youtu.be/aG8_CV9zAjU

Alternatively, you can run the app on an emulated device using Android 14.0. Do note, that data
collection and subsequent model training and prediction are not set up for virtual devices.

Afterwards, sync and build the project file and mirror it to your device. The app should open
automatically and you will need to grant it the required permissions.

This project also contains an optional companion app, which you can install on your smartphone
connected to your watch as long as it is connected to your installation of Android Studio.
To run the companion app on a virtual device, follow these instructions:
https://developer.android.com/training/wearables/get-started/connect-phone