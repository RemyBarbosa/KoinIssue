package fr.radiofrance.alarm.activity;

import android.content.Context;
import android.media.MediaPlayer;
import android.provider.Settings;

import java.io.IOException;

class DefaultRingMediaPlayer extends MediaPlayer {

    private final Context context;

    DefaultRingMediaPlayer(final Context context, final int streamType) {
        super();
        this.context = context;
        setAudioStreamType(streamType);
        setLooping(true);
    }

    public void start() {
        try {
            setDataSource(context, Settings.System.DEFAULT_ALARM_ALERT_URI);
            prepare();
            super.start();
        } catch (IOException e) {
            e.printStackTrace();
            release();
        }
    }

    public void stop() {
        super.stop();
        release();
    }
}
