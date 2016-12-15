package fr.radiofrance.alarmdemo.application;

import android.content.Intent;
import android.media.AudioManager;

import fr.radiofrance.alarm.manager.AlarmManager;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarmdemo.MainActivity;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AlarmManager.initialize(this, AudioManager.STREAM_MUSIC, new Intent(this, MainActivity.class), Alarm.class);
    }

}
