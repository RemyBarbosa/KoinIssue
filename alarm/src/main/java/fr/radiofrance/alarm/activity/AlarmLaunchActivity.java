package fr.radiofrance.alarm.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import fr.radiofrance.alarm.R;
import fr.radiofrance.alarm.exception.RfAlarmAlreadyExecutedException;
import fr.radiofrance.alarm.manager.RfAlarmManager;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.util.AlarmIntentUtils;
import fr.radiofrance.alarm.util.DeviceVolumeUtils;
import fr.radiofrance.alarm.util.NetworkUtils;
import fr.radiofrance.alarm.util.WeakRefOnClickListener;

public abstract class AlarmLaunchActivity extends AppCompatActivity {

    private static final int CHECK_NETWORK_RETRY_COUNT = 6;
    private static final long CHECK_NETWORK_RETRY_DELAY_MS = 500L;
    private static final long FINISH_DELAYED_TIME_MS = 2500L;
    private static final long REVEALED_TRANSITION_DURATION_MS = 600L;
    private static final long REVEALED_TRANSITION_FADE_OUT_MS = 300L;

    protected enum TypeAction {
        Stop, Snooze, Continue
    }

    private RfAlarmManager alarmManager;

    private Alarm alarm;
    private DefaultRingMediaPlayer defaultRingMediaPlayer;
    private TimeTickBroadcastReceiver timeTickBroadcastReceiver;
    private CheckNetworkHandler checkNetworkHandler;

    private View stopView;
    private View snoozeView;
    private View continueView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("AlarmScheduler", "AlarmLaunchActivity onCreate: " + getIntent().getExtras().getString(AlarmIntentUtils.LAUNCH_PENDING_INTENT_EXTRA_ALARM_ID));

