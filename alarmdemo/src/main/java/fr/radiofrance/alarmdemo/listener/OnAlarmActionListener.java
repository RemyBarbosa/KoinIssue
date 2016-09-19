package fr.radiofrance.alarmdemo.listener;

import fr.radiofrance.alarmdemo.model.Alarm;

/**
 * Created by mondon on 13/09/16.
 */
public interface OnAlarmActionListener {

    void onAlarmClick(Alarm alarm, int position);

    void onAlarmLongClick(Alarm alarm, int position);

    void onAlarmActivated(Alarm alarm, boolean isActivated, int position);

}
