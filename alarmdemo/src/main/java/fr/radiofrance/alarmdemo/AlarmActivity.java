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
import android.widget.Button;

import java.io.IOException;

import fr.radiofrance.alarm.manager.AlarmManager;
import fr.radiofrance.alarm.model.Alarm;

/**
 * Created by mondon on 17/05/16.
 */
public class AlarmActivity extends AppCompatActivity {

    private Button stopButton;
    private Button snoozeButton;
    private MediaPlayer player;
    private Alarm alarm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String alarmId = extras.getString(AlarmManager.INTENT_ALARM_ID);
                alarm = AlarmManager.getInstance().getAlarm(alarmId);
            }
        }

        setContentView(R.layout.activity_alarm);
        findViews();
        initViews();

        if (isNetworkAvailable(this)) {
            try {
                player = new MediaPlayer();
                player.setDataSource(this, Uri.parse("http://audio.scdn.arkena.com/11016/fip-midfi128.mp3"));
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.prepare();
                player.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            AlarmManager.getInstance().playDefaultAlarmSound(alarm != null ? alarm.getVolume()
                    : AlarmManager.getInstance().getDeviceMaxVolume(), true);
        }
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }

        AlarmManager.getInstance().stopDefaultAlarmSound();

        super.onDestroy();
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
                finish();
            }

        });

        if (alarm == null) {
            snoozeButton.setVisibility(View.GONE);
        } else {
            snoozeButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    AlarmManager.getInstance().snoozeAlarm(alarm.getId());
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
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
