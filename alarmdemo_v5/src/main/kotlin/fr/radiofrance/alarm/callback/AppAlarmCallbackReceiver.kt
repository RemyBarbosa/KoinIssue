package fr.radiofrance.alarm.callback

import android.content.Context
import android.os.Handler
import android.os.Message
import fr.radiofrance.alarm.broadcast.AlarmCallbackReceiver

class AppAlarmCallbackReceiver : AlarmCallbackReceiver() {

    override fun onRang(context: Context, callback: OnRangCustomCallback) {
        val player = DemoPlayer.getInstance(context)
        player.play("http://direct.fipradio.fr/live/fip-lofi.mp3")
        CheckPlayerHandler(player, callback).check()
    }

    override fun onStop(context: Context) {
        DemoPlayer.getInstance(context).stop()
    }

    private class CheckPlayerHandler(private val player: DemoPlayer, private val callback: OnRangCustomCallback) : Handler() {

        internal fun check() {
            sendMessage(obtainMessage(MESSAGE_WHAT))
        }

        override fun handleMessage(msg: Message) {
            if (player.isPlaying) {
                callback.onRangCustomSucceed()
                return
            }
            sendMessageDelayed(obtainMessage(MESSAGE_WHAT), CHECK_WAKE_UP_RETRY_DELAY_MS)
        }

        companion object {
            private const val CHECK_WAKE_UP_RETRY_DELAY_MS = 500L
            private const val MESSAGE_WHAT = 0
        }
    }

}