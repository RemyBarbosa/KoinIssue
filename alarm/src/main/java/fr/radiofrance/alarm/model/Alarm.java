package fr.radiofrance.alarm.model;

import android.content.Intent;
import android.support.annotation.NonNull;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Alarm implements Comparable {

    private String id;
    private List<Integer> days;
    private int hours;
    private int minutes;
    private int volume;
    private int snoozeDuration;
    private String intentUri;
    private boolean activated;
    protected int version;
    private String customValue;
    private int customFlags;
    private long fromTimeMs;

    public Alarm() {
        this.id = UUID.randomUUID().toString();
        this.hours = -1;
        this.minutes = -1;
        this.volume = -1;
        this.snoozeDuration = -1;
        this.activated = false;
        this.version = -1;
        this.customValue = null;
        this.customFlags = 0;
        this.fromTimeMs = 0L;
    }

    public String getId() {
        return id;
    }

    @NonNull
    public List<Integer> getDays() {
        if (days == null) {
            days = new ArrayList<>();
        }
        return days;
    }

    /**
     * Sets the days when the alarm will ring.
     *
     * @param days The days: see Day
     */
    public void setDays(final List<Integer> days) {
        this.days = days;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(final int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(final int minutes) {
        this.minutes = minutes;
    }

    public int getVolume() {
        return volume;
    }

    /**
     * Sets the alarm volume. Define it to -1 if you want to keep the default system alarm volume.
     *
     * @param volume The volume of the current alarm.
     */
    public void setVolume(final int volume) {
        this.volume = volume;
    }

    /**
     * Get snoozeDuration in milliseconds
     * @return snoozeDuration in milliseconds
     */
    public int getSnoozeDuration() {
        return snoozeDuration;
    }

    public void setSnoozeDuration(final int snoozeDurationInMillis) {
        this.snoozeDuration = snoozeDurationInMillis;
    }

    public Intent getIntent() {
        if (intentUri == null) {
            return null;
        }

        try {
            return Intent.parseUri(intentUri, 0);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setIntent(final Intent intent) {
        this.intentUri = intent.toUri(0);
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(final boolean activated) {
        this.activated = activated;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    public String getCustomValue() {
        return customValue;
    }

    public void setCustomValue(final String customValue) {
        this.customValue = customValue;
    }

    public int getCustomFlags() {
        return customFlags;
    }

    public void setCustomFlags(final int customFlags) {
        this.customFlags = customFlags;
    }

    public long getFromTimeMs() {
        return fromTimeMs;
    }

    public void setFromTimeMs(final long fromTimeMs) {
        this.fromTimeMs = fromTimeMs;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Alarm{");
        sb.append("id='").append(id).append('\'');
        sb.append(", days=").append(days);
        sb.append(", hours=").append(hours);
        sb.append(", minutes=").append(minutes);
        sb.append(", volume=").append(volume);
        sb.append(", snoozeDuration=").append(snoozeDuration);
        sb.append(", intentUri='").append(intentUri).append('\'');
        sb.append(", activated=").append(activated);
        sb.append(", version=").append(version);
        sb.append(", customValue='").append(customValue).append('\'');
        sb.append(", customFlags=").append(customFlags);
        sb.append(", fromTimeMs=").append(fromTimeMs);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(@NonNull final Object o) {
        if (!(o instanceof Alarm)) {
            return 0;
        }

        final Alarm other = (Alarm) o;
        if (getHours() > other.getHours()) {
            return 1;
        }
        if (getHours() == other.getHours()) {
            if (getMinutes() > other.getMinutes()) {
                return 1;
            } else if (getMinutes() < other.getMinutes()) {
                return -1;
            }
            return 0;
        }
        if (getHours() < other.getHours()) {
            return -1;
        }

        return 0;
    }
}