        // Showing on lock screen
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Hide Navigation bar
            final View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }

        alarmManager = RfAlarmManager.with(getApplicationContext());
        checkNetworkHandler = new CheckNetworkHandler(this, CHECK_NETWORK_RETRY_COUNT, CHECK_NETWORK_RETRY_DELAY_MS);

        final Intent intent = getIntent();
        if (intent != null) {
            final Bundle extras = intent.getExtras();
            if (extras != null) {
                final String alarmId = extras.getString(AlarmIntentUtils.LAUNCH_PENDING_INTENT_EXTRA_ALARM_ID);
                alarm = alarmManager.getAlarm(alarmId);
                try {
                    final int alarmHash = extras.getInt(AlarmIntentUtils.LAUNCH_PENDING_INTENT_EXTRA_ALARM_HASH, -1);
                    alarmManager.onAlarmIsExecuted(alarmHash);
                } catch (RfAlarmAlreadyExecutedException e) {
                    // Activity can had been resume from background and already have execute this alarm
                    if (e.appLaunchIntent != null) {
                        startActivity(e.appLaunchIntent);
                    }
                    finish();
                    return;
                }
            }
        }

        try {
            alarmManager.onAlarmIsConsumed(alarm);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(getLayoutRes());

        stopView = findViewById(R.id.alarm_stop_action_view);
        if (stopView != null) {
            stopView.setOnClickListener(new WeakRefOnClickListener<AlarmLaunchActivity>(this) {
                @Override
                public void onClick(final AlarmLaunchActivity reference, final View view) {
                    boolean succeed = true;
                    try {
                        reference.onActionStop();
                    } catch (Exception e) {
                        e.printStackTrace();
                        succeed = false;
                    }
                    reference.onActionDone(alarm, TypeAction.Stop, succeed, view);
                }
            });
        }
        snoozeView = findViewById(R.id.alarm_snooze_action_view);
        if (snoozeView != null) {
            snoozeView.setOnClickListener(new WeakRefOnClickListener<AlarmLaunchActivity>(this) {
                @Override
                public void onClick(final AlarmLaunchActivity reference, final View view) {
                    boolean succeed = true;
                    try {
                        reference.onActionSnooze();
                    } catch (Exception e) {
                        e.printStackTrace();
                        succeed = false;
                    }
                    reference.onActionDone(alarm, TypeAction.Snooze, succeed, view);
                }
            });
        }
        continueView = findViewById(R.id.alarm_continue_action_view);
        if (continueView != null) {
            continueView.setOnClickListener(new WeakRefOnClickListener<AlarmLaunchActivity>(this) {
                @Override
                public void onClick(final AlarmLaunchActivity reference, final View view) {
                    boolean succeed = true;
                    try {
                        reference.onActionContinue();
                    } catch (Exception e) {
                        e.printStackTrace();
                        succeed = false;
                    }
                    reference.onActionDone(alarm, TypeAction.Continue, succeed, view);
                }
            });
        }

        timeTickBroadcastReceiver = new TimeTickBroadcastReceiver(this);
        registerReceiver(timeTickBroadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        bindCurrentTime();

        checkNetworkHandler.check();
    }

    @Override
    public void onBackPressed() {
        // Block back like the Android Alarms App
    }

    @Override
    protected void onDestroy() {
        stopDefaultRingAlarm();
        if (checkNetworkHandler != null) {
            checkNetworkHandler.cancel();
        }
        if (timeTickBroadcastReceiver != null) {
            unregisterReceiver(timeTickBroadcastReceiver);
        }
        super.onDestroy();
    }

    @LayoutRes
    protected int getLayoutRes() {
        return R.layout.activity_alarm;
    }

    @ColorInt
    protected int getThemeColor(final Alarm alarm) {
        return ContextCompat.getColor(getApplicationContext(), R.color.alarm_theme_color);
    }

    protected final void onActionStop() throws Exception {
        onAlarmShouldStop(alarm);
    }

    protected final void onActionSnooze() throws Exception {
        onAlarmShouldStop(alarm);
        alarmManager.onAlarmIsSnoozed(alarm);
    }

    protected final void onActionContinue() throws Exception {
        stopDefaultRingAlarm();
    }

    // Keep attributes for subClass override
    protected void onAlarmShouldStart(final Alarm alarm, final boolean networkAvailable) {
        startDefaultRingAlarm();
    }

    // Keep attributes for subClass override
    protected void onAlarmShouldStop(final Alarm alarm) {
        stopDefaultRingAlarm();
    }

    protected void onActionDone(final Alarm alarm, final TypeAction typeAction, final boolean succeed, final View actionView) {
        if (!succeed) {
            Toast.makeText(getApplicationContext(), R.string.alarm_screen_error_toast, Toast.LENGTH_SHORT).show();
            finishWithDelay();
            return;
        }

        if (stopView != null) {
            stopView.setOnClickListener(null);
        }
        if (snoozeView != null) {
            snoozeView.setOnClickListener(null);
        }
        if (continueView != null) {
            continueView.setOnClickListener(null);
        }

        final View actionDoneLayout = findViewById(R.id.alarm_action_done_layout);

        final TextView actionDoneTextView = findViewById(R.id.alarm_action_done_textview);
        final ImageView actionDoneImageView = findViewById(R.id.alarm_action_done_imageview);

        int revealColor = getThemeColor(alarm);
        int backgroundEndColor = revealColor;

        switch (typeAction) {
            case Snooze:
                final int minutes = (alarm != null && alarm.getSnoozeDuration() > DateUtils.MINUTE_IN_MILLIS) ? (int) (alarm.getSnoozeDuration() / DateUtils.MINUTE_IN_MILLIS) : 0;
                if (minutes == 0) {
                    actionDoneTextView.setText(R.string.alarm_screen_snooze_done_less_label);
                } else {
                    actionDoneTextView.setText(getResources().getString(R.string.alarm_screen_snooze_done_label, minutes));
                }
                actionDoneImageView.setImageResource(R.drawable.ic_alarm_action_snooze_48dp);
                break;
            case Stop:
                actionDoneTextView.setText(R.string.alarm_screen_stop_done_label);
                actionDoneImageView.setImageResource(R.drawable.ic_alarm_action_stop_48dp);
                break;
            case Continue:
                actionDoneTextView.setText(R.string.alarm_screen_continue_done_label);
                revealColor = ContextCompat.getColor(getApplicationContext(), R.color.alarm_continue_reveal);
                backgroundEndColor = ContextCompat.getColor(getApplicationContext(), R.color.alarm_black);
                break;
        }

        actionDoneLayout.setBackgroundColor(revealColor);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            actionDoneLayout.setVisibility(View.VISIBLE);
            finishWithDelay();
            return;
        }

        revealWithAnimation(actionDoneLayout, actionView, revealColor, backgroundEndColor);
        finishWithDelay();
    }

    private void onNetworkChecked(@NonNull final NetworkInfo.State networkState) {
        onAlarmShouldStart(alarm, networkState == NetworkInfo.State.CONNECTED);
    }

    private void startDefaultRingAlarm() {
        if (defaultRingMediaPlayer != null) {
            return;
        }
        final int streamType = AudioManager.STREAM_ALARM;
        final int volume = alarm != null ? alarm.getVolume() : DeviceVolumeUtils.getDeviceMaxVolume(getApplicationContext(), AudioManager.STREAM_MUSIC);
        // Because is set with a default STREAM_MUSIC volume, we should convert it to STREAM_ALARM volume
        final int convertedVolume = DeviceVolumeUtils.convertDeviceVolume(getApplicationContext(), AudioManager.STREAM_MUSIC, streamType, volume);

        DeviceVolumeUtils.saveDeviceVolume(getApplicationContext(), streamType);
        DeviceVolumeUtils.setDeviceVolume(getApplicationContext(), streamType, convertedVolume);

        defaultRingMediaPlayer = new DefaultRingMediaPlayer(getApplicationContext(), streamType);
        defaultRingMediaPlayer.start();
    }

    private void stopDefaultRingAlarm() {
        if (defaultRingMediaPlayer == null) {
            return;
        }
        defaultRingMediaPlayer.stop();
        defaultRingMediaPlayer = null;

        DeviceVolumeUtils.restoreDeviceVolume(getApplicationContext(), AudioManager.STREAM_ALARM);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void revealWithAnimation(final View revealedLayout, final View actionView, @ColorInt final int revealColor, @ColorInt final int backgroundEndColor) {
        final View revealView = findViewById(R.id.alarm_reveal_view);
        revealView.setBackgroundColor(revealColor);

        final int x = actionView.getLeft() + actionView.getWidth() / 2;
        final int y = actionView.getTop() + actionView.getHeight() / 2;

        final int startRadius = 0;
        final int endRadius = (int) Math.hypot(revealView.getWidth(), revealView.getHeight());

        final Animator anim = ViewAnimationUtils.createCircularReveal(revealView, x, y, startRadius, endRadius)
                .setDuration(REVEALED_TRANSITION_DURATION_MS);
        revealView.setVisibility(View.VISIBLE);

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                revealedLayout.setVisibility(View.VISIBLE);
                revealView.animate().setDuration(REVEALED_TRANSITION_FADE_OUT_MS).alpha(0F);
                actionView.animate().setDuration(REVEALED_TRANSITION_FADE_OUT_MS).alpha(0F);
                revealedLayout.setBackgroundColor(backgroundEndColor);
            }
        });

        actionView.bringToFront();

        anim.start();
    }

    private void finishWithDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                AlarmLaunchActivity.this.finish();
            }
        }, FINISH_DELAYED_TIME_MS);
    }

    private void bindCurrentTime() {
        final TextView textView = findViewById(R.id.alarm_hour_label_textview);
        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        if (DateFormat.is24HourFormat(getApplicationContext())) {
            textView.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime()));
        } else {
            textView.setText(new SimpleDateFormat("hh:mm", Locale.getDefault()).format(calendar.getTime()));
        }
    }

    private static class TimeTickBroadcastReceiver extends BroadcastReceiver {

        private final WeakReference<AlarmLaunchActivity> refActivity;

        TimeTickBroadcastReceiver(final AlarmLaunchActivity activity) {
            this.refActivity = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final AlarmLaunchActivity activity = refActivity.get();
            if (activity == null) {
                return;
            }
            activity.bindCurrentTime();
        }
    }

    private static class CheckNetworkHandler extends Handler {

        private static final int MESSAGE_WHAT = 0;

        private final WeakReference<AlarmLaunchActivity> refActivity;
        private final int retryCount;
        private final long retryDelayMS;

        CheckNetworkHandler(final AlarmLaunchActivity activity, final int retryCount, final long retryDelayMS) {
            this.refActivity = new WeakReference<>(activity);
            this.retryCount = retryCount;
            this.retryDelayMS = retryDelayMS;
        }

        void check() {
            sendMessage(obtainMessage(MESSAGE_WHAT, retryCount, 0));
        }

        void cancel() {
            removeCallbacksAndMessages(null);
        }

        @Override
        public void handleMessage(final Message msg) {
            final AlarmLaunchActivity activity = refActivity.get();
            if (activity == null) {
                return;
            }
            final NetworkInfo.State networkState = NetworkUtils.getNetworkState(activity.getApplicationContext());
            switch (networkState) {
                case CONNECTED:
                    activity.onNetworkChecked(networkState);
                    return;
                default:
                    if (msg.arg1 == 0) {
                        activity.onNetworkChecked(networkState);
                        return;
                    }
                    sendMessageDelayed(obtainMessage(MESSAGE_WHAT, msg.arg1 - 1, 0), retryDelayMS);
            }
        }
    }

}
