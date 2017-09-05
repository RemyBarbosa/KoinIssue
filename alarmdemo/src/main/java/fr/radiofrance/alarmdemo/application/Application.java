package fr.radiofrance.alarmdemo.application;

import android.content.Intent;

import fr.radiofrance.alarm.manager.RfAlarmManager;
import fr.radiofrance.alarmdemo.DemoAlarmActivity;
import fr.radiofrance.alarmdemo.MainActivity;
import fr.radiofrance.alarmdemo.model.DemoAlarm;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final RfAlarmManager<DemoAlarm> alarmManager = new RfAlarmManager<>(getApplicationContext(), DemoAlarm.class);
        alarmManager.setConfigurationAlarmDefaultLaunchIntent(new Intent(getApplicationContext(), DemoAlarmActivity.class));
        alarmManager.setConfigurationAlarmAppLaunchIntent(new Intent(getApplicationContext(), MainActivity.class));
    }

}
