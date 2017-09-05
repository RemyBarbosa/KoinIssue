package fr.radiofrance.alarmdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import fr.radiofrance.alarm.exception.RfAlarmException;
import fr.radiofrance.alarm.manager.RfAlarmManager;
import fr.radiofrance.alarm.util.DeviceVolumeUtils;
import fr.radiofrance.alarmdemo.adapter.AlarmsAdapter;
import fr.radiofrance.alarmdemo.listener.OnAlarmActionListener;
import fr.radiofrance.alarmdemo.model.DemoAlarm;
import fr.radiofrance.alarmdemo.player.DemoPlayer;
import fr.radiofrance.alarmdemo.view.DividerItemDecoration;

public class MainActivity extends AppCompatActivity {

    private TextView nextAlarmMessageTextView;
    private RecyclerView alarmsRecyclerView;
    private View addAlarmButton;
    private View addAlarmDialogView;
    private AlarmsAdapter alarmsAdapter;

    private RfAlarmManager<DemoAlarm> alarmManager;

    private CheckBox monday;
    private CheckBox tuesday;
    private CheckBox wednesday;
    private CheckBox thursday;
    private CheckBox friday;
    private CheckBox saturday;
    private CheckBox sunday;
    private EditText hours;
    private EditText minutes;
    private SeekBar volume;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        alarmManager = new RfAlarmManager<>(getApplicationContext(), DemoAlarm.class);

        findViews();
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        alarmsAdapter.setAlarms(alarmManager.getAllAlarms());
        updateNextAlarmMessage();
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
                    showErrorMessage(e.getMessage());
                    break;
                }
                alarmsAdapter.getAlarms().clear();
                alarmsAdapter.notifyDataSetChanged();
                updateNextAlarmMessage();
                break;
            case R.id.debug:
                final List<DemoAlarm> alarms = alarmManager.getAllAlarms();

                String debug = "";
                for (final DemoAlarm alarm : alarms) {
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
            case R.id.stop_player:
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

        addAlarmDialogView = LayoutInflater.from(this).inflate(R.layout.activity_main_dialog, null);
        monday = addAlarmDialogView.findViewById(R.id.monday);
        tuesday = addAlarmDialogView.findViewById(R.id.tuesday);
        wednesday = addAlarmDialogView.findViewById(R.id.wednesday);
        thursday = addAlarmDialogView.findViewById(R.id.thursday);
        friday = addAlarmDialogView.findViewById(R.id.friday);
        saturday = addAlarmDialogView.findViewById(R.id.saturday);
        sunday = addAlarmDialogView.findViewById(R.id.sunday);
        hours = addAlarmDialogView.findViewById(R.id.hours);
        minutes = addAlarmDialogView.findViewById(R.id.minutes);
        volume = addAlarmDialogView.findViewById(R.id.volume);
    }

    private void initViews() {
        alarmsAdapter = new AlarmsAdapter(this, new ArrayList<DemoAlarm>(), new OnAlarmActionListener() {

            @Override
            public void onAlarmClick(DemoAlarm alarm, int position) {
                showUpdateAlarmDialog(alarm, position);
            }

            @Override
            public void onAlarmLongClick(DemoAlarm alarm, int position) {
                try {
                    alarmManager.removeAlarm(alarm.getId());
                } catch (RfAlarmException e) {
                    showErrorMessage(e.getMessage());
                    return;
                }
                alarmsAdapter.removeAlarm(alarm);
                updateNextAlarmMessage();
            }

            @Override
            public void onAlarmActivated(DemoAlarm alarm, boolean isActivated, int position) {
                if (alarm == null) {
                    return;
                }
                alarm.setActivated(isActivated);
                try {
                    alarmManager.updateAlarm(alarm);
                } catch (RfAlarmException e) {
                    showErrorMessage(e.getMessage());
                    return;
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

    private void showAddAlarmDialog() {
        final ViewGroup parentView = (ViewGroup) addAlarmDialogView.getParent();
        if (parentView != null) {
            parentView.removeAllViews();
        }

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
                createAlarm();
            }

        });
        builder.setNegativeButton("Cancel", null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    private void showUpdateAlarmDialog(@NonNull final DemoAlarm alarm, final int alarmPosition) {
        final ViewGroup parentView = (ViewGroup) addAlarmDialogView.getParent();
        if (parentView != null) {
            parentView.removeAllViews();
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(addAlarmDialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updateAlarm(alarm, alarmPosition);
            }

        });
        builder.setNegativeButton("Cancel", null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    private void createAlarm() {
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

        final DemoAlarm alarm = new DemoAlarm();
        alarm.setDays(days);
        alarm.setHours(h);
        alarm.setMinutes(m);
        alarm.setSnoozeDuration(10000);
        alarm.setVolume(volume.getProgress());
        alarm.setActivated(true);
        alarm.setCustomField("custom field");
        alarm.setIntent(new Intent(getApplicationContext(), DemoAlarmActivity.class));

        alarmsAdapter.addAlarm(alarm);

        try {
            alarmManager.addAlarm(alarm);
        } catch (RfAlarmException e) {
            showErrorMessage(e.getMessage());
            return;
        }
        updateNextAlarmMessage();
    }

    private void updateAlarm(@NonNull final DemoAlarm alarm, final int alarmPosition) {
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
        alarm.setSnoozeDuration(10000);
        alarm.setVolume(volume.getProgress());

        alarmsAdapter.notifyItemChanged(alarmPosition);

        try {
            alarmManager.updateAlarm(alarm);
        } catch (RfAlarmException e) {
            showErrorMessage(e.getMessage());
            return;
        }
        updateNextAlarmMessage();
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

}
