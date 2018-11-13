package com.example.scott.timesheets_capstone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.scott.timesheets_capstone.model.Contract;
import com.example.scott.timesheets_capstone.model.Timesheet;
import com.example.scott.timesheets_capstone.ui.TimesheetDetailFragment;
import com.google.firebase.auth.FirebaseAuth;

import static com.example.scott.timesheets_capstone.ui.TimesheetDetailFragment.CONTRACT_EXTRA;
import static com.example.scott.timesheets_capstone.ui.TimesheetDetailFragment.TIMESHEET_EXTRA;

/**
 * An activity representing a single Timesheet detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link TimesheetListActivity}.
 */
public class TimesheetDetailActivity extends AppCompatActivity {

    public static final String WIDGET_TIMESHEET_EXTRA = "widget_timesheet_extra";
    public static final String WIDGET_CONTRACT_EXTRA = "widget_contract_extra";


    private Contract mCurrentContract;
    private Timesheet mCurrentTimesheet;
    private Context mContext;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

    DialogInterface.OnClickListener mDialogClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timesheet_detail);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        Intent intentThatStartedThisActivity = getIntent();

        try {
            if (intentThatStartedThisActivity != null) {
                if (intentThatStartedThisActivity.hasExtra(WIDGET_CONTRACT_EXTRA)) {
                    mCurrentContract = intentThatStartedThisActivity.getParcelableExtra(WIDGET_CONTRACT_EXTRA);
                }
                if (intentThatStartedThisActivity.hasExtra(WIDGET_TIMESHEET_EXTRA)) {
                    mCurrentTimesheet = intentThatStartedThisActivity.getParcelableExtra(WIDGET_TIMESHEET_EXTRA);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mContext.getString(R.string.timesheet_edit));
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(CONTRACT_EXTRA,
                    mCurrentContract);
            if (mCurrentTimesheet != null) {
                arguments.putParcelable(TIMESHEET_EXTRA, mCurrentTimesheet);
            }
            TimesheetDetailFragment fragment = new TimesheetDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.timesheet_detail_container, fragment)
                    .commit();
        }

        mDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        mFirebaseAuth.signOut();
                        Intent launchSignInActivity = new Intent(mContext, SignInActivity.class);
                        startActivity(launchSignInActivity);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);
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

        TextView positiveButton = promptView.findViewById(R.id.positiveButton);
        positiveButton.setText(getString(R.string.yes));
        TextView negativeButton = promptView.findViewById(R.id.negativeButton);
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
}
