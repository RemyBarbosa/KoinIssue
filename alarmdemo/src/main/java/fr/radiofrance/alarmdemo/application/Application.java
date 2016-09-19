package fr.radiofrance.alarmdemo.application;

import android.media.AudioManager;

import fr.radiofrance.alarm.manager.AlarmManager;

/**
 * Created by mondon on 13/09/16.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AlarmManager.initialize(this, AudioManager.STREAM_MUSIC);
    }

}
