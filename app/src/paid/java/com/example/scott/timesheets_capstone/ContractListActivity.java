package com.example.scott.timesheets_capstone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.scott.timesheets_capstone.adapter.ContractsListAdapter;
import com.example.scott.timesheets_capstone.model.Contract;
import com.example.scott.timesheets_capstone.sync.ReminderUtilities;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * An activity representing a list of Contracts.
 */

public class ContractListActivity extends AppCompatActivity
        implements ContractsListAdapter.ContractOnClickHandler, ContractsListAdapter.EditButtonOnClickHandler {

    private ArrayList<Contract> mUserContractsList;
    private RecyclerView mRecyclerView;
    private ContractsListAdapter mContractsListAdapter;
    private Context mContext;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsernameDatabaseRef;
    private FirebaseAuth mFirebaseAuth;
    private ChildEventListener mChildEventListener;
    private String mUserId;

    public static final String FIREBASE_CHILD_USERS = "users";
    public static final String FIREBASE_CHILD_CONTRACTS = "contracts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mUserContractsList = new ArrayList<>();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUserId = mFirebaseAuth.getCurrentUser().getUid();
        mUsernameDatabaseRef = mFirebaseDatabase.getReference().child(FIREBASE_CHILD_USERS).child(mUserId).child(FIREBASE_CHILD_CONTRACTS);

        // Schedule the notification to remind users to submit their timesheet
        ReminderUtilities.scheduleSubmittalReminder(mContext);

        // Set the ContentView and the Toolbar
        setContentView(R.layout.activity_contract_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mContext.getString(R.string.contracts_title));
        }

        // Find the RecyclerView and set a new @ContractsListAdapter to it.
        mRecyclerView = findViewById(R.id.contract_list);
        assert mRecyclerView != null;
        mContractsListAdapter = new ContractsListAdapter(this, this, this);
        mRecyclerView.setAdapter(mContractsListAdapter);

        //Find the FAB and set an onClickListener that launches the NewContractActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startNewContractActivity = new Intent(mContext, NewContractActivity.class);
                startActivity(startNewContractActivity);
            }
        });


        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Adds all instances of the current users contracts to mUserContractsList initially.
                // Then updates any changes after that. The mContractsListAdapter is then updated to
                // update the UI.
                mUserContractsList.add(dataSnapshot.getValue(Contract.class));
                mContractsListAdapter.setAdapterData(mUserContractsList);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Contract updatedContract = dataSnapshot.getValue(Contract.class);
                for (int i = 0; i < mUserContractsList.size(); i++) {
                    assert updatedContract != null;
                    if (mUserContractsList.get(i).getCompanyName().matches(updatedContract.getCompanyName())) {
                        mUserContractsList.remove(i);
                        mUserContractsList.add(i, updatedContract);
                        break;
                    }
                }
                mContractsListAdapter.setAdapterData(mUserContractsList);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mUsernameDatabaseRef.addChildEventListener(mChildEventListener);
    }

    @Override
    public void onClick(Contract contract) {
        Context context = this;
        Intent intent = new Intent(context, TimesheetListActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, contract);
        startActivity(intent);
    }

    @Override
    public void onEditClick(Contract contract) {
        Intent startNewContractActivity = new Intent(mContext, NewContractActivity.class);
        startNewContractActivity.putExtra(Intent.EXTRA_TEXT, contract);
        startActivity(startNewContractActivity);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    // A custom Dialog that requires users to confirm that they want to log out
    private void customdialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.alert_dialog, null);

        final AlertDialog alertD = new AlertDialog.Builder(this).create();

        TextView subtitle = (TextView) promptView.findViewById(R.id.dialogSubtitle);

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