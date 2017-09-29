package fr.radiofrance.alarmdemo.application;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import fr.radiofrance.alarm.datastore.recovery.AlarmRecoveryModule;
import fr.radiofrance.alarm.manager.RfAlarmManager;
import fr.radiofrance.alarm.model.Alarm;
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
                .setConfigurationAlarmShowEditLaunchIntent(showEditLaunchIntent)
                .setRecoveryModule(new AlarmRecoveryModule() {
                    @Override
                    public Alarm onUpdateNeeded(final Alarm alarm, final String source) {
                        alarm.setIntent(new Intent(getApplicationContext(), DemoAlarmActivity.class));
                        return alarm;
                    }
                });
    }

}
