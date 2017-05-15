package fr.radiofrance.alarmdemo.model;

import android.os.Parcel;

import fr.radiofrance.alarm.model.Alarm;

public class AlarmModel extends Alarm {

    public static final Creator<AlarmModel> CREATOR = new Creator<AlarmModel>() {

        @Override
        public AlarmModel createFromParcel(Parcel source) {
            return new AlarmModel(source);
        }

        @Override
        public AlarmModel[] newArray(int size) {
            return new AlarmModel[size];
        }

    };

    private String uuid;

    public AlarmModel(String id) {
        super(id);
    }

    private AlarmModel(Parcel in) {
        super(in);
        uuid = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(uuid);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
