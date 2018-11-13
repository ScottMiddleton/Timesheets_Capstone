package com.example.scott.timesheets_capstone.model;

import android.os.Parcel;
import android.os.Parcelable;

public class DefaultDays implements Parcelable {

    private Boolean friday;
    private Boolean monday;
    private Boolean saturday;
    private Boolean sunday;
    private Boolean thursday;
    private Boolean tuesday;
    private Boolean wednesday;

    public final static Parcelable.Creator<DefaultDays> CREATOR = new Creator<DefaultDays>() {

        @SuppressWarnings({
                "unchecked"
        })
        public DefaultDays createFromParcel(Parcel in) {
            return new DefaultDays(in);
        }

        public DefaultDays[] newArray(int size) {
            return (new DefaultDays[size]);
        }

    };

    protected DefaultDays(Parcel in) {
        this.monday = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
        this.tuesday = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
        this.wednesday = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
        this.thursday = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
        this.friday = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
        this.saturday = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
        this.sunday = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
    }

    public DefaultDays() {
    }

    public Boolean getMonday() {
        return monday;
    }

    public void setMonday(Boolean monday) {
        this.monday = monday;
    }

    public Boolean getTuesday() {
        return tuesday;
    }

    public void setTuesday(Boolean tuesday) {
        this.tuesday = tuesday;
    }

    public Boolean getWednesday() {
        return wednesday;
    }

    public void setWednesday(Boolean wednesday) {
        this.wednesday = wednesday;
    }

    public Boolean getThursday() {
        return thursday;
    }

    public void setThursday(Boolean thursday) {
        this.thursday = thursday;
    }

    public Boolean getFriday() {
        return friday;
    }

    public void setFriday(Boolean friday) {
        this.friday = friday;
    }

    public Boolean getSaturday() {
        return saturday;
    }

    public void setSaturday(Boolean saturday) {
        this.saturday = saturday;
    }

    public Boolean getSunday() {
        return sunday;
    }

    public void setSunday(Boolean sunday) {
        this.sunday = sunday;
    }


    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(monday);
        dest.writeValue(tuesday);
        dest.writeValue(wednesday);
        dest.writeValue(thursday);
        dest.writeValue(friday);
        dest.writeValue(saturday);
        dest.writeValue(sunday);
    }

    public int describeContents() {
        return 0;
    }

}
