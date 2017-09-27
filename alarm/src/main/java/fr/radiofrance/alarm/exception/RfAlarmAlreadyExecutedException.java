package fr.radiofrance.alarm.exception;

import android.content.Intent;

public class RfAlarmAlreadyExecutedException extends RfAlarmException {

    public final Intent appLaunchIntent;

    public RfAlarmAlreadyExecutedException(final Intent appLaunchIntent) {
        this.appLaunchIntent = appLaunchIntent;
    }
}
