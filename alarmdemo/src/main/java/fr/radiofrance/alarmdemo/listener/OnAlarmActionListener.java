package fr.radiofrance.alarmdemo.listener;

import fr.radiofrance.alarmdemo.model.DemoAlarm;

public interface OnAlarmActionListener {

    void onAlarmClick(DemoAlarm alarm, int position);

    void onAlarmLongClick(DemoAlarm alarm, int position);

    void onAlarmActivated(DemoAlarm alarm, boolean isActivated, int position);

}
