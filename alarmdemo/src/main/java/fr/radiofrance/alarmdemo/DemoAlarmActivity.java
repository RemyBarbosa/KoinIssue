package fr.radiofrance.alarmdemo;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

import fr.radiofrance.alarm.activity.AlarmActivity;
import fr.radiofrance.alarm.model.Alarm;

public class DemoAlarmActivity extends AlarmActivity {

    private MediaPlayer player;

    @Override
    protected void onAlarmShouldStart(final Alarm alarm, final boolean networkAvailable) {
        if (!networkAvailable) {
            super.onAlarmShouldStart(alarm, networkAvailable);
            return;
        }
        if (player != null) {
            return;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    player = new MediaPlayer();
                    player.setDataSource(DemoAlarmActivity.this, Uri.parse("http://direct.fipradio.fr/live/fip-lofi.mp3"));
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    @Override
    protected void onAlarmShouldStop(final Alarm alarm) {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        super.onAlarmShouldStop(alarm);
    }

}
