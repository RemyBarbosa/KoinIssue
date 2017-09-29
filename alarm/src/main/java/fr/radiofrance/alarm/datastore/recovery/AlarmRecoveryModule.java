package fr.radiofrance.alarm.datastore.recovery;

import fr.radiofrance.alarm.BuildConfig;
import fr.radiofrance.alarm.model.Alarm;

public abstract class AlarmRecoveryModule {

    public abstract Alarm onUpdateNeeded(final Alarm alarm, final String source);

    public Alarm update(final Alarm alarm, final String source) {
        if (alarm == null) {
            return null;
        }
        if (alarm.getVersion() == BuildConfig.LIBRARY_VERSION_CODE) {
            return null;
        }
        final Alarm alarmUpdated = onUpdateNeeded(alarm, source);
        if (alarmUpdated == null) {
            return null;
        }
        alarmUpdated.setVersion(BuildConfig.LIBRARY_VERSION_CODE);
        return alarmUpdated;
    }

}
