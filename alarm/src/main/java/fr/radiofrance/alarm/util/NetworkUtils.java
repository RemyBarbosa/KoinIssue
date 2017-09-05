package fr.radiofrance.alarm.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

public abstract class NetworkUtils {

    /**
     * Return state of network connection
     *
     * @param context
     * @return NetworkInfo.State state
     */
    @NonNull
    public static NetworkInfo.State getNetworkState(final Context context) {
        final ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return NetworkInfo.State.UNKNOWN;
        }

        final NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
        if (networkInfo == null || networkInfo.getState() == null) {
            return NetworkInfo.State.UNKNOWN;
        }
        return networkInfo.getState();
    }

}
