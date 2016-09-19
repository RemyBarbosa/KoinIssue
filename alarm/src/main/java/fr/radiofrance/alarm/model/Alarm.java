package fr.radiofrance.alarm.model;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import fr.radiofrance.alarm.type.Day;

/**
 * Created by mondon on 09/09/16.
 */
public class Alarm implements Parcelable {

    protected String id;
    protected List<Day> days;
    protected int hours;
    protected int minutes;
    protected int volume;
    protected int snoozeDuration;
    protected String intentUri;

    public static final Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {

        @Override
        public Alarm createFromParcel(Parcel source) {
            return new Alarm(source);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }

    };

    public Alarm(String id) {
        this.id = id;
        this.volume = -1;
        this.hours = -1;
        this.minutes = -1;
        this.snoozeDuration = -1;
    }

    protected Alarm(Parcel in) {
        this.id = in.readString();
        this.days = new ArrayList<>();
        in.readList(this.days, Day.class.getClassLoader());
        this.hours = in.readInt();
        this.minutes = in.readInt();
        this.volume = in.readInt();
        this.snoozeDuration = in.readInt();
        this.intentUri = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeList(this.days);
        dest.writeInt(this.hours);
        dest.writeInt(this.minutes);
        dest.writeInt(this.volume);
        dest.writeInt(this.snoozeDuration);
        dest.writeString(this.intentUri);
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
                '}';
    }

    public String getId() {
        return id;
    }

    public List<Day> getDays() {
        return days;
    }

    /**
     * Sets the days when the alarm will ring.
     * @param days The days: see Day
     */
    public void setDays(List<Day> days) {
        this.days = days;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
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
    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getSnoozeDuration() {
        return snoozeDuration;
    }

    public void setSnoozeDuration(int snoozeDurationInMillis) {
        this.snoozeDuration = snoozeDurationInMillis;
    }

    public Intent getIntent() {
        if (intentUri == null) return null;

        try {
            return Intent.parseUri(intentUri, 0);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setIntent(Intent intent) {
        this.intentUri = intent.toUri(0);
    }

}
