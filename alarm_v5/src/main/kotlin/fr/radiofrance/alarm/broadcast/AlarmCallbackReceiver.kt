package fr.radiofrance.alarm.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager


abstract class AlarmCallbackReceiver : BroadcastReceiver() {

    class OnRangCustomCallback(val context: Context, val data: Bundle) {
        fun onRangCustomSucceed() {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(AlarmIntentBuilder.buildCallbackOnRangCustomOkAction(context, data))
        }
    }

    final override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return
        intent.action?.let {
            when (it) {
                AlarmIntentBuilder.ALARM_CALLBACK_ON_RANG_ACTION -> return onRang(context, OnRangCustomCallback(context, intent.getBundleExtra(AlarmIntentBuilder.ALARM_EXTRA_DATA_KEY)))
                AlarmIntentBuilder.ALARM_CALLBACK_ON_STOP_ACTION -> return onStop(context)
                else -> return
            }
        }
    }

    open fun onRang(context: Context, callback: OnRangCustomCallback) {}
    open fun onStop(context: Context) {}

}