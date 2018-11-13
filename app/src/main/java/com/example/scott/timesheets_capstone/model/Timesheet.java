package com.example.scott.timesheets_capstone.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Timesheet implements Parcelable {
    @SerializedName("monday_hrs")
    @Expose
    private double mondayHrs;
    @SerializedName("tuesday_hrs")
    @Expose
    private double tuesdayHrs;
    @SerializedName("wednesday_hrs")
    @Expose
    private double wednesdayHrs;
    @SerializedName("thursday_hrs")
    @Expose
    private double thursdayHrs;
    @SerializedName("friday_hrs")
    @Expose
    private double fridayHrs;
    @SerializedName("saturday_hrs")
    @Expose
    private double saturdayHrs;
    @SerializedName("sunday_hrs")
    @Expose
    private double sundayHrs;
    @SerializedName("start_date")
    @Expose
    private long weekStartDate;
    @SerializedName("sent")
    @Expose
    private Boolean sent;
    @SerializedName("approved")
    @Expose
    private String approved;
    @SerializedName("id")
    @Expose
    private String id;

    public final static Parcelable.Creator<Timesheet> CREATOR = new Creator<Timesheet>() {

        @SuppressWarnings({
                "unchecked"
        })
        public Timesheet createFromParcel(Parcel in) {
            return new Timesheet(in);
        }

        public Timesheet[] newArray(int size) {
            return (new Timesheet[size]);
        }

    };

    public Timesheet() {
    }

    public Timesheet(Parcel in) {
        this.weekStartDate = ((long) in.readValue((long.class.getClassLoader())));
        this.mondayHrs = ((double) in.readValue((double.class.getClassLoader())));
        this.tuesdayHrs = ((double) in.readValue((double.class.getClassLoader())));
        this.wednesdayHrs = ((double) in.readValue((double.class.getClassLoader())));
        this.thursdayHrs = ((double) in.readValue((double.class.getClassLoader())));
        this.fridayHrs = ((double) in.readValue((double.class.getClassLoader())));
        this.saturdayHrs = ((double) in.readValue((double.class.getClassLoader())));
        this.sundayHrs = ((double) in.readValue((double.class.getClassLoader())));
        this.sent = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
        this.approved = ((String) in.readValue((String.class.getClassLoader())));
        this.id = ((String) in.readValue((String.class.getClassLoader())));
    }

    public long getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(long startDate) {
        this.weekStartDate = startDate;
    }

    public double getMondayHrs() {
        return mondayHrs;
    }

    public void setMondayHrs(double mondayHrs) {
        this.mondayHrs = mondayHrs;
    }

    public double getTuesdayHrs() {
        return tuesdayHrs;
    }

    public void setTuesdayHrs(double tuesdayHrs) {
        this.tuesdayHrs = tuesdayHrs;
    }

    public double getWednesdayHrs() {
        return wednesdayHrs;
    }

    public void setWednesdayHrs(double wednesdayHrs) {
        this.wednesdayHrs = wednesdayHrs;
    }

    public double getThursdayHrs() {
        return thursdayHrs;
    }

    public void setThursdayHrs(double thursdayHrs) {
        this.thursdayHrs = thursdayHrs;
    }

    public double getFridayHrs() {
        return fridayHrs;
    }

    public void setFridayHrs(double fridayHrs) {
        this.fridayHrs = fridayHrs;
    }

    public double getSaturdayHrs() {
        return saturdayHrs;
    }

    public void setSaturdayHrs(double saturdayHrs) {
        this.saturdayHrs = saturdayHrs;
    }

    public double getSundayHrs() {
        return sundayHrs;
    }

    public void setSundayHrs(double sundayHrs) {
        this.sundayHrs = sundayHrs;
    }

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    public String getApproved() {
        return approved;
    }

    public void setApproved(String approved) {
        this.approved = approved;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(weekStartDate);
        dest.writeValue(mondayHrs);
        dest.writeValue(tuesdayHrs);
        dest.writeValue(wednesdayHrs);
        dest.writeValue(thursdayHrs);
        dest.writeValue(fridayHrs);
        dest.writeValue(saturdayHrs);
        dest.writeValue(sundayHrs);
        dest.writeValue(sent);
        dest.writeValue(approved);
        dest.writeValue(id);
    }

    public int describeContents() {
        return 0;
    }

}
