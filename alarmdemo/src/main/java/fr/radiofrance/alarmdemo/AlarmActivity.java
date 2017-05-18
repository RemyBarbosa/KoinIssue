package fr.radiofrance.alarmdemo;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import fr.radiofrance.alarm.manager.AlarmManager;
import fr.radiofrance.alarmdemo.model.AlarmModel;

public class AlarmActivity extends AppCompatActivity {

    private Button stopButton;
    private Button snoozeButton;
    private MediaPlayer player;
    private AlarmModel alarm;
    private MediaPlayer alarmMediaPlayer;

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

        setContentView(R.layout.activity_alarm);
        findViews();
        initViews();

        if (isNetworkAvailable(this)) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        player = new MediaPlayer();
                        player.setDataSource(AlarmActivity.this, Uri.parse("http://direct.fipradio.fr/live/fip-lofi.mp3"));
                        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        player.prepare();
                        player.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();
        } else if (alarmMediaPlayer == null) {
            alarmMediaPlayer = AlarmManager.playDefaultAlarmSound(this, alarm != null ? alarm.getVolume() : AlarmManager.getDeviceMaxVolume(this), true);
        }
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }

        AlarmManager.stopDefaultAlarmSound(this, alarmMediaPlayer);
        alarmMediaPlayer = null;

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Block back like the Android Alarms App
    }

    private void findViews() {
        stopButton = (Button) findViewById(R.id.stop);
        snoozeButton = (Button) findViewById(R.id.snooze);
    }

    private void initViews() {
        stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (player != null) {
                    player.stop();
                    player.release();
                    player = null;
                }
                alarm.setActivated(false);
                if (!AlarmManager.updateAlarm(AlarmActivity.this, alarm)) {
                    Toast.makeText(AlarmActivity.this, "Error when updating the alarm", Toast.LENGTH_SHORT).show();
                }
                finish();
            }

        });

        if (alarm == null) {
            snoozeButton.setVisibility(View.GONE);
        } else {
            snoozeButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!AlarmManager.snoozeAlarm(AlarmActivity.this, alarm.getId())) {
                        Toast.makeText(AlarmActivity.this, "Error when snoozing the alarm", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }

            });
        }
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

        final NetworkInfo[] info = connectivity.getAllNetworkInfo();
        if (info == null) {
            return false;
        }

        for (final NetworkInfo anInfo : info) {
            if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }

        return false;
    }

}
