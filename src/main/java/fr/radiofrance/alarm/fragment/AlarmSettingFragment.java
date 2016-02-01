package fr.radiofrance.alarm.fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.TimeZone;

import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelChangedListener;
import antistatic.spinnerwheel.OnWheelClickedListener;
import antistatic.spinnerwheel.adapters.NumericWheelAdapter;
import fr.radiofrance.alarm.R;
import fr.radiofrance.alarm.activity.AlarmActivity;
import fr.radiofrance.alarm.receiver.AlarmReceiver;
import fr.radiofrance.androidtoolbox.analytics.AtInternetHelper;
import fr.radiofrance.androidtoolbox.constant.Constants;
import fr.radiofrance.androidtoolbox.io.PrefsTools;
import fr.radiofrance.androidtoolbox.view.FontTextView;
import fr.radiofrance.model.station.Station;

public class AlarmSettingFragment extends Fragment {

    private static final int START_MODE = 0;
    private static final int EDIT_MODE = 1;
    private static final int RUNNING_MODE = 2;

    private Calendar mAlarmTime;
    private AbstractWheel hoursWheel;
    private AbstractWheel minsWheel;
    private AlarmActivity mActivity;
    private TextView      mCountDownView;
    private int mMode = START_MODE;

    private FrameLayout mTouchInterceptorLayout;
    private ImageView   mEditionButtonImageView;

    private CountDownTimer mCountDown;
    Station station;

    private FontTextView changeHourBtn;

