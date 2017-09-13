package fr.radiofrance.alarmdemo;

import android.content.Context;
import android.media.AudioManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import fr.radiofrance.alarm.activity.AlarmLaunchActivity;
import fr.radiofrance.alarm.manager.RfAlarmManager;
import fr.radiofrance.alarm.util.DeviceVolumeUtils;
import fr.radiofrance.alarmdemo.model.DemoAlarm;
import fr.radiofrance.alarmdemo.player.DemoPlayer;

public class DemoAlarmActivity extends AlarmLaunchActivity<DemoAlarm> {

    private static final String LOG_TAG = DemoAlarmActivity.class.getSimpleName();

    @Override
    protected RfAlarmManager<DemoAlarm> getInstanceOfAlarmManager(final Context context) {
        return new RfAlarmManager<>(context, DemoAlarm.class);
    }

    @Override
    protected void onAlarmShouldStart(final DemoAlarm alarm, final boolean networkAvailable) {
        Log.d(LOG_TAG, "onAlarmShouldStart: " + getIntent().getStringExtra("EXTRA_TEST"));

        if (!networkAvailable) {
            // If no network, let super class play default sound
            super.onAlarmShouldStart(alarm, networkAvailable);
            return;
        }
        if (!BuildConfig.DEBUG) {
            DeviceVolumeUtils.setDeviceVolume(getApplicationContext(), AudioManager.STREAM_MUSIC, DeviceVolumeUtils.getValidVolume(getApplicationContext(), AudioManager.STREAM_MUSIC, alarm));
        }
        DemoPlayer.getInstance(getApplicationContext()).play();
    }

    @Override
    protected void onAlarmShouldStop(final DemoAlarm alarm) {
        DemoPlayer.getInstance(getApplicationContext()).stop();
        super.onAlarmShouldStop(alarm);
    }

    @Override
    protected int getThemeColor(final DemoAlarm alarm) {
        return ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
    }

    @Override
    protected void onActionDone(final DemoAlarm alarm, final TypeAction typeAction, final boolean succeed, final View actionView) {
        // Force succeed true
        // This is a demo app trick to see animation event when action failed (no alarmId when using "TEST SCREEN" option)
        super.onActionDone(alarm, typeAction, true, actionView);
        if (typeAction == TypeAction.Continue) {
            final ImageView actionDoneImageView = findViewById(R.id.alarm_action_done_imageview);
            actionDoneImageView.setImageResource(R.drawable.alarm_continue_done_logo);
        }
    }
}
