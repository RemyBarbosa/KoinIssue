package fr.radiofrance.alarmdemo.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;


/**
 * Dummy player for demo only
 */
public class DemoPlayer {

    private static final String LOG_TAG = DemoPlayer.class.getSimpleName();

    private static DemoPlayer instance;

    public static DemoPlayer getInstance(final Context context) {
        synchronized (DemoPlayer.class) {
            if (instance == null) {
                instance = new DemoPlayer(context);
            }
        }
        return instance;
    }

    private final Context context;
    private MediaPlayer player;

    private DemoPlayer(final Context context) {
        this.context = context;
    }

    public void play(final String uriString) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (player != null) {
                        player.stop();
                        player.release();
                        player = null;
                    }
                    player = new MediaPlayer();
                    player.setDataSource(context, Uri.parse(uriString));
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.prepare();
                    player.start();
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Error on MediaPlayer start: ", e);
                }
            }

        }).start();
    }

    public void stop() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (player != null) {
                    player.stop();
                    player.release();
                    player = null;
                }
            }

        }).start();
    }
}
