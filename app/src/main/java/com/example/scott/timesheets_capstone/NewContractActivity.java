package com.example.scott.timesheets_capstone;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.scott.timesheets_capstone.model.Contract;
import com.example.scott.timesheets_capstone.model.DefaultDays;
import com.example.scott.timesheets_capstone.ui.DayPickerDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Objects;
import static com.example.scott.timesheets_capstone.ContractListActivity.FIREBASE_CHILD_CONTRACTS;
import static com.example.scott.timesheets_capstone.ContractListActivity.FIREBASE_CHILD_USERS;

public class NewContractActivity extends AppCompatActivity implements DayPickerDialog.MyDialogFragmentListener {

    private final String SAVED_STATE_DEFAULT_DAYS = "default_days";
    private final String SAVED_STATE_CONTRACT = "save_contract";
    private final String SAVED_STATE_EDIT = "edit_existing";

    private DatabaseReference mUsersDatabaseRef;

    private Context mContext;

    private String startDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    private Boolean editExistingContract = false;

    private EditText companyNameEdit;
    private EditText managerNameEdit;
    private EditText managerEmailEdit;
    private TextView defaultDailyHoursEdit;
    private TextView defaultDaysEdit;
    private DefaultDays mDefaultDays;
    private TextView startDateEdit;
    private TextView createContractButtontv;
    private Contract mContract;
    private FirebaseAuth mFirebaseAuth;
    private CardView createContractButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_new_contract);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mContext.getString(R.string.create_contract));
        }

        Intent intentThatStartedThisActivity = getIntent();

        try {
            if (intentThatStartedThisActivity != null) {
                if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
                    mContract = intentThatStartedThisActivity.getParcelableExtra(Intent.EXTRA_TEXT);
                    editExistingContract = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (savedInstanceState != null) {
            mDefaultDays = savedInstanceState.getParcelable(SAVED_STATE_DEFAULT_DAYS);
            mContract = savedInstanceState.getParcelable(SAVED_STATE_CONTRACT);
            editExistingContract = savedInstanceState.getBoolean(SAVED_STATE_EDIT);
        }

        mFirebaseAuth = FirebaseAuth.getInstance();
        String mUserId = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();

        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseRef = mFirebaseDatabase.getReference().child(FIREBASE_CHILD_USERS).child(mUserId);

        createContractButton = findViewById(R.id.create_contract_button);
        createContractButtontv = findViewById(R.id.create_contract_button_tv);
        companyNameEdit = findViewById(R.id.company_name_edit);
        managerNameEdit = findViewById(R.id.manager_name_edit);
        managerEmailEdit = findViewById(R.id.manager_email_edit);
        defaultDailyHoursEdit = findViewById(R.id.default_hours_edit);

        defaultDaysEdit = findViewById(R.id.default_days_edit);
        defaultDaysEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDefaultDays != null) {
                    DayPickerDialog.setmDefaultDays(mDefaultDays);
                }
                showDayPickerDialog();
            }
        });

        startDateEdit = findViewById(R.id.start_date_edit);
        startDateEdit.setClickable(true);
        startDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        mContext,
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
                month = month + 1;
                startDate = dayOfMonth + "/" + month + "/" + year;
                startDateEdit.setText(startDate);
            }
        };

        createContractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldsAreEmpty()) {
                    Toast.makeText(mContext, mContext.getString(R.string.please_complete_all_fields), Toast.LENGTH_LONG).show();
                } else if (mDefaultDays == null) {
                    Toast.makeText(mContext, mContext.getString(R.string.please_set_default_days), Toast.LENGTH_LONG).show();
                } else {
                    Contract newContract = createNewContractFromUserInput();
                    mUsersDatabaseRef.child(FIREBASE_CHILD_CONTRACTS).child(newContract.getCompanyName()).setValue(newContract);
                    finish();
                }
            }
        });

        if (mContract != null) {
            companyNameEdit.setText(mContract.getCompanyName());
            managerNameEdit.setText(mContract.getApprovingManagerName());
            managerEmailEdit.setText(mContract.getApprovingManagerEmail());
            mDefaultDays = mContract.getDefaultDays();
            defaultDailyHoursEdit.setText(formatdecimal(mContract.getDefaultHrs()));
            startDateEdit.setText(mContract.getStartDate());
            if(mContract.getDefaultDays() != null){
            setDefaultDaysString(mContract.getDefaultDays());}
            if(editExistingContract){
                createContractButtontv.setText(R.string.update_contract);
            }
        }
    }

    // This method takes the current values from the EditText fields and creates a new Contract Object

    private Contract createNewContractFromUserInput() {
        String companyName = companyNameEdit.getText().toString();
        String managerName = managerNameEdit.getText().toString();
        String managerEmail = managerEmailEdit.getText().toString();

        double defaultHours = 0;
        try {
            defaultHours = Double.parseDouble(defaultDailyHoursEdit.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Contract contract = new Contract();
        contract.setCompanyName(companyName);
        contract.setApprovingManagerName(managerName);
        contract.setApprovingManagerEmail(managerEmail);
        contract.setStartDate(startDateEdit.getText().toString());
        contract.setDefaultDays(mDefaultDays);
        contract.setDefaultHrs(defaultHours);
        return contract;
    }

    private Boolean fieldsAreEmpty() {
        return companyNameEdit.getText().toString().matches("") ||
                managerNameEdit.getText().toString().matches("") ||
                startDateEdit.getText().toString().matches("") ||
                managerEmailEdit.getText().toString().matches("");
    }

    @Override
    public void onReturnValue(DefaultDays defaultDays) {
        mDefaultDays = defaultDays;
        setDefaultDaysString(mDefaultDays);
    }

    void showDayPickerDialog() {

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = new DayPickerDialog();
        newFragment.show(ft, "dialog");
    }

    @Override
    protected void onPause() {
        super.onPause();
        DayPickerDialog.setmDefaultDays(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mContract = createNewContractFromUserInput();
        outState.putParcelable(SAVED_STATE_DEFAULT_DAYS, mDefaultDays);
        outState.putParcelable(SAVED_STATE_CONTRACT, mContract);
        outState.putBoolean(SAVED_STATE_EDIT, editExistingContract);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.standard_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            onBackPressed();
            return true;
        }
        if (id == R.id.logout) {
            customdialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void customdialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View promptView = layoutInflater.inflate(R.layout.alert_dialog, null);

        final AlertDialog alertD = new AlertDialog.Builder(this).create();

        TextView subtitle = promptView.findViewById(R.id.dialogSubtitle);

        subtitle.setText(getString(R.string.are_you_sure));

        Button positiveButton = promptView.findViewById(R.id.positiveButton);
        positiveButton.setText(getString(R.string.yes));
        Button negativeButton = promptView.findViewById(R.id.negativeButton);
        negativeButton.setText(getString(R.string.no));

        positiveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mFirebaseAuth.signOut();
                Intent launchSignInActivity = new Intent(mContext, SignInActivity.class);
                startActivity(launchSignInActivity);
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertD.cancel();
            }
        });

        alertD.setView(promptView);
        alertD.show();

    }

    private void setDefaultDaysString(DefaultDays defaultDays) {
        StringBuilder sb = new StringBuilder();
        if (defaultDays.getMonday()) {
            sb.append(mContext.getString(R.string.mon) + " ");
        }
        if (defaultDays.getTuesday()) {
            sb.append(mContext.getString(R.string.tue) + " ");
        }
        if (defaultDays.getWednesday()) {
            sb.append(mContext.getString(R.string.weds) + " ");
        }
        if (defaultDays.getThursday()) {
            sb.append(mContext.getString(R.string.thurs) + " ");
        }
        if (defaultDays.getFriday()) {
            sb.append(mContext.getString(R.string.fri) + " ");
        }
        if (defaultDays.getSaturday()) {
            sb.append(mContext.getString(R.string.sat) + " ");
        }
        if (defaultDays.getSunday()) {
            sb.append(mContext.getString(R.string.sun) + " ");
        }
        defaultDaysEdit.setText(sb.toString());
    }

    private String formatdecimal(double totalHours) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(false);
        String formattedTotal = format.format(totalHours);
        return formattedTotal.replace(",", "");
    }



}
