package fr.radiofrance.alarmdemo.player;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;


/**
 * Dummy player for demo only
 */
public class DemoPlayer {


    private static final int DEFAULT_PLAYER_CONNECT_TIMEOUT = 30000; // 30 seconds;
    private static final int DEFAULT_PLAYER_READ_TIMEOUT = 30000; // 30 seconds;

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
    private ExoPlayer exoPlayer;

    private DemoPlayer(final Context context) {
        this.context = context;
    }

    public void play(final String uriString) {
        createNewExoPlayer();

        Uri sourceUri = Uri.parse(uriString);
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory("RFPlayerDemo", bandwidthMeter, DEFAULT_PLAYER_CONNECT_TIMEOUT, DEFAULT_PLAYER_READ_TIMEOUT, false);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, bandwidthMeter, httpDataSourceFactory);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource = new ExtractorMediaSource(sourceUri, dataSourceFactory, extractorsFactory, null, null);

        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
    }

    public boolean isPlaying() {
        if (exoPlayer == null) {
            return false;
        }
        return exoPlayer.getPlaybackState() == ExoPlayer.STATE_READY && exoPlayer.getPlayWhenReady();
    }

    public void stop() {
        if (exoPlayer == null) {
            return;
        }
        exoPlayer.stop();
    }

    protected void createNewExoPlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
        }

        TrackSelector trackSelector = new DefaultTrackSelector(new FixedTrackSelection.Factory());
        LoadControl loadControl = new DefaultLoadControl();

        exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
    }
}
