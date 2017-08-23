package fr.radiofrance.alarm.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import fr.radiofrance.alarm.R;
import fr.radiofrance.alarm.manager.AlarmManager;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.util.WeakRefOnClickListener;

public abstract class AlarmActivity extends AppCompatActivity {

    private MediaPlayer player;
    private Alarm alarm;
    private MediaPlayer defaultRingMediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Showing on lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        final Intent intent = getIntent();
        if (intent != null) {
            final Bundle extras = intent.getExtras();
            if (extras != null) {
                final String alarmId = extras.getString(AlarmManager.INTENT_ALARM_ID);
                alarm = AlarmManager.getAlarm(this, alarmId);
            }
        }

        setContentView(getLayoutRes());

        final View stopView = findViewById(R.id.alarm_stop_button);
        if (stopView != null) {
            stopView.setOnClickListener(new WeakRefOnClickListener<AlarmActivity>(this) {
                @Override
                public void onClick(final AlarmActivity reference, final View view) {
                    reference.onStopButtonClick();
                }
            });
        }
        final View snoozeView = findViewById(R.id.alarm_snooze_button);
        if (snoozeView != null) {
            snoozeView.setOnClickListener(new WeakRefOnClickListener<AlarmActivity>(this) {
                @Override
                public void onClick(final AlarmActivity reference, final View view) {
                    reference.onSnoozeButtonClick();
                }
            });
        }
        final View continueView = findViewById(R.id.alarm_continue_button);
        if (continueView != null) {
            continueView.setOnClickListener(new WeakRefOnClickListener<AlarmActivity>(this) {
                @Override
                public void onClick(final AlarmActivity reference, final View view) {
                    reference.onContinueButtonClick();
                }
            });
        }

        onAlarmShouldStart(alarm, isNetworkAvailable(this));
    }

    protected final void onStopButtonClick() {
        onAlarmShouldStop(alarm);
        if (alarm == null) {
            return;
        }
        alarm.setActivated(false);
        if (!AlarmManager.updateAlarm(AlarmActivity.this, alarm)) {
            onAlarmError();
        }
        finish();
    }

    protected final void onSnoozeButtonClick() {
        if (alarm == null) {
            // Its a security, just do nothing on snooze if we are not able to reprogram a new alarm
            return;
        }
        onAlarmShouldStop(alarm);
        if (!AlarmManager.snoozeAlarm(AlarmActivity.this, alarm.getId())) {
            onAlarmError();
        }
        finish();
    }

    protected final void onContinueButtonClick() {
        if (defaultRingMediaPlayer != null) {
            // Security check to let default ring go on
            AlarmManager.stopDefaultAlarmSound(this, defaultRingMediaPlayer);
            defaultRingMediaPlayer = null;
        }
        finish();
    }

    @LayoutRes
    protected int getLayoutRes() {
        return R.layout.activity_alarm;
    }

    protected void onAlarmShouldStart(final Alarm alarm, final boolean networkAvailable) {
        if (defaultRingMediaPlayer != null) {
            return;
        }
        defaultRingMediaPlayer = AlarmManager.playDefaultAlarmSound(this, alarm != null ? alarm.getVolume() : AlarmManager.getDeviceMaxVolume(this), true);
    }

    protected void onAlarmShouldStop(final Alarm alarm) {
        if (defaultRingMediaPlayer == null) {
            return;
        }
        AlarmManager.stopDefaultAlarmSound(this, defaultRingMediaPlayer);
        defaultRingMediaPlayer = null;
    }

    protected void onAlarmError() {
        Toast.makeText(AlarmActivity.this, "Error when trying to midify the alarm.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        onAlarmShouldStop(alarm);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Block back like the Android Alarms App
    }

    /**
     * Test internet connection
     *
     * @param context
     * @return TRUE if connection exists, else FALSE.
     */
    private boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }

        final NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        }
        return true;
    }

}
