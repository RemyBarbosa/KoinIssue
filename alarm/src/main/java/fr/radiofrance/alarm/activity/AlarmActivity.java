package fr.radiofrance.alarm.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import fr.radiofrance.alarm.R;
import fr.radiofrance.alarm.manager.AlarmManager;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.util.WeakRefOnClickListener;

public abstract class AlarmActivity extends AppCompatActivity {

    public enum TypeAction {
        Stop, Snooze, Continue
    }

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
                    reference.onActionDone(TypeAction.Stop, view);
                }
            });
        }
        final View snoozeView = findViewById(R.id.alarm_snooze_button);
        if (snoozeView != null) {
            snoozeView.setOnClickListener(new WeakRefOnClickListener<AlarmActivity>(this) {
                @Override
                public void onClick(final AlarmActivity reference, final View view) {
                    reference.onSnoozeButtonClick();
                    reference.onActionDone(TypeAction.Snooze, view);
                }
            });
        }
        final View continueView = findViewById(R.id.alarm_continue_button);
        if (continueView != null) {
            continueView.setOnClickListener(new WeakRefOnClickListener<AlarmActivity>(this) {
                @Override
                public void onClick(final AlarmActivity reference, final View view) {
                    reference.onContinueButtonClick();
                    reference.onActionDone(TypeAction.Continue, view);
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
    }

    protected final void onContinueButtonClick() {
        if (defaultRingMediaPlayer != null) {
            // Security check to let default ring go on
            AlarmManager.stopDefaultAlarmSound(this, defaultRingMediaPlayer);
            defaultRingMediaPlayer = null;
        }
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

    protected void onActionDone(final TypeAction typeAction, final View actionView) {
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
                revealView.animate().setDuration(300L).alpha(0F);
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
        }, 2000L);
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
