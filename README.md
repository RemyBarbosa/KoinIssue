# ALARM CLOCK ANDROID

## Introduction

This Android lib contains core classes to include the Alarm Clock in an app

The source code is available in the /src directory

The root directory contains :

* gradle build files
* The default definition file of the project : AndroidManifest.xml

## Install

Use submodules git to integrate in your Project directory

`git submodule add git@gitlab.dev.dnm.radiofrance.fr:francebleu/bleuandroid.git alarm`


Then include the lib in the dependencies section of your project's gradle file `build.gradle` :

`compile project(':alarm')`
  

## Integrate in your project

### Basic integration

To launch the alarm screen :

            Intent intent = new Intent(this, AlarmActivity.class);
            intent.putExtra(AlarmActivity.DEFAULT_RADIO, getDefaultRadioId());
            intent.putExtra(AlarmActivity.DEFAULT_LOCALE, getDefaultLocaleIndex());
            startActivity(intent);

You can parameter different values in the SharedPreferences (use :androidtoolbox PrefsTools class)
To configure the time of wake up and the alarm activation

            key : com.radiofrance.radio.radiofrance.RadioFranceAlarmTime
            type : java.lang.String
            value : 19700101T175800 //timestamp


            key : com.radiofrance.radio.radiofrance.RadioFranceAlarmActivated
            type : java.lang.Boolean
            value : true
