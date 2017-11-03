package fr.radiofrance.alarmdemo;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import fr.radiofrance.alarm.activity.AlarmLaunchActivity;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.util.DeviceVolumeUtils;
import fr.radiofrance.alarmdemo.player.DemoPlayer;

public class DemoAlarmActivity extends AlarmLaunchActivity {

    private static final String LOG_TAG = DemoAlarmActivity.class.getSimpleName();

    private CheckWakeUpHandler checkWakeUpHandler;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkWakeUpHandler = new CheckWakeUpHandler(this);
    }

    @Override
    protected void onDestroy() {
        if (checkWakeUpHandler != null) {
            checkWakeUpHandler.cancel();
        }
        super.onDestroy();
    }

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
        if (alarm != null) {
            DemoPlayer.getInstance(getApplicationContext()).play(alarm.getCustomValue());
        } else {
            DemoPlayer.getInstance(getApplicationContext()).play("http://direct.fipradio.fr/live/fip-lofi.mp3");
        }
        checkWakeUpHandler.check();
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

    private void onAlarmWakeUpIsOk() {
        super.onNotDefaultAlarmWakeUpIsOk();
    }

    private static class CheckWakeUpHandler extends Handler {

        private static final long CHECK_WAKE_UP_RETRY_DELAY_MS = 500L;
        private static final int MESSAGE_WHAT = 0;

        private final WeakReference<DemoAlarmActivity> refActivity;

        CheckWakeUpHandler(final DemoAlarmActivity activity) {
            this.refActivity = new WeakReference<>(activity);
        }

        void check() {
            sendMessage(obtainMessage(MESSAGE_WHAT));
        }

        void cancel() {
            removeCallbacksAndMessages(null);
        }

        @Override
        public void handleMessage(final Message msg) {
            final DemoAlarmActivity activity = refActivity.get();
            if (activity == null) {
                return;
            }
            if (DemoPlayer.getInstance(activity.getApplicationContext()).isPlaying()) {
                activity.onAlarmWakeUpIsOk();
                return;
            }
            sendMessageDelayed(obtainMessage(MESSAGE_WHAT), CHECK_WAKE_UP_RETRY_DELAY_MS);
        }
    }

}
