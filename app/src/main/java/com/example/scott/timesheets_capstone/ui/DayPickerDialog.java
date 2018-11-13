package com.example.scott.timesheets_capstone.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;
import com.example.scott.timesheets_capstone.R;
import com.example.scott.timesheets_capstone.model.DefaultDays;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DayPickerDialog extends DialogFragment {

    @BindView(R.id.tD) ToggleButton tD;
    @BindView(R.id.tL) ToggleButton tL;
    @BindView(R.id.tM) ToggleButton tM;
    @BindView(R.id.tMi) ToggleButton tMi;
    @BindView(R.id.tJ) ToggleButton tJ;
    @BindView(R.id.tV) ToggleButton tV;
    @BindView(R.id.tS) ToggleButton tS;
    @BindView(R.id.set_days_button) CardView mSetDaysButton;

    private static DefaultDays mDefaultDays;
    private Boolean mondaySelected = false;
    private Boolean tuesdaySelected= false;
    private Boolean wednesdaySelected= false;
    private Boolean thursdaySelected= false;
    private Boolean fridaySelected= false;
    private Boolean saturdaySelected= false;
    private Boolean sundaySelected= false;

    public DayPickerDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_day_picker, container, false);

        ButterKnife.bind(this, v);

        mSetDaysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDefaultDays = getDefaultDays();
                MyDialogFragmentListener activity = (MyDialogFragmentListener) getActivity();
                activity.onReturnValue(mDefaultDays);
                dismiss();
            }
        });

        if (mDefaultDays != null) {
            if (mDefaultDays.getMonday()) {
                tD.setChecked(true);
            }
            if (mDefaultDays.getTuesday()) {
                tL.setChecked(true);
            }
            if (mDefaultDays.getWednesday()) {
                tM.setChecked(true);
            }
            if (mDefaultDays.getThursday()) {
                tMi.setChecked(true);
            }
            if (mDefaultDays.getFriday()) {
                tJ.setChecked(true);
            }
            if (mDefaultDays.getSaturday()) {
                tV.setChecked(true);
            }
            if (mDefaultDays.getSunday()) {
                tS.setChecked(true);
            }
        }

        return v;
    }

    private DefaultDays getDefaultDays() {
        if (tD.isChecked()) {
            mondaySelected = true;
        }
        if (tL.isChecked()) {
            tuesdaySelected = true;
        }
        if (tM.isChecked()) {
            wednesdaySelected = true;
        }
        if (tMi.isChecked()) {
            thursdaySelected = true;
        }
        if (tJ.isChecked()) {
            fridaySelected = true;
        }
        if (tV.isChecked()) {
            saturdaySelected = true;
        }
        if (tS.isChecked()) {
            sundaySelected = true;
        }

        DefaultDays defaultDays = new DefaultDays();
        defaultDays.setMonday(mondaySelected);
        defaultDays.setTuesday(tuesdaySelected);
        defaultDays.setWednesday(wednesdaySelected);
        defaultDays.setThursday(thursdaySelected);
        defaultDays.setFriday(fridaySelected);
        defaultDays.setSaturday(saturdaySelected);
        defaultDays.setSunday(sundaySelected);
        return defaultDays;
    }

    public interface MyDialogFragmentListener {
        void onReturnValue(DefaultDays defaultDays);
    }

    public static void setmDefaultDays(DefaultDays defaultDays){
        mDefaultDays = defaultDays;
    }

}