    private final static int ONE_DAY = 1000 * 60 * 60 * 24;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PrefsTools.hasKey(getActivity(), Constants.PREF_SELECTED_STATION)) {
            String json = PrefsTools.getString(getActivity(), Constants.PREF_SELECTED_STATION);
            station =  new Gson().fromJson(json, Station.class);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_alarm_setting, null);

        mActivity = (AlarmActivity) getActivity();

        if (mActivity.getAlarmActivated(false)) {
            mMode = RUNNING_MODE;
        }

        mAlarmTime = AlarmActivity.getAlarmTime(mActivity);
        AlarmActivity.logDate( "ALARM_TIME", mAlarmTime);

        mAlarmTime = timeAfterNow(mAlarmTime);

        view.findViewById(R.id.radio).setVisibility(View.INVISIBLE);

        mCountDownView = (TextView) view.findViewById(R.id.countdown);
        changeHourBtn = (FontTextView) view.findViewById(R.id.changeHourBtn);

        view.findViewById(R.id.edit).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mMode) {
                    case START_MODE:
                        mAlarmTime = timeFromWheels();
                        mActivity.setAlarmTime(mActivity, mAlarmTime);
                        AlarmReceiver.setAlarm(getActivity(), mAlarmTime);
                        mActivity.setAlarmActivated(true);
                        mMode = RUNNING_MODE;

                        if (station != null){
                            AtInternetHelper.sendGestureTag(AtInternetHelper.GESTURE_TOUCH,
                                    AtInternetHelper.formatLabel(station.getTitle()),
                                    getString(R.string.xiti_alarm_clock),
                                    getString(R.string.xiti_status),
                                    getString(R.string.xiti_on)
                            );
                        }


                        break;
                    case EDIT_MODE:
                        AtInternetHelper.sendGestureTag(AtInternetHelper.GESTURE_TOUCH,
                                AtInternetHelper.formatLabel(station.getTitle()),
                                getString(R.string.xiti_alarm_clock),
                                getString(R.string.xiti_hour_changing)
                        );

                        mMode = START_MODE;
                        changeHourBtn.setVisibility(View.VISIBLE);

                        break;
                    case RUNNING_MODE:
                        AlarmReceiver.cancelAlarm(getActivity());
                        mActivity.setAlarmActivated(false);
                        mMode = START_MODE;

                        if (station != null){
                            AtInternetHelper.sendGestureTag(AtInternetHelper.GESTURE_TOUCH,
                                    AtInternetHelper.formatLabel(station.getTitle()),
                                    getString(R.string.xiti_alarm_clock),
                                    getString(R.string.xiti_status),
                                    getString(R.string.xiti_off)
                            );
                        }

                        break;
                }
                updateContent();
            }
        });

        mTouchInterceptorLayout = (FrameLayout) view.findViewById(R.id.touch_interceptor);
        mEditionButtonImageView = (ImageView) view.findViewById(R.id.edit);

        hoursWheel = (AbstractWheel) view.findViewById(R.id.hour);
        NumericWheelAdapter hoursAdapter = new NumericWheelAdapter(getActivity(), 0, 23, "%02d");
        hoursAdapter.setTextColor(getResources().getColor(android.R.color.white));
        hoursAdapter.setTextSize(80);
        hoursWheel.setViewAdapter(hoursAdapter);
        hoursWheel.setCyclic(true);

        minsWheel = (AbstractWheel) view.findViewById(R.id.mins);
        NumericWheelAdapter minAdapter = new NumericWheelAdapter(getActivity(), 0, 59, "%02d");
        minAdapter.setTextColor(getResources().getColor(android.R.color.white));
        minAdapter.setTextSize(80);
        minsWheel.setViewAdapter(minAdapter);
        minsWheel.setCyclic(true);

        hoursWheel.setCurrentItem(mAlarmTime.get(Calendar.HOUR_OF_DAY));
        minsWheel.setCurrentItem(mAlarmTime.get(Calendar.MINUTE));

        OnWheelChangedListener wheelListener = new OnWheelChangedListener() {
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                ((AlarmActivity) getActivity()).setupBackground(timeFromWheels());
            }
        };
        hoursWheel.addChangingListener(wheelListener);
        minsWheel.addChangingListener(wheelListener);

        OnWheelClickedListener click = new OnWheelClickedListener() {
            public void onItemClicked(AbstractWheel wheel, int itemIndex) {
                wheel.setCurrentItem(itemIndex, true);

                if (station != null){
                    AtInternetHelper.sendGestureTag(AtInternetHelper.GESTURE_TOUCH,
                            AtInternetHelper.formatLabel(station.getTitle()),
                            getString(R.string.xiti_alarm_clock),
                            getString(R.string.xiti_hour_changing)
                    );
                }

            }
        };
        hoursWheel.addClickingListener(click);
        minsWheel.addClickingListener(click);

        OnClickListener editClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMode == START_MODE) {
                    mMode = EDIT_MODE;
                    updateContent();
                    changeHourBtn.setVisibility(View.INVISIBLE);
                }
            }
        };

        mTouchInterceptorLayout.setOnClickListener(editClickListener);
        changeHourBtn.setOnClickListener(editClickListener);

        updateContent();
        return view;
    }

    public void updateContent() {
        hoursWheel.setVisibleItems(mMode == EDIT_MODE ? 3 : 1);
        minsWheel.setVisibleItems(mMode == EDIT_MODE ? 3 : 1);
        hoursWheel.setEnabled(mMode == EDIT_MODE);
        minsWheel.setEnabled(mMode == EDIT_MODE);
        mTouchInterceptorLayout.setVisibility(mMode != START_MODE ? View.INVISIBLE : View.VISIBLE);
        int res = 0;
        switch (this.mMode) {
            case START_MODE:
                res = R.drawable.alarm_start_button;
                break;
            case EDIT_MODE:
                res = R.drawable.alarm_validate_button;
                break;
            case RUNNING_MODE:
                res = R.drawable.alarm_disable_button;
                break;
        }
        mEditionButtonImageView.setImageResource(res);
        if (mCountDown != null) {
            mCountDown.cancel();
            mCountDown = null;
        }
        if (mMode != RUNNING_MODE) {
            mCountDownView.setVisibility(View.INVISIBLE);
        } else {
            timeAfterNow(mAlarmTime);
            long t = mAlarmTime.getTimeInMillis() - System.currentTimeMillis();
            if (t > 0) {
                mCountDownView.setVisibility(View.VISIBLE);
                mCountDown = new CountDownTimer(3000, 1000) {
                    boolean isStopped = false;

                    @Override
                    public void onTick(long l) {
                        if (getActivity() == null || mCountDownView == null) {
                            isStopped = true;
                        }
                        if (!isStopped) {
                            long t = (mAlarmTime.getTimeInMillis() - System.currentTimeMillis()) / 1000;
                            int sec = (int) t % 60;
                            t = (t - sec) / 60;
                            int min = (int) t % 60;
                            int h = (int) (t - min) / 60;
                            mCountDownView.setText(getString(R.string.reveil_countdown, h, min, sec));
                        }
                    }

                    @Override
                    public void onFinish() {
                        isStopped = true;
                        mCountDownView.setVisibility(View.INVISIBLE);
                        cancel();
                    }
                }.start();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Xiti.sendTag(getActivity(), "Reveil");
    }

    private Calendar timeFromWheels() {
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, hoursWheel.getCurrentItem());
        time.set(Calendar.MINUTE, minsWheel.getCurrentItem());
        time.set(Calendar.SECOND, 0);
        time.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        timeAfterNow(time);
        return time;
    }

    public static Calendar timeAfterNow(Calendar time) {
        Calendar now = Calendar.getInstance();

        int tmpM = time.get(Calendar.MINUTE);
        int tmpH = time.get(Calendar.HOUR_OF_DAY);
        time = Calendar.getInstance();
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.HOUR_OF_DAY, tmpH);
        time.set(Calendar.MINUTE, tmpM);

        // Used to compute date sets
        time.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        if (time.getTimeInMillis() <= now.getTimeInMillis()) {
            time.setTimeInMillis(time.getTimeInMillis() + ONE_DAY);
        }

        // Used to compute date sets
        time.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

//        AlarmActivity.logDate("TIMEAFTERNOW result", time);

        return time;
    }
}
