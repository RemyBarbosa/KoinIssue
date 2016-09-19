package fr.radiofrance.alarmdemo.model;

import android.os.Parcel;

/**
 * Created by mondon on 09/09/16.
 */
public class Alarm extends fr.radiofrance.alarm.model.Alarm {

    private boolean activated;

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

    public Alarm(String id) {
        super(id);
    }

    protected Alarm(Parcel in) {
        super(in);

        activated = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(activated ? 1 : 0);
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

}
