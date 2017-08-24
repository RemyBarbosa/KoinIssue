package fr.radiofrance.alarmdemo.application;

import android.content.Intent;
import android.media.AudioManager;

import fr.radiofrance.alarm.manager.AlarmManager;
import fr.radiofrance.alarmdemo.model.DemoAlarm;
import fr.radiofrance.alarmdemo.MainActivity;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AlarmManager.initialize(this, new Intent(this, MainActivity.class), AudioManager.STREAM_MUSIC, DemoAlarm.class);
    }

}
