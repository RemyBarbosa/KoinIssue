package fr.radiofrance.alarmdemo;

import android.media.AudioManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import fr.radiofrance.alarm.activity.AlarmLaunchActivity;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.util.DeviceVolumeUtils;
import fr.radiofrance.alarmdemo.player.DemoPlayer;

public class DemoAlarmActivity extends AlarmLaunchActivity {

    private static final String LOG_TAG = DemoAlarmActivity.class.getSimpleName();

    @Override
    protected void onAlarmShouldStart(final Alarm alarm, final boolean networkAvailable) {
        Log.d(LOG_TAG, "onAlarmShouldStart: " + getIntent().getStringExtra("EXTRA_TEST"));

        if (!networkAvailable) {
            // If no network, let super class play default sound
            super.onAlarmShouldStart(alarm, networkAvailable);
            return;
        }
        if (!BuildConfig.DEBUG) {
            DeviceVolumeUtils.setDeviceVolume(getApplicationContext(), AudioManager.STREAM_MUSIC, DeviceVolumeUtils.getValidVolume(getApplicationContext(), AudioManager.STREAM_MUSIC, alarm));
        }
        DemoPlayer.getInstance(getApplicationContext()).play(alarm.getCustomValue());
    }

    @Override
    protected void onAlarmShouldStop(final Alarm alarm) {
        DemoPlayer.getInstance(getApplicationContext()).stop();
        super.onAlarmShouldStop(alarm);
    }

    @Override
    protected int getThemeColor(final Alarm alarm) {
        return ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
    }

    @Override
    protected void onActionDone(final Alarm alarm, final TypeAction typeAction, final boolean succeed, final View actionView) {
        // Force succeed true
        // This is a demo app trick to see animation event when action failed (no alarmId when using "TEST SCREEN" option)
        super.onActionDone(alarm, typeAction, true, actionView);
        if (typeAction == TypeAction.Continue) {
            final ImageView actionDoneImageView = findViewById(R.id.alarm_action_done_imageview);
            actionDoneImageView.setImageResource(R.drawable.alarm_continue_done_logo);
        }
    }
}
