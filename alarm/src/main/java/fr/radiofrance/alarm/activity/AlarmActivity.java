package fr.radiofrance.alarm.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import fr.radiofrance.alarm.R;
import fr.radiofrance.alarm.manager.AlarmManager;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.util.WeakRefOnClickListener;

public abstract class AlarmActivity extends AppCompatActivity {

    private static final long FINISH_DELAYED_TIME_MS = 2000L;
    private static final long REVEALED_TRANSITION_DURATION_MS = 300L;

    protected enum TypeAction {
        Stop, Snooze, Continue
    }

    private Alarm alarm;
    private MediaPlayer defaultRingMediaPlayer;
    private TimeTickBroadcastReceiver timeTickBroadcastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Showing on lock screen
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        final Intent intent = getIntent();
        if (intent != null) {
            final Bundle extras = intent.getExtras();
            if (extras != null) {
                final String alarmId = extras.getString(AlarmManager.INTENT_ALARM_ID);
                alarm = AlarmManager.getAlarm(this, alarmId);
            }
        }

        setContentView(getLayoutRes());

        final View stopView = findViewById(R.id.alarm_stop_action_view);
        if (stopView != null) {
            stopView.setOnClickListener(new WeakRefOnClickListener<AlarmActivity>(this) {
                @Override
                public void onClick(final AlarmActivity reference, final View view) {
                    final boolean succeed = reference.onActionStop();
                    reference.onActionDone(TypeAction.Stop, succeed, view);
                }
            });
        }
        final View snoozeView = findViewById(R.id.alarm_snooze_action_view);
        if (snoozeView != null) {
            snoozeView.setOnClickListener(new WeakRefOnClickListener<AlarmActivity>(this) {
                @Override
                public void onClick(final AlarmActivity reference, final View view) {
                    final boolean succeed = reference.onActionSnooze();
                    reference.onActionDone(TypeAction.Snooze, succeed, view);
                }
            });
        }
        final View continueView = findViewById(R.id.alarm_continue_action_view);
        if (continueView != null) {
            continueView.setOnClickListener(new WeakRefOnClickListener<AlarmActivity>(this) {
                @Override
                public void onClick(final AlarmActivity reference, final View view) {
                    final boolean succeed = reference.onActionContinue();
                    reference.onActionDone(TypeAction.Continue, succeed, view);
                }
            });
        }

        onAlarmShouldStart(alarm, isNetworkAvailable(this));


        timeTickBroadcastReceiver = new TimeTickBroadcastReceiver(this);
        registerReceiver(timeTickBroadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        bindCurrentTime();
    }

    @Override
    public void onBackPressed() {
        // Block back like the Android Alarms App
    }

    @Override
    protected void onDestroy() {
        onAlarmShouldStop(alarm);
        unregisterReceiver(timeTickBroadcastReceiver);
        super.onDestroy();
    }

    @LayoutRes
    protected int getLayoutRes() {
        return R.layout.activity_alarm;
    }

    protected final boolean onActionStop() {
        onAlarmShouldStop(alarm);
        if (alarm == null) {
            return true;
        }
        alarm.setActivated(false);
        return AlarmManager.updateAlarm(AlarmActivity.this, alarm);
    }

    protected final boolean onActionSnooze() {
        if (alarm == null) {
            // Its a security, just do nothing on snooze if we are not able to reprogram a new alarm
            return false;
        }
        onAlarmShouldStop(alarm);
        return AlarmManager.snoozeAlarm(AlarmActivity.this, alarm.getId());
    }

    protected final boolean onActionContinue() {
        if (defaultRingMediaPlayer != null) {
            // Security check to let default ring go on
            AlarmManager.stopDefaultAlarmSound(this, defaultRingMediaPlayer);
            defaultRingMediaPlayer = null;
        }
        return true;
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

    protected void onActionDone(final TypeAction typeAction, final boolean succeed, final View actionView) {
        if (!succeed) {
            Toast.makeText(AlarmActivity.this, R.string.alarm_screen_error_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        final View revealedLayout = findViewById(R.id.alarm_revealed_layout);
        final TextView revealedTextView = findViewById(R.id.alarm_revealed_textview);
        switch (typeAction) {
            case Snooze:
                revealedTextView.setText(R.string.alarm_screen_snooze_revealed_label);
                break;
            case Stop:
                revealedTextView.setText(R.string.alarm_screen_stop_revealed_label);
                break;
            case Continue:
                revealedTextView.setText(R.string.alarm_screen_continue_revealed_label);
                break;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            revealedLayout.setVisibility(View.VISIBLE);
            finishWithDelay();
            return;
        }

        revealWithAnimation(revealedLayout, actionView);
        finishWithDelay();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void revealWithAnimation(final View revealedLayout, final View actionView) {
        final View revealView = findViewById(R.id.alarm_reveal_view);
        final int x = actionView.getRight();
        final int y = actionView.getBottom();

        final int startRadius = 0;
        final int endRadius = (int) Math.hypot(revealView.getWidth(), revealView.getHeight());

        final Animator anim = ViewAnimationUtils.createCircularReveal(revealView, x, y, startRadius, endRadius);
        revealView.setVisibility(View.VISIBLE);

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                revealedLayout.setVisibility(View.VISIBLE);
                revealView.animate().setDuration(REVEALED_TRANSITION_DURATION_MS).alpha(0F);
            }
        });

        anim.start();
    }

    private void finishWithDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                AlarmActivity.this.finish();
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

    private static class TimeTickBroadcastReceiver extends BroadcastReceiver {

        private final WeakReference<AlarmActivity> refActivity;

        TimeTickBroadcastReceiver(final AlarmActivity activity) {
            this.refActivity = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final AlarmActivity activity = refActivity.get();
            if (activity == null) {
                return;
            }
            activity.bindCurrentTime();
        }
    }

}
