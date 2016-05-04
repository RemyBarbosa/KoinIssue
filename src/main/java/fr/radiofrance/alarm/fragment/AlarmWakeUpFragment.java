package fr.radiofrance.alarm.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.radiofrance.player.mediaplayer.Playable;
import com.radiofrance.player.service.PlayerService;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import fr.radiofrance.alarm.R;
import fr.radiofrance.alarm.activity.AlarmActivity;
import fr.radiofrance.alarm.receiver.AlarmReceiver;
import fr.radiofrance.analytics.AtInternetHelper;
import fr.radiofrance.analytics.Xiti;
import fr.radiofrance.androidtoolbox.constant.Constants;
import fr.radiofrance.androidtoolbox.io.PrefsTools;
import fr.radiofrance.model.station.Station;

public class AlarmWakeUpFragment extends Fragment {

    public static final String ALARM_RADIO_INDEX = "com.radiofrance.radio.radiofrance.RadioFranceAlarmRadio";
    public static final String ALARM_TIME = "com.radiofrance.radio.radiofrance.RadioFranceAlarmTime";
    private static final long TEN_SECONDS = 10 * 1000;
    private Station mRadio;
    private TextUpdateHandler mHandler;
    private AlarmActivity mActivity;
    private static ProgressDialog mProgressDialog      = null;
    private static int            mProgressRequested   = 0;
    Station station;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AlarmActivity) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PrefsTools.hasKey(mActivity, Constants.PREF_SELECTED_STATION)) {
            String json = PrefsTools.getString(mActivity, Constants.PREF_SELECTED_STATION);
            station =  new Gson().fromJson(json, Station.class);
        }

        if (station != null){
            AtInternetHelper.sendScreenTag(Xiti.formatLabel(station.getTitle()),
                    getString(R.string.xiti_configuration),
                    getString(R.string.xiti_alarm_clock),
                    getString(R.string.xiti_wake_up)
            );
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_alarm_wakeup, null);
        mHandler = new TextUpdateHandler(mActivity);

        view.findViewById(R.id.snooze).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (station != null){
                    AtInternetHelper.sendGestureTag(AtInternetHelper.GESTURE_TOUCH,
                            Xiti.formatLabel(station.getTitle()),
                            getString(R.string.xiti_alarm_clock),
                            getString(R.string.xiti_wake_up),
                            getString(R.string.xiti_clock_snooze)
                    );
                }

                stopPlayer();

                ((TextView) view.findViewById(R.id.wakeup_text)).setText(getString(R.string.ten_min));
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (!isDetached()){
                            try {
                                ((TextView) view.findViewById(R.id.wakeup_text)).setText(getString(R.string.wake_up));
                            } catch (Exception e){

                            }
                        }
                    }
                }, TEN_SECONDS);
                AlarmReceiver.snoozeAlarm(mActivity);
                v.setEnabled(false);
                ((TextView) v).setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        });

        view.findViewById(R.id.stop).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (station != null){
                    AtInternetHelper.sendGestureTag(AtInternetHelper.GESTURE_TOUCH,
                            Xiti.formatLabel(station.getTitle()),
                            getString(R.string.xiti_alarm_clock),
                            getString(R.string.xiti_wake_up),
                            getString(R.string.xiti_stop)
                    );
                }

                stopPlayer();
                dismissProgressDialog();

                // Start default Activity of the app
                PackageManager pm = mActivity.getPackageManager();
                Intent LaunchIntent = pm.getLaunchIntentForPackage(mActivity.getPackageName());
                startActivity(LaunchIntent);

                mActivity.finish();
            }
        });

        return view;
    }

    private Playable generatePlayable() {

        String desc = mRadio.getTitle();

        return Playable.PlayableFactory.fromURL(
                mRadio.getDefaultStream(),
                mRadio.getTitle(),
                mRadio.getTitle(),
                desc,
                R.drawable.logo_fb);
    }

    private void stopPlayer(){
        Intent intent = new Intent(mActivity, PlayerService.class);
        intent.putExtra(PlayerService.PARAM_MODE, PlayerService.MODE_STOP);
        mActivity.startService(intent);
    }

    private void startPlayer(){
        Playable replay = generatePlayable();

        Intent intent = new Intent(mActivity, PlayerService.class);
        intent.putExtra(PlayerService.PARAM_MODE, PlayerService.MODE_PLAY);
        intent.putExtra(PlayerService.PARAM_PLAYABLE, replay);
        mActivity.startService(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        Calendar time = AlarmActivity.getAlarmTime(mActivity);

        mRadio = new Gson().fromJson(PrefsTools.getString(mActivity, Constants.PREF_SELECTED_STATION), Station.class);
        startPlayer();

        AtInternetHelper.sendScreenTag(
                getString(R.string.xiti_alarm_clock), getString(R.string.xiti_wake_up),
                Xiti.formatForTag(mRadio.getTitle()), time.get(Calendar.HOUR_OF_DAY) + "_" + time.get(Calendar.MINUTE));
    }

    public static void showProgressDialog(Activity activity) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setMessage(activity.getString(R.string.loading));
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressRequested = 0;
                    mProgressDialog = null;
                }
            });
            mProgressDialog.show();
        }
        mProgressRequested++;
    }

    public static void hideProgressDialog() {
        mProgressRequested--;
        if (mProgressRequested <= 0) {
            mProgressRequested = 0;
            if (mProgressDialog != null) {
                mProgressDialog.hide();
                mProgressDialog = null;
            }
        }
    }

    public static void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressRequested = 0;
        }
    }

    private static class TextUpdateHandler extends Handler {
        private final WeakReference<AlarmActivity> mActivity;


        private TextUpdateHandler(AlarmActivity mActivity) {
            this.mActivity = new WeakReference<AlarmActivity>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
        }
    }

}
