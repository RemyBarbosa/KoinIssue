package fr.radiofrance.alarmdemo;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import fr.radiofrance.alarm.activity.AlarmActivity;
import fr.radiofrance.alarm.model.Alarm;

public class DemoAlarmActivity extends AlarmActivity {

    private static final String LOG_TAG = DemoAlarmActivity.class.getSimpleName();

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
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Error on MediaPlayer start: ", e);
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

    @Override
    protected int getThemeColor(final Alarm alarm) {
        return ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
    }

    @Override
    protected void onActionDone(final Alarm alarm, final TypeAction typeAction, final boolean succeed, final View actionView) {
        super.onActionDone(alarm, typeAction, succeed, actionView);
        if (typeAction == TypeAction.Continue) {
            final ImageView actionDoneImageView = findViewById(R.id.alarm_action_done_imageview);
            actionDoneImageView.setImageResource(R.drawable.alarm_continue_done_logo);
        }
    }
}
