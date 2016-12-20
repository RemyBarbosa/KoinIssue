package fr.radiofrance.alarmdemo;

import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import fr.radiofrance.alarm.manager.AlarmManager;
import fr.radiofrance.alarm.model.Alarm;
import fr.radiofrance.alarmdemo.adapter.AlarmsAdapter;
import fr.radiofrance.alarmdemo.listener.OnAlarmActionListener;
import fr.radiofrance.alarmdemo.view.DividerItemDecoration;

public class MainActivity extends AppCompatActivity {

    private TextView nextAlarmMessageTextView;
    private RecyclerView alarmsRecyclerView;
    private View addAlarmButton;
    private View addAlarmDialogView;
    private AlarmsAdapter alarmsAdapter;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViews();
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateNextAlarmMessage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemId = item.getItemId();

        if (menuItemId == R.id.clean_all) {
            AlarmManager.getInstance().removeAllAlarms();
            alarmsAdapter.getAlarms().clear();
            alarmsAdapter.notifyDataSetChanged();
            updateNextAlarmMessage();
        } else if (menuItemId == R.id.debug) {
            List<fr.radiofrance.alarm.model.Alarm> alarms = AlarmManager
                    .getInstance().getAllAlarms();

            String debug = "";
            for (fr.radiofrance.alarm.model.Alarm alarm : alarms) {
                if (alarm != null) {
                    debug += alarm + "\n";
                }
            }

            if (TextUtils.isEmpty(debug)) {
                debug = getString(R.string.no_alarms);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(debug);
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void findViews() {
        nextAlarmMessageTextView = (TextView) findViewById(R.id.next_alarm_message);
        alarmsRecyclerView = (RecyclerView) findViewById(R.id.alarms_recycler_view);
        addAlarmButton = findViewById(R.id.add_alarm_button);

        addAlarmDialogView = LayoutInflater.from(this).inflate(R.layout.activity_main_dialog, null);
        monday = (CheckBox) addAlarmDialogView.findViewById(R.id.monday);
        tuesday = (CheckBox) addAlarmDialogView.findViewById(R.id.tuesday);
        wednesday = (CheckBox) addAlarmDialogView.findViewById(R.id.wednesday);
        thursday = (CheckBox) addAlarmDialogView.findViewById(R.id.thursday);
        friday = (CheckBox) addAlarmDialogView.findViewById(R.id.friday);
        saturday = (CheckBox) addAlarmDialogView.findViewById(R.id.saturday);
        sunday = (CheckBox) addAlarmDialogView.findViewById(R.id.sunday);
        hours = (EditText) addAlarmDialogView.findViewById(R.id.hours);
        minutes = (EditText) addAlarmDialogView.findViewById(R.id.minutes);
        volume = (SeekBar) addAlarmDialogView.findViewById(R.id.volume);
    }

    private void initViews() {
        alarmsAdapter = new AlarmsAdapter(this, AlarmManager.getInstance().getAllAlarms(), new OnAlarmActionListener() {

            @Override
            public void onAlarmClick(Alarm alarm, int position) {
                showUpdateAlarmDialog(alarm, position);
            }

            @Override
            public void onAlarmLongClick(Alarm alarm, int position) {
                AlarmManager.getInstance().removeAlarm(alarm.getId());
                alarmsAdapter.removeAlarm(alarm);
                updateNextAlarmMessage();
            }

            @Override
            public void onAlarmActivated(Alarm alarm, boolean isActivated, int position) {
                if (alarm == null) return;

                alarm.setActivated(isActivated);
                AlarmManager.getInstance().updateAlarm(alarm);
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

        volume.setMax(AlarmManager.getInstance().getDeviceMaxVolume());
        volume.setProgress(AlarmManager.getInstance().getDeviceVolume());
    }

    private void showAddAlarmDialog() {
        ViewGroup parentView = (ViewGroup) addAlarmDialogView.getParent();
        if (parentView != null) {
            parentView.removeAllViews();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(addAlarmDialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                createAlarm();
            }

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }

        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    private void showUpdateAlarmDialog(@NonNull final Alarm alarm, final int alarmPosition) {
        ViewGroup parentView = (ViewGroup) addAlarmDialogView.getParent();
        if (parentView != null) {
            parentView.removeAllViews();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(addAlarmDialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updateAlarm(alarm, alarmPosition);
            }

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }

        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    private void createAlarm() {
        List<Integer> days = new ArrayList<>();
        if (monday.isChecked()) days.add(Calendar.MONDAY);
        if (tuesday.isChecked()) days.add(Calendar.TUESDAY);
        if (wednesday.isChecked()) days.add(Calendar.WEDNESDAY);
        if (thursday.isChecked()) days.add(Calendar.THURSDAY);
        if (friday.isChecked()) days.add(Calendar.FRIDAY);
        if (saturday.isChecked()) days.add(Calendar.SATURDAY);
        if (sunday.isChecked()) days.add(Calendar.SUNDAY);

        int h = Integer.parseInt(hours.getText().toString());
        int m = Integer.parseInt(minutes.getText().toString());

        Alarm alarm = new Alarm(String.format("%s", new Date().getTime()));
        alarm.setDays(days);
        alarm.setHours(h);
        alarm.setMinutes(m);
        alarm.setSnoozeDuration(10000);
        alarm.setVolume(volume.getProgress());
        alarm.setActivated(true);
        alarm.setIntent(new Intent(MainActivity.this, AlarmActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));

        alarmsAdapter.addAlarm(alarm);

        AlarmManager.getInstance().addAlarm(alarm);
        updateNextAlarmMessage();
    }

    private void updateAlarm(@NonNull Alarm alarm, int alarmPosition) {
        List<Integer> days = new ArrayList<>();
        if (monday.isChecked()) days.add(Calendar.MONDAY);
        if (tuesday.isChecked()) days.add(Calendar.TUESDAY);
        if (wednesday.isChecked()) days.add(Calendar.WEDNESDAY);
        if (thursday.isChecked()) days.add(Calendar.THURSDAY);
        if (friday.isChecked()) days.add(Calendar.FRIDAY);
        if (saturday.isChecked()) days.add(Calendar.SATURDAY);
        if (sunday.isChecked()) days.add(Calendar.SUNDAY);

        int h = Integer.parseInt(hours.getText().toString());
        int m = Integer.parseInt(minutes.getText().toString());

        alarm.setDays(days);
        alarm.setHours(h);
        alarm.setMinutes(m);
        alarm.setSnoozeDuration(10000);
        alarm.setVolume(volume.getProgress());

        alarmsAdapter.notifyItemChanged(alarmPosition);

        AlarmManager.getInstance().updateAlarm(alarm);
        updateNextAlarmMessage();
    }

    private void updateNextAlarmMessage() {
        Calendar nextAlarmDate = AlarmManager.getInstance().getNextAlarmDate();
        if (nextAlarmDate != null) {
            nextAlarmMessageTextView.setText(DateUtils
                    .getRelativeTimeSpanString(nextAlarmDate.getTimeInMillis(),
                            Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis(), 0));
        } else {
            nextAlarmMessageTextView.setText(R.string.no_alarms);
        }
    }

}
