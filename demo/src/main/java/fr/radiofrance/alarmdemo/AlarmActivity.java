package fr.radiofrance.alarmdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import fr.radiofrance.alarm.manager.AlarmManager;

/**
 * Created by mondon on 17/05/16.
 */
public class AlarmActivity extends AppCompatActivity {

    private Button stopButton;
    private Button snoozeButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm);
        findViews();
        initViews();
    }

    private void findViews() {
        stopButton = (Button) findViewById(R.id.stop);
        snoozeButton = (Button) findViewById(R.id.snooze);
    }

    private void initViews() {
        stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });
        snoozeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new AlarmManager(AlarmActivity.this).snoozeAlarm();
                finish();
            }

        });
    }

}
