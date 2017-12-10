#!/bin/bash

echo Set up gradle version
./gradlew wrapper --gradle-version 3.3

echo Building a debug APK ...
./gradlew assembleDebug

echo Setting gcloud project ...
gcloud config set project mcc-fall-2017-g19

echo Deploying app ...
gcloud app deploy Backend/app.yaml

echo Deploying cron job ...
gcloud app deploy Backend/cron.yaml
