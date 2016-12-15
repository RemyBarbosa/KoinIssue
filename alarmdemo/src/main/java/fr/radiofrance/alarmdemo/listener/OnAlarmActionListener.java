package fr.radiofrance.alarmdemo.listener;

import fr.radiofrance.alarm.model.Alarm;

public interface OnAlarmActionListener {

    void onAlarmClick(Alarm alarm, int position);

    void onAlarmLongClick(Alarm alarm, int position);

    void onAlarmActivated(Alarm alarm, boolean isActivated, int position);

}
