package fr.radiofrance.alarmdemo.application;

import android.content.Intent;
import android.net.Uri;

import fr.radiofrance.alarm.manager.RfAlarmManager;
import fr.radiofrance.alarmdemo.DemoAlarmActivity;
import fr.radiofrance.alarmdemo.MainActivity;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final Intent showEditLaunchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("alarmdemo://screen.alarm.edit"));

        RfAlarmManager.with(getApplicationContext())
                .setConfigurationAlarmDefaultLaunchIntent(new Intent(getApplicationContext(), DemoAlarmActivity.class))
                .setConfigurationAlarmAppLaunchIntent(new Intent(getApplicationContext(), MainActivity.class))
                .setConfigurationAlarmShowEditLaunchIntent(showEditLaunchIntent);
    }

}
