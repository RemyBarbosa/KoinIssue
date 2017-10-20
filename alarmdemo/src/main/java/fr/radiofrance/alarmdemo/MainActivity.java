package fr.radiofrance.alarmdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import fr.radiofrance.alarm.exception.RfAlarmException;
import fr.radiofrance.alarm.manager.RfAlarmManager;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarm.receiver.RfAlarmReceiver;
import fr.radiofrance.alarm.util.AlarmBatteryOptimizationUtils;
import fr.radiofrance.alarm.util.DeviceVolumeUtils;
import fr.radiofrance.alarmdemo.adapter.AlarmsAdapter;
import fr.radiofrance.alarmdemo.listener.OnAlarmActionListener;
import fr.radiofrance.alarmdemo.player.DemoPlayer;
import fr.radiofrance.alarmdemo.view.DividerItemDecoration;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private RefreshNextAlarmMessageHandler refreshNextAlarmMessageHandler;

    private static final int SNOOZE_DURATION_MS = (int) TimeUnit.MINUTES.toMillis(2); // 2 minutes
    private static final String PLAYER_URI_TEST = "http://direct.fipradio.fr/live/fip-lofi.mp3";

    private AlarmNeedUiRefreshBroadcastReceiver alarmNeedUiRefreshBroadcastReceiver;

    private TextView nextAlarmMessageTextView;
    private RecyclerView alarmsRecyclerView;
    private View addAlarmButton;
    private AlarmsAdapter alarmsAdapter;

    private RfAlarmManager alarmManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        refreshNextAlarmMessageHandler = new RefreshNextAlarmMessageHandler(this);

        setContentView(R.layout.activity_main);

        alarmManager = RfAlarmManager.with(getApplicationContext());
        alarmNeedUiRefreshBroadcastReceiver = new AlarmNeedUiRefreshBroadcastReceiver(this);

        findViews();
        initViews();

        checkDeeplink(getIntent());
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        checkDeeplink(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncAlarmList();
        updateNextAlarmMessage();
        refreshNextAlarmMessageHandler.start();
        registerReceiver(alarmNeedUiRefreshBroadcastReceiver, new IntentFilter(RfAlarmReceiver.ACTION_BROADCAST_RECEIVER_ON_ALARM_NEED_UI_REFRESH));
    }

    @Override
    protected void onPause() {
        refreshNextAlarmMessageHandler.stop();
        unregisterReceiver(alarmNeedUiRefreshBroadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clean_all:
                try {
                    alarmManager.removeAllAlarms();
                } catch (RfAlarmException e) {
                    Log.w(LOG_TAG, "Error when removing all alarm: ", e);
                    showErrorMessage(e.getMessage());
                    break;
                }
                syncAlarmList();
                updateNextAlarmMessage();
                break;
            case R.id.debug:
                final List<Alarm> alarms = alarmManager.getAllAlarms();

                String debug = "";
                for (final Alarm alarm : alarms) {
                    if (alarm != null) {
                        debug += alarm + "\n";
                    }
                }

                if (TextUtils.isEmpty(debug)) {
                    debug = getString(R.string.no_alarms);
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(debug);
                builder.show();
                break;
            case R.id.test_alarm_screen:
                startActivity(new Intent(getApplicationContext(), DemoAlarmActivity.class));
                break;
            case R.id.player_test:
                DemoPlayer.getInstance(getApplicationContext()).play(PLAYER_URI_TEST);
                break;
            case R.id.player_stop:
                DemoPlayer.getInstance(getApplicationContext()).stop();
                break;
            default:

        }

        return super.onOptionsItemSelected(item);
    }

    private void findViews() {
        nextAlarmMessageTextView = findViewById(R.id.next_alarm_message);
        alarmsRecyclerView = findViewById(R.id.alarms_recycler_view);
        addAlarmButton = findViewById(R.id.add_alarm_button);
    }

    private void initViews() {
        alarmsAdapter = new AlarmsAdapter(this, new ArrayList<Alarm>(), new OnAlarmActionListener() {

            @Override
            public void onAlarmClick(Alarm alarm, int position) {
                showUpdateAlarmDialog(alarm);
            }

            @Override
            public void onAlarmLongClick(Alarm alarm, int position) {
                try {
                    alarmManager.removeAlarm(alarm.getId());
                } catch (RfAlarmException e) {
                    Log.w(LOG_TAG, "Error when removing one alarm: ", e);
                    showErrorMessage(e.getMessage());
                    return;
                }
                syncAlarmList();
                updateNextAlarmMessage();
            }

            @Override
            public void onAlarmActivated(Alarm alarm, boolean isActivated, int position) {
                if (alarm == null) {
                    return;
                }
                alarm.setActivated(isActivated);
                try {
                    alarmManager.updateAlarm(alarm);
                } catch (RfAlarmException e) {
                    Log.w(LOG_TAG, "Error when updating active value of alarm: ", e);
                    showErrorMessage(e.getMessage());
                    return;
                }
                syncAlarmList();
                if (isActivated) {
                    AlarmBatteryOptimizationUtils.showBatteryOptimizationDialogIfNeeded(MainActivity.this, getString(R.string.app_name));
                }
                updateNextAlarmMessage();
            }

        });

        alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alarmsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        alarmsRecyclerView.setAdapter(alarmsAdapter);

        addAlarmButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showAddAlarmDialog();
            }

        });
    }

    private void syncAlarmList() {
        alarmsAdapter.setAlarms(alarmManager.getAllAlarms());
    }

    private void showAddAlarmDialog() {
        final View addAlarmDialogView = LayoutInflater.from(this).inflate(R.layout.activity_main_dialog, null);
        final CheckBox monday = addAlarmDialogView.findViewById(R.id.monday);
        final CheckBox tuesday = addAlarmDialogView.findViewById(R.id.tuesday);
        final CheckBox wednesday = addAlarmDialogView.findViewById(R.id.wednesday);
        final CheckBox thursday = addAlarmDialogView.findViewById(R.id.thursday);
        final CheckBox friday = addAlarmDialogView.findViewById(R.id.friday);
        final CheckBox saturday = addAlarmDialogView.findViewById(R.id.saturday);
        final CheckBox sunday = addAlarmDialogView.findViewById(R.id.sunday);
        final EditText hours = addAlarmDialogView.findViewById(R.id.hours);
        final EditText minutes = addAlarmDialogView.findViewById(R.id.minutes);
        final SeekBar volume = addAlarmDialogView.findViewById(R.id.volume);

        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);

        hours.setText(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)));
        minutes.setText(String.format("%02d", calendar.get(Calendar.MINUTE)));
        final int volumeMax = DeviceVolumeUtils.getDeviceMaxVolume(getApplicationContext(), AudioManager.STREAM_MUSIC);
        volume.setMax(volumeMax);
        volume.setProgress(volumeMax / 2);

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(addAlarmDialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final List<Integer> days = new ArrayList<>();
                if (monday.isChecked()) days.add(Calendar.MONDAY);
                if (tuesday.isChecked()) days.add(Calendar.TUESDAY);
                if (wednesday.isChecked()) days.add(Calendar.WEDNESDAY);
                if (thursday.isChecked()) days.add(Calendar.THURSDAY);
                if (friday.isChecked()) days.add(Calendar.FRIDAY);
                if (saturday.isChecked()) days.add(Calendar.SATURDAY);
                if (sunday.isChecked()) days.add(Calendar.SUNDAY);

                final int h = Integer.parseInt(hours.getText().toString());
                final int m = Integer.parseInt(minutes.getText().toString());

                final Alarm alarm = new Alarm();
                alarm.setDays(days);
                alarm.setHours(h);
                alarm.setMinutes(m);
                alarm.setSnoozeDuration(SNOOZE_DURATION_MS);
                alarm.setVolume(volume.getProgress());
                alarm.setActivated(true);
                alarm.setCustomValue(PLAYER_URI_TEST);

                final Intent intent = new Intent(getApplicationContext(), DemoAlarmActivity.class);
                intent.putExtra("EXTRA_TEST", "Extra test content");
                alarm.setIntent(intent);

                try {
                    alarmManager.addAlarm(alarm);
                } catch (RfAlarmException e) {
                    Log.w(LOG_TAG, "Error when adding alarm: ", e);
                    showErrorMessage(e.getMessage());
                    return;
                }
                syncAlarmList();
                updateNextAlarmMessage();
                AlarmBatteryOptimizationUtils.showBatteryOptimizationDialogIfNeeded(MainActivity.this, getString(R.string.app_name));
            }

        });
        builder.setNegativeButton("Cancel", null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    private void showUpdateAlarmDialog(@NonNull final Alarm alarm) {
        final View updateAlarmDialogView = LayoutInflater.from(this).inflate(R.layout.activity_main_dialog, null);
        final CheckBox monday = updateAlarmDialogView.findViewById(R.id.monday);
        final CheckBox tuesday = updateAlarmDialogView.findViewById(R.id.tuesday);
        final CheckBox wednesday = updateAlarmDialogView.findViewById(R.id.wednesday);
        final CheckBox thursday = updateAlarmDialogView.findViewById(R.id.thursday);
        final CheckBox friday = updateAlarmDialogView.findViewById(R.id.friday);
        final CheckBox saturday = updateAlarmDialogView.findViewById(R.id.saturday);
        final CheckBox sunday = updateAlarmDialogView.findViewById(R.id.sunday);
        final EditText hours = updateAlarmDialogView.findViewById(R.id.hours);
        final EditText minutes = updateAlarmDialogView.findViewById(R.id.minutes);
        final SeekBar volume = updateAlarmDialogView.findViewById(R.id.volume);

        monday.setChecked(alarm.getDays().contains(Calendar.MONDAY));
        tuesday.setChecked(alarm.getDays().contains(Calendar.TUESDAY));
        wednesday.setChecked(alarm.getDays().contains(Calendar.WEDNESDAY));
        thursday.setChecked(alarm.getDays().contains(Calendar.THURSDAY));
        friday.setChecked(alarm.getDays().contains(Calendar.FRIDAY));
        saturday.setChecked(alarm.getDays().contains(Calendar.SATURDAY));
        sunday.setChecked(alarm.getDays().contains(Calendar.SUNDAY));

        hours.setText(String.valueOf(alarm.getHours()));
        minutes.setText(String.valueOf(alarm.getMinutes()));
        final int volumeMax = DeviceVolumeUtils.getDeviceMaxVolume(getApplicationContext(), AudioManager.STREAM_MUSIC);
        volume.setMax(volumeMax);
        volume.setProgress(alarm.getVolume());

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(updateAlarmDialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final List<Integer> days = new ArrayList<>();
                if (monday.isChecked()) days.add(Calendar.MONDAY);
                if (tuesday.isChecked()) days.add(Calendar.TUESDAY);
                if (wednesday.isChecked()) days.add(Calendar.WEDNESDAY);
                if (thursday.isChecked()) days.add(Calendar.THURSDAY);
                if (friday.isChecked()) days.add(Calendar.FRIDAY);
                if (saturday.isChecked()) days.add(Calendar.SATURDAY);
                if (sunday.isChecked()) days.add(Calendar.SUNDAY);

                final int h = Integer.parseInt(hours.getText().toString());
                final int m = Integer.parseInt(minutes.getText().toString());

                alarm.setDays(days);
                alarm.setHours(h);
                alarm.setMinutes(m);
                alarm.setVolume(volume.getProgress());

                try {
                    alarmManager.updateAlarm(alarm);
                } catch (RfAlarmException e) {
                    Log.w(LOG_TAG, "Error when updating alarm: ", e);
                    showErrorMessage(e.getMessage());
                    return;
                }
                syncAlarmList();
                updateNextAlarmMessage();
                AlarmBatteryOptimizationUtils.showBatteryOptimizationDialogIfNeeded(MainActivity.this, getString(R.string.app_name));
            }

        });
        builder.setNegativeButton("Cancel", null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    private void updateNextAlarmMessage() {
        final Calendar nextAlarmDate = alarmManager.getNextAlarmScheduleDate();
        if (nextAlarmDate == null) {
            nextAlarmMessageTextView.setText(R.string.no_alarms);
            return;
        }
        nextAlarmMessageTextView.setText(DateUtils.getRelativeTimeSpanString(nextAlarmDate.getTimeInMillis(),
                Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis(), 0));
    }

    private void showErrorMessage(final String error) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(error)
                .show();
    }

    private void checkDeeplink(final Intent intent) {
        if (intent == null) {
            return;
        }
        final Uri data = intent.getData();
        if (data == null) {
            return;
        }
        final String host = data.getHost();
        Toast.makeText(this, "Load from deeplink : " + host, Toast.LENGTH_LONG).show();
    }

    private static class RefreshNextAlarmMessageHandler extends Handler {

        private final WeakReference<MainActivity> activityRef;

        RefreshNextAlarmMessageHandler(final MainActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(final Message msg) {
            final MainActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            activity.updateNextAlarmMessage();
            sendEmptyMessageDelayed(0, 1000L);
        }

        void start() {
            sendEmptyMessageDelayed(0, 1000L);
        }

        void stop() {
            removeCallbacksAndMessages(null);
        }

    }

    private static class AlarmNeedUiRefreshBroadcastReceiver extends BroadcastReceiver {

        private final WeakReference<MainActivity> mainActivityRef;

        AlarmNeedUiRefreshBroadcastReceiver(final MainActivity mainActivity) {
            this.mainActivityRef = new WeakReference<>(mainActivity);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final MainActivity mainActivity = mainActivityRef.get();
            if (mainActivity == null) {
                return;
            }

            mainActivity.syncAlarmList();
            mainActivity.updateNextAlarmMessage();
        }

    }

}
