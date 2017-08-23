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

    private String customField;

    private AlarmModel(final Parcel in) {
        super(in);
        customField = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(customField);
    }

    public String getCustomField() {
        return customField;
    }

    public void setCustomField(final String customField) {
        this.customField = customField;
    }

}
