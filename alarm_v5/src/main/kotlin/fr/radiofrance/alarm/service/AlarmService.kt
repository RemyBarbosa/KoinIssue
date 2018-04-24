package fr.radiofrance.alarm.service


import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Vibrator
import fr.radiofrance.alarm.activity.AlarmActivity


class AlarmService : Service() {

    private val ringtone by lazy { RingtoneManager.getRingtone(applicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)).toString() }

    private val vibrate = true
    private val ramping = true

    private var mPlayer: MediaPlayer? = null
    private var mVibrator: Vibrator? = null
    private var mVolumeLevel = 0f

    private var mHandler = Handler()
    private val mVibrationRunnable = object : Runnable {
        override fun run() {
            mVibrator!!.vibrate(DURATION_OF_VIBRATION.toLong())
            // Provide loop for vibration
            mHandler.postDelayed(this,
                    (DURATION_OF_VIBRATION + VIBRATE_DELAY_TIME).toLong())
        }
    }

    private val mVolumeRunnable = object : Runnable {
        override fun run() {
            // increase volume level until reach max value
            if (mPlayer != null && mVolumeLevel < MAX_VOLUME) {
                mVolumeLevel += VOLUME_INCREASE_STEP
                mPlayer!!.setVolume(mVolumeLevel, mVolumeLevel)
                // set next increase in 600ms
                mHandler.postDelayed(this, VOLUME_INCREASE_DELAY.toLong())
            }
        }
    }

    private val mErrorListener = MediaPlayer.OnErrorListener { mp, what, extra ->
        mp.stop()
        mp.release()
        mHandler.removeCallbacksAndMessages(null)
        this@AlarmService.stopSelf()
        true
    }

    override fun onCreate() {
        val ht = HandlerThread("alarm_service")
        ht.start()
        mHandler = Handler(ht.looper)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startPlayer()
        // Start the activity where you can stop alarm
        val i = Intent(Intent.ACTION_MAIN)
        i.component = ComponentName(this, AlarmActivity::class.java!!)
        i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        startActivity(i)
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        if (mPlayer!!.isPlaying) {
            mPlayer!!.stop()
            mPlayer!!.release()
            mPlayer = null
        }
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun startPlayer() {
        mPlayer = MediaPlayer()
        mPlayer!!.setOnErrorListener(mErrorListener)

        try {
            // add vibration to alarm alert if it is set
            if (vibrate) {
                mVibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                mHandler.post(mVibrationRunnable)
            }
            // Player setup is here
            mPlayer!!.setDataSource(this, Uri.parse(ringtone))
            mPlayer!!.isLooping = true
            mPlayer!!.setAudioStreamType(AudioManager.STREAM_ALARM)
            mPlayer!!.setVolume(mVolumeLevel, mVolumeLevel)
            mPlayer!!.prepare()
            mPlayer!!.start()

            if (ramping) {
                mHandler.postDelayed(mVolumeRunnable, VOLUME_INCREASE_DELAY.toLong())
            } else {
                mPlayer!!.setVolume(MAX_VOLUME, MAX_VOLUME)
            }
        } catch (e: Exception) {
            if (mPlayer!!.isPlaying) {
                mPlayer!!.stop()
            }
            stopSelf()
        }

    }

    companion object {
        // Time period between two vibration events
        private val VIBRATE_DELAY_TIME = 2000
        // Vibrate for 1000 milliseconds
        private val DURATION_OF_VIBRATION = 1000
        // Increase alarm volume gradually every 600ms
        private val VOLUME_INCREASE_DELAY = 600
        // Volume level increasing step
        private val VOLUME_INCREASE_STEP = 0.01f
        // Max player volume level
        private val MAX_VOLUME = 1.0f
    }
}
