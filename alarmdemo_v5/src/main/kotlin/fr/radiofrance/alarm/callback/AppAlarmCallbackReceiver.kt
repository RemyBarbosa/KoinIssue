package fr.radiofrance.alarm.callback

import android.content.Context
import fr.radiofrance.alarm.broadcast.AlarmCallbackReceiver

class AppAlarmCallbackReceiver : AlarmCallbackReceiver() {

    override fun onRang(context: Context, callback: OnRangCustomCallback) {

        DemoPlayer.getInstance(context).play("http://direct.fipradio.fr/live/fip-lofi.mp3")

        callback.onRangCustomSucceed()
    }

}