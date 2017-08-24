package fr.radiofrance.alarmdemo.model;

import android.os.Parcel;

import fr.radiofrance.alarm.model.Alarm;

public class DemoAlarm extends Alarm {

    public static final Creator<DemoAlarm> CREATOR = new Creator<DemoAlarm>() {

        @Override
        public DemoAlarm createFromParcel(Parcel source) {
            return new DemoAlarm(source);
        }

        @Override
        public DemoAlarm[] newArray(int size) {
            return new DemoAlarm[size];
        }

    };

    private String customField;

    public DemoAlarm() {
        super();
    }

    private DemoAlarm(final Parcel in) {
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
