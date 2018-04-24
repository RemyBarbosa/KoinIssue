package fr.radiofrance.alarm.service


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.support.v4.app.NotificationCompat
import fr.radiofrance.alarm.R
import fr.radiofrance.alarm.activity.AlarmActivity
import fr.radiofrance.alarm.schedule.AlarmIntentUtils


class AlarmService : Service() {

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

        private val NOTIFICATION_ID = 569754
        private val NOTIFICATION_CHANNEL_ID = "rfalarm_notification_channel"
    }

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

    @SuppressLint("NewApi")
    override fun onCreate() {
        val ht = HandlerThread("alarm_service")
        ht.start()
        mHandler = Handler(ht.looper)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            with(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
                createNotificationChannel(
                        NotificationChannel(NOTIFICATION_CHANNEL_ID,
                                getString(R.string.alarm_notif_label_channel),
                                NotificationManager.IMPORTANCE_LOW)
                                .apply {
                                    enableVibration(false)
                                    enableLights(false)
                                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                                }
                )
            }
        }

        startForeground(NOTIFICATION_ID,
                NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_alarm_notification)
                        .setContentText(getString(R.string.alarm_notif_label))
                        .build()
        )
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startPlayer()
        // Start the activity where you can stop alarm
        startActivity(Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(applicationContext, AlarmActivity::class.java!!)
            setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            putExtra(AlarmIntentUtils.ALARM_CLOCK_AT_TIME_KEY, intent.getLongExtra(AlarmIntentUtils.ALARM_CLOCK_AT_TIME_KEY, 0L))
            putExtra(AlarmIntentUtils.ALARM_CLOCK_DATA_KEY, intent.getBundleExtra(AlarmIntentUtils.ALARM_CLOCK_DATA_KEY))
        })
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

}
