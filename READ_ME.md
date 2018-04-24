# Alarm library #

## Description ##

This is the common Android library used to create alarm in radiofrance Android applications.

- It creates, removes and stores the alarms create by the user.
- It schedules the alarms in the system to be fired on right time.
- It provides a default AlarmLockScreen Activity, displaid on time, with an additional Snooze feature.

## Debug adb commands ##

### Test deeplink :###

``` > adb shell am start -a android.intent.action.VIEW -d "alarmdemo://screen.alarm.edit" fr.radiofrance.alarmdemo```


### Logs system alarms :###

``` > adb shell dumpsys alarm```

or


``` > adb shell "dumpsys alarm | grep fr.radiofrance.alarmdemo"```

### Simulate doze mode on device :###

``` > adb shell dumpsys battery unplug```
      
``` > adb shell dumpsys deviceidle force-idle ```

revert with :

``` > adb shell dumpsys battery reset```

``` > adb shell dumpsys deviceidle disable```

### Android documentations on alarms###

https://developer.android.com/training/scheduling/alarms.html

https://developer.android.com/topic/performance/vitals/wakeup.html

https://developer.android.com/reference/android/app/AlarmManager.html


### Stackoverflow threads on alarms###

https://stackoverflow.com/questions/48008999/setalarmclock-fires-too-late-in-doze-mode

https://stackoverflow.com/questions/28742884/how-to-read-adb-shell-dumpsys-alarm-output

https://stackoverflow.com/questions/34074955/android-exact-alarm-is-always-3-minutes-off

https://stackoverflow.com/questions/34699662/how-does-alarmmanager-alarmclockinfos-pendingintent-work