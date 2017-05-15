package fr.radiofrance.alarmdemo.listener;

import fr.radiofrance.alarmdemo.model.AlarmModel;

public interface OnAlarmActionListener {

    void onAlarmClick(AlarmModel alarm, int position);

    void onAlarmLongClick(AlarmModel alarm, int position);

    void onAlarmActivated(AlarmModel alarm, boolean isActivated, int position);

}
