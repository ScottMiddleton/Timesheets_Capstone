package com.example.scott.timesheets_capstone.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Contract implements Parcelable {

    @SerializedName("approvingManagerEmail")
    @Expose
    private String approvingManagerEmail;
    @SerializedName("approvingManagerName")
    @Expose
    private String approvingManagerName;
    @SerializedName("companyName")
    @Expose
    private String companyName;
    @SerializedName("defaultDays")
    @Expose
    private DefaultDays defaultDays;
    @SerializedName("defaultHrs")
    @Expose
    private double defaultHrs;
    @SerializedName("startDate")
    @Expose
    private String startDate;

    public final static Parcelable.Creator<Contract> CREATOR = new Creator<Contract>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Contract createFromParcel(Parcel in) {
            return new Contract(in);
        }

        public Contract[] newArray(int size) {
            return (new Contract[size]);
        }
    };

    protected Contract(Parcel in) {
        this.companyName = ((String) in.readValue((String.class.getClassLoader())));
        this.approvingManagerName = ((String) in.readValue((String.class.getClassLoader())));
        this.approvingManagerEmail = ((String) in.readValue((String.class.getClassLoader())));
        this.startDate = ((String) in.readValue((String.class.getClassLoader())));
        this.defaultDays = ((DefaultDays) in.readValue((DefaultDays.class.getClassLoader())));
        this.defaultHrs = ((double) in.readValue((Double.class.getClassLoader())));
    }

    public Contract() {
    }

    public String getApprovingManagerEmail() {
        return approvingManagerEmail;
    }

    public void setApprovingManagerEmail(String approvingManagerEmail) {
        this.approvingManagerEmail = approvingManagerEmail;
    }

    public String getApprovingManagerName() {
        return approvingManagerName;
    }

    public void setApprovingManagerName(String approvingManagerName) {
        this.approvingManagerName = approvingManagerName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public DefaultDays getDefaultDays() {
        return defaultDays;
    }

    public void setDefaultDays(DefaultDays defaultDays) {
        this.defaultDays = defaultDays;
    }

    public double getDefaultHrs() {
        return defaultHrs;
    }

    public void setDefaultHrs(double defaultHrs) {
        this.defaultHrs = defaultHrs;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(companyName);
        dest.writeValue(approvingManagerName);
        dest.writeValue(approvingManagerEmail);
        dest.writeValue(startDate);
        dest.writeValue(defaultDays);
        dest.writeValue(defaultHrs);
    }

    public int describeContents() {
        return 0;
    }

}
