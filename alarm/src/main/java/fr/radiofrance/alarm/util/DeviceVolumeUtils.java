package fr.radiofrance.alarm.util;

import android.content.Context;
import android.media.AudioManager;

import fr.radiofrance.alarm.datastore.prefs.SharedPreferencesManager;
import fr.radiofrance.alarm.model.Alarm;

public abstract class DeviceVolumeUtils {

    private static final String KEY_DEVICE_VOLUME = "rf.alarm.prefs.device.volume";

    /**
     * Returns the volume in a valid value on a specific stream.
     * If the volume is between 0 and {@link #getDeviceMaxVolume(Context, int)}, the volume will not be modified.
     *
     * @param context
     * @param streamType
     * @param alarm
     * @return The new valid volume
     */
    public static int getValidVolume(final Context context, final int streamType, Alarm alarm) {
        return getValidVolume(context, streamType, alarm != null ? alarm.getVolume() : getDeviceMaxVolume(context, streamType));
    }

    /**
     * Returns the volume in a valid value on a specific stream.
     * If the volume is between 0 and {@link #getDeviceMaxVolume(Context, int)}, the volume will not be modified.
     *
     * @param context
     * @param streamType
     * @param volume The volume
     * @return The new valid volume
     */
    public static int getValidVolume(final Context context, final int streamType, int volume) {
        final int maxVolume = getDeviceMaxVolume(context, streamType);
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (volume < 0) {
            volume = audioManager.getStreamVolume(streamType);
        } else if (volume > maxVolume) {
            volume = maxVolume;
        }
        return volume;
    }

    /**
     * Saves the current volume of the device stream.
     */
    public static void saveDeviceVolume(final Context context, final int streamType) {
        new SharedPreferencesManager(context).storeInt(KEY_DEVICE_VOLUME + streamType, getDeviceVolume(context, streamType));
    }

    /**
     * Restores the volume of the device stream previously saved by {@link #saveDeviceVolume(Context, int)}.
     */
    public static void restoreDeviceVolume(final Context context, final int streamType) {
        final int savedVolume = new SharedPreferencesManager(context).getInt(KEY_DEVICE_VOLUME + streamType, 0);
        setDeviceVolume(context, streamType, getValidVolume(context, streamType, savedVolume));
    }

    /**
     * Sets the volume of the device on a specific stream.
     * To know the volume max authorized for this stream, please call {@link #getDeviceMaxVolume(Context, int)}.
     *
     * @param context
     * @param streamType
     * @param volume The volume to set
     */
    public static void setDeviceVolume(final Context context, final int streamType, final int volume) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(streamType, volume, 0);
    }

    /**
     * Gets the volume of the device on a specific stream.
     *
     * @param context
     * @param streamType
     *
     * @return The volume
     */
    public static int getDeviceVolume(final Context context, final int streamType) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamVolume(streamType);
    }

    /**
     * Gets the max volume of the device on a specific stream.
     *
     * @param context
     * @param streamType
     *
     * @return The max volume
     */
    public static int getDeviceMaxVolume(final Context context, final int streamType) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamMaxVolume(streamType);
    }

    /**
     * convertDeviceVolume
     * @param context
     * @param fromStreamType
     * @param toStreamType
     * @param volume
     * @return
     */
    public static int convertDeviceVolume(final Context context, final int fromStreamType, final int toStreamType, final int volume) {
        final int inputValidVolume = getValidVolume(context, fromStreamType, volume);
        final float percent = ((float) inputValidVolume) / ((float) getDeviceMaxVolume(context, fromStreamType));
        return (int) (percent * getDeviceMaxVolume(context, toStreamType));
    }

}
