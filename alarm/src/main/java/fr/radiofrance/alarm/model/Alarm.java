package fr.radiofrance.alarm.model;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Alarm implements Parcelable {

    public static final Creator<Alarm> CREATOR = new Creator<Alarm>() {

        @Override
        public Alarm createFromParcel(Parcel source) {
            return new Alarm(source);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }

    };

    protected String id;
    protected List<Integer> days;
    protected int hours;
    protected int minutes;
    protected int volume;
    protected int snoozeDuration;
    protected String intentUri;
    protected boolean activated;

    public Alarm(String id) {
        this.id = id;
        this.hours = -1;
        this.minutes = -1;
        this.volume = -1;
        this.snoozeDuration = -1;
        this.activated = false;
    }

    protected Alarm(final Parcel in) {
        this.id = in.readString();
        this.days = new ArrayList<>();
        in.readList(this.days, Integer.class.getClassLoader());
        this.hours = in.readInt();
        this.minutes = in.readInt();
        this.volume = in.readInt();
        this.snoozeDuration = in.readInt();
        this.intentUri = in.readString();
        this.activated = in.readByte() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(this.id);
        dest.writeList(this.days);
        dest.writeInt(this.hours);
        dest.writeInt(this.minutes);
        dest.writeInt(this.volume);
        dest.writeInt(this.snoozeDuration);
        dest.writeString(this.intentUri);
        dest.writeByte(this.activated ? (byte) 1 : (byte) 0);
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "id='" + id + "'" +
                ", days=" + days +
                ", hours=" + hours +
                ", minutes=" + minutes +
                ", volume=" + volume +
                ", snoozeDuration=" + snoozeDuration +
                ", intentUri='" + intentUri + "'" +
                ", isActivated='" + activated + "'" +
                '}';
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

}
