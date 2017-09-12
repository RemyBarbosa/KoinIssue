** DEBUG ADB COMMANDS :

* Test deeplink :

``` > adb shell am start -a android.intent.action.VIEW -d "alarmdemo://screen.alarm.edit" fr.radiofrance.alarmdemo```


* Logs system alarms :

``` > adb shell dumpsys alarm```

or


``` > adb shell "dumpsys alarm | grep fr.radiofrance.alarmdemo"```


* Stack overflow thread on alarms

https://stackoverflow.com/questions/28742884/how-to-read-adb-shell-dumpsys-alarm-output

https://stackoverflow.com/questions/34074955/android-exact-alarm-is-always-3-minutes-off