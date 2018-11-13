package com.example.scott.timesheets_capstone.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.scott.timesheets_capstone.PDFCreator;
import com.example.scott.timesheets_capstone.R;
import com.example.scott.timesheets_capstone.TimesheetListActivity;
import com.example.scott.timesheets_capstone.model.Contract;
import com.example.scott.timesheets_capstone.model.DefaultDays;
import com.example.scott.timesheets_capstone.model.Timesheet;
import com.example.scott.timesheets_capstone.service.UpdateWidgetService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;
import static com.example.scott.timesheets_capstone.ContractListActivity.FIREBASE_CHILD_USERS;
import static com.example.scott.timesheets_capstone.TimesheetListActivity.FIREBASE_CHILD_TIMESHEETS;
import static com.example.scott.timesheets_capstone.adapter.TimesheetsListAdapter.APPROVED;

/**
 * A fragment representing a single Timesheet detail screen.
 * This fragment is either contained in a {@link TimesheetListActivity}
 * in two-pane mode (on tablets) or a {@link TimesheetDetailActivity}
 * on handsets.
 */
public class TimesheetDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String TIMESHEET_EXTRA = "timesheet_extra";
    public static final String CONTRACT_EXTRA = "current_contract_extra";
    public static final String CONTRACT_SHARED_PREF = "lastSavedContract";
    public static final String TIMESHEET_SHARED_PREF = "lastSavedTimesheet";
    public static final String PREFS_NAME = "MyApp_Settings";

    private static Boolean sendEmail = false;
    SharedPreferences mPrefs;

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @BindView(R.id.monday_edit)
    EditText mondayEdit;
    @BindView(R.id.tuesday_edit)
    EditText tuesdayEdit;
    @BindView(R.id.wednesday_edit)
    EditText wednesdayEdit;
    @BindView(R.id.thursday_edit)
    EditText thursdayEdit;
    @BindView(R.id.friday_edit)
    EditText fridayEdit;
    @BindView(R.id.saturday_edit)
    EditText saturdayEdit;
    @BindView(R.id.sunday_edit)
    EditText sundayEdit;
    @BindView(R.id.week_commence_date_tv)
    TextView weekCommencingEdit;
    @BindView(R.id.detail_loading_indicator)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.send_button_tv)
    TextView sendButtonTv;
    @BindView(R.id.send_button)
    CardView sendButton;
    @BindView(R.id.download_button)
    TextView downloadButton;

    private Boolean isNewlyCreatedTimesheet;
    private FirebaseStorage storage;
    private Context mContext;

    private Timesheet mCurrentTimesheet;
    private DatabaseReference mTimesheetDatabaseRef;
    private Contract mCurrentContract;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TimesheetDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentTimesheet = savedInstanceState.getParcelable("timesheet");
            mCurrentContract = savedInstanceState.getParcelable("contract");
            isNewlyCreatedTimesheet = savedInstanceState.getBoolean("newly_Created");
        }

        mContext = this.getContext();
        // If a Timesheet instance is passed as an argument then this is not a newly created Timesheet.
        // Store this in the Boolean variable isNewlyCreatedTimesheet.
        assert getArguments() != null;
        mCurrentContract = getArguments().getParcelable(CONTRACT_EXTRA);
        if (getArguments().getParcelable(TIMESHEET_EXTRA) != null) {
            mCurrentTimesheet = getArguments().getParcelable(TIMESHEET_EXTRA);
            isNewlyCreatedTimesheet = false;
        } else {
            isNewlyCreatedTimesheet = true;
        }


        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        String mUserId = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();
        storage = FirebaseStorage.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

        mTimesheetDatabaseRef = mFirebaseDatabase.getReference()
                .child(FIREBASE_CHILD_USERS)
                .child(mUserId)
                .child(FIREBASE_CHILD_TIMESHEETS)
                .child(mCurrentContract.getCompanyName());

        mPrefs = Objects.requireNonNull(getActivity()).getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.timesheet_detail, container, false);

        setHasOptionsMenu(true);

        ButterKnife.bind(this, rootView);

        AdView mAdView = rootView.findViewById(R.id.detail_adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        MobileAds.initialize(mContext, mContext.getString(R.string.mobile_ads_id));

        if (mCurrentTimesheet != null) {
            if (mCurrentTimesheet.getSent()) {

                sendButtonTv.setText(getResources().getString(R.string.resend));
            } else {
                sendButtonTv.setText(getResources().getString(R.string.send));
            }
            //If the timesheet has been approved them each individual view is made
            //uneditable.
            if (mCurrentTimesheet.getApproved().equals(APPROVED)) {
                mondayEdit.setEnabled(false);
                tuesdayEdit.setEnabled(false);
                wednesdayEdit.setEnabled(false);
                thursdayEdit.setEnabled(false);
                fridayEdit.setEnabled(false);
                saturdayEdit.setEnabled(false);
                sundayEdit.setEnabled(false);
                wednesdayEdit.setEnabled(false);
                sendButton.setVisibility(View.GONE);
                weekCommencingEdit.setEnabled(false);
                downloadButton.setVisibility(View.VISIBLE);
            }
        }

        //If there is an existing Timesheet set the data that has been saved, else
        //set the data from the defaults defined in the Contract.

        if (mCurrentTimesheet != null) {
            setTimesheetData();
        } else {
            setTimesheetDataFromDefaults();
        }

        weekCommencingEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        Objects.requireNonNull(getContext()),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String monthString;
                String dayOfMonthString;
                month = month + 1;

                //if statements place a 0 to before the dayOfMonth and month integer if they are a single digit
                //as they display more uniformly this way in the timesheet list items.
                if (month < 10) {
                    monthString = "0" + Integer.toString(month);
                } else {
                    monthString = Integer.toString(month);
                }
                if (dayOfMonth < 10) {
                    dayOfMonthString = "0" + Integer.toString(dayOfMonth);
                } else {
                    dayOfMonthString = Integer.toString(dayOfMonth);
                }
                String startDate = dayOfMonthString + "/" + monthString + "/" + year;
                weekCommencingEdit.setText(startDate);
            }
        };

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isNewlyCreatedTimesheet) {
                        saveNewTimesheet(true, "null");
                        pdfAndSendTimesheet();
                    } else {
                        saveExistngTimesheet(true, mCurrentTimesheet.getApproved());
                        pdfAndSendTimesheet();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                getActivity().finish();
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                storage.getReference().child("Stamped/" + mCurrentContract.getCompanyName() + "/" + mCurrentTimesheet.getId())
                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        downloadFile(getContext(), "Timesheet_" + mCurrentTimesheet.getId(), "pdf",
                                Objects.requireNonNull(getContext()).getFilesDir().getPath(), uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }
        });

        return rootView;
    }

    private Timesheet createTimesheetFromUserInput(Timesheet timesheet) throws ParseException {
        timesheet.setWeekStartDate(formatDateToMillis(weekCommencingEdit.getText().toString()));

        if (!mondayEdit.getText().toString().equals("")) {
            timesheet.setMondayHrs(Double.parseDouble(mondayEdit.getText().toString()));
        } else {
            timesheet.setMondayHrs(0);
        }
        if (!tuesdayEdit.getText().toString().equals("")) {
            timesheet.setTuesdayHrs(Double.parseDouble(tuesdayEdit.getText().toString()));
        } else {
            timesheet.setTuesdayHrs(0);
        }
        if (!wednesdayEdit.getText().toString().equals("")) {
            timesheet.setWednesdayHrs(Double.parseDouble(wednesdayEdit.getText().toString()));
        } else {
            timesheet.setTuesdayHrs(0);
        }
        if (!thursdayEdit.getText().toString().equals("")) {
            timesheet.setThursdayHrs(Double.parseDouble(thursdayEdit.getText().toString()));
        } else {
            timesheet.setThursdayHrs(0);
        }
        if (!fridayEdit.getText().toString().equals("")) {
            timesheet.setFridayHrs(Double.parseDouble(fridayEdit.getText().toString()));
        } else {
            timesheet.setFridayHrs(0);
        }
        if (!saturdayEdit.getText().toString().equals("")) {
            timesheet.setSaturdayHrs(Double.parseDouble(saturdayEdit.getText().toString()));
        } else {
            timesheet.setFridayHrs(0);
        }
        if (!sundayEdit.getText().toString().equals("")) {
            timesheet.setSundayHrs(Double.parseDouble(sundayEdit.getText().toString()));
        } else {
            timesheet.setSundayHrs(0);
        }
        if (isNewlyCreatedTimesheet) {
            timesheet.setId("null");
            timesheet.setSent(false);
            timesheet.setApproved("null");
        } else {
            timesheet.setId(mCurrentTimesheet.getId());
            timesheet.setSent(mCurrentTimesheet.getSent());
            timesheet.setApproved(mCurrentTimesheet.getApproved());
        }
        return timesheet;
    }

    private void setTimesheetData() {
        weekCommencingEdit.setText(formatDateToString(mCurrentTimesheet.getWeekStartDate()));
        mondayEdit.setText(formatdecimal(mCurrentTimesheet.getMondayHrs()));
        tuesdayEdit.setText(formatdecimal(mCurrentTimesheet.getTuesdayHrs()));
        wednesdayEdit.setText(formatdecimal(mCurrentTimesheet.getWednesdayHrs()));
        thursdayEdit.setText(formatdecimal(mCurrentTimesheet.getThursdayHrs()));
        fridayEdit.setText(formatdecimal(mCurrentTimesheet.getFridayHrs()));
        saturdayEdit.setText(formatdecimal(mCurrentTimesheet.getSaturdayHrs()));
        sundayEdit.setText(formatdecimal(mCurrentTimesheet.getSundayHrs()));
    }

    private void setTimesheetDataFromDefaults() {
        double defaultHours = mCurrentContract.getDefaultHrs();
        DefaultDays defaultDays = mCurrentContract.getDefaultDays();
        if (defaultDays.getMonday()) {
            mondayEdit.setText(formatdecimal(defaultHours));
        } else {
            mondayEdit.setText("0");
        }
        if (defaultDays.getTuesday()) {
            tuesdayEdit.setText(formatdecimal(defaultHours));
        } else {
            tuesdayEdit.setText("0");
        }
        if (defaultDays.getWednesday()) {
            wednesdayEdit.setText(formatdecimal(defaultHours));
        } else {
            wednesdayEdit.setText("0");
        }
        if (defaultDays.getThursday()) {
            thursdayEdit.setText(formatdecimal(defaultHours));
        } else {
            thursdayEdit.setText("0");
        }
        if (defaultDays.getFriday()) {
            fridayEdit.setText(formatdecimal(defaultHours));
        } else {
            fridayEdit.setText("0");
        }
        if (defaultDays.getSaturday()) {
            saturdayEdit.setText(formatdecimal(defaultHours));
        } else {
            saturdayEdit.setText("0");
        }
        if (defaultDays.getSunday()) {
            sundayEdit.setText(formatdecimal(defaultHours));
        } else {
            sundayEdit.setText("0");
        }
    }

    private void saveNewTimesheet(Boolean sent, String approved) throws ParseException {
        Timesheet newTimesheet = new Timesheet();
        if (weekCommencingEdit.getText().toString().matches("")) {
            Toast.makeText(getContext(), getString(R.string.set_date_toast), Toast.LENGTH_LONG).show();
        } else {
            mCurrentTimesheet = createTimesheetFromUserInput(newTimesheet);
            mCurrentTimesheet.setSent(sent);
            mCurrentTimesheet.setApproved(approved);
            DatabaseReference newDatabaseReference = mTimesheetDatabaseRef.push();
            mCurrentTimesheet.setId(newDatabaseReference.getKey());
            newDatabaseReference.setValue(mCurrentTimesheet);
            isNewlyCreatedTimesheet = false;
            Toast.makeText(getContext(), mContext.getString(R.string.timesheet_saved), Toast.LENGTH_SHORT).show();
            saveTimesheetInSharedPrefs();
            UpdateWidgetService.startActionUpdateWidget(mContext);
        }
    }

    private void saveExistngTimesheet(Boolean sent, String approved) throws ParseException {
        mCurrentTimesheet = createTimesheetFromUserInput(mCurrentTimesheet);
        mCurrentTimesheet.setSent(sent);
        mCurrentTimesheet.setApproved(approved);
        mTimesheetDatabaseRef.child(mCurrentTimesheet.getId()).setValue(mCurrentTimesheet);
        Toast.makeText(getContext(), mContext.getString(R.string.timesheet_saved), Toast.LENGTH_SHORT).show();
        saveTimesheetInSharedPrefs();
        UpdateWidgetService.startActionUpdateWidget(mContext);
    }

    private void pdfAndSendTimesheet() {
        isNewlyCreatedTimesheet = false;
        if (weekCommencingEdit.getText().toString().matches("")) {
            Toast.makeText(getContext(), getString(R.string.set_date_toast), Toast.LENGTH_LONG).show();
        } else {
            mLoadingIndicator.setVisibility(View.VISIBLE);
            mCurrentTimesheet.setSent(true);
            PDFCreator pdfCreator = new PDFCreator(mContext, mCurrentTimesheet, mCurrentContract, mLoadingIndicator);
            try {
                pdfCreator.generatePdf();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                sendEmail = false;
                try {
                    if (isNewlyCreatedTimesheet) {
                        saveNewTimesheet(false, "null");
                    } else {
                        saveExistngTimesheet(mCurrentTimesheet.getSent(), mCurrentTimesheet.getApproved());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                getActivity().finish();
                return true;
            default:
                break;
        }
        return false;
    }

    public void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {

        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);

        assert downloadmanager != null;
        downloadmanager.enqueue(request);
    }

    private void saveTimesheetInSharedPrefs() {
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String jsonTimesheet = gson.toJson(mCurrentTimesheet);
        prefsEditor.putString(TIMESHEET_SHARED_PREF, jsonTimesheet);
        String jsonContract = gson.toJson(mCurrentContract);
        prefsEditor.putString(CONTRACT_SHARED_PREF, jsonContract);
        prefsEditor.commit();
    }

    private long formatDateToMillis(String dateString) throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(mContext.getString(R.string.date_format));
        Date date = sdf.parse(dateString);
        return date.getTime();
    }

    private String formatDateToString(long milliSeconds) {
        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat(mContext.getString(R.string.date_format));
        Date date = new Date(milliSeconds);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(date);
    }

    private String formatdecimal(double totalHours) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(false);
        String formattedTotal = format.format(totalHours);
        return formattedTotal.replace(",", "");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (isNewlyCreatedTimesheet) {
            try {
                mCurrentTimesheet = createTimesheetFromUserInput(new Timesheet());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Boolean sent = mCurrentTimesheet.getSent();
                String approved = mCurrentTimesheet.getApproved();
                mCurrentTimesheet = createTimesheetFromUserInput(mCurrentTimesheet);
                mCurrentTimesheet.setSent(sent);
                mCurrentTimesheet.setApproved(approved);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        outState.putParcelable("timesheet", mCurrentTimesheet);
        outState.putParcelable("contract", mCurrentContract);
        outState.putBoolean("newly_Created", isNewlyCreatedTimesheet);
        super.onSaveInstanceState(outState);
    }
}

