package fr.radiofrance.alarmdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import fr.radiofrance.alarm.manager.AlarmManager;
import fr.radiofrance.alarm.type.Day;

public class MainActivity extends AppCompatActivity {

    private CheckBox monday;
    private CheckBox tuesday;
    private CheckBox wednesday;
    private CheckBox thursday;
    private CheckBox friday;
    private CheckBox saturday;
    private CheckBox sunday;
    private EditText hours;
    private EditText minutes;
    private Button setAlarmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViews();
        initViews();
    }

    private void findViews() {
        monday = (CheckBox) findViewById(R.id.monday);
        tuesday = (CheckBox) findViewById(R.id.tuesday);
        wednesday = (CheckBox) findViewById(R.id.wednesday);
        thursday = (CheckBox) findViewById(R.id.thursday);
        friday = (CheckBox) findViewById(R.id.friday);
        saturday = (CheckBox) findViewById(R.id.saturday);
        sunday = (CheckBox) findViewById(R.id.sunday);
        hours = (EditText) findViewById(R.id.hours);
        minutes = (EditText) findViewById(R.id.minutes);
        setAlarmButton = (Button) findViewById(R.id.set_alarm_button);
    }

    private void initViews() {
        setAlarmButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                List<Day> days = new ArrayList<>();
                if (monday.isChecked()) days.add(Day.MONDAY);
                if (tuesday.isChecked()) days.add(Day.TUESDAY);
                if (wednesday.isChecked()) days.add(Day.WEDNESDAY);
                if (thursday.isChecked()) days.add(Day.THURSDAY);
                if (friday.isChecked()) days.add(Day.FRIDAY);
                if (saturday.isChecked()) days.add(Day.SATURDAY);
                if (sunday.isChecked()) days.add(Day.SUNDAY);

                int h = Integer.parseInt(hours.getText().toString());
                int m = Integer.parseInt(minutes.getText().toString());

                new AlarmManager(MainActivity.this)
                        .setAlarm(new Intent(MainActivity.this, AlarmActivity.class), days, h, m);
            }

        });
    }

}
