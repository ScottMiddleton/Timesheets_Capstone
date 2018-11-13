package com.example.scott.timesheets_capstone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.scott.timesheets_capstone.adapter.TimesheetsListAdapter;
import com.example.scott.timesheets_capstone.model.Contract;
import com.example.scott.timesheets_capstone.model.Timesheet;
import com.example.scott.timesheets_capstone.ui.TimesheetDetailFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static com.example.scott.timesheets_capstone.ContractListActivity.FIREBASE_CHILD_USERS;
import static com.example.scott.timesheets_capstone.TimesheetDetailActivity.WIDGET_CONTRACT_EXTRA;
import static com.example.scott.timesheets_capstone.TimesheetDetailActivity.WIDGET_TIMESHEET_EXTRA;
import static com.example.scott.timesheets_capstone.ui.TimesheetDetailFragment.CONTRACT_EXTRA;
import static com.example.scott.timesheets_capstone.ui.TimesheetDetailFragment.TIMESHEET_EXTRA;

/**
 * An activity representing a list of Timesheets. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TimesheetDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class TimesheetListActivity extends AppCompatActivity
        implements TimesheetsListAdapter.TimesheetOnClickHandler, TimesheetsListAdapter.SendButtonOnClickHandler {

    private final String CURRENT_CONTRACT = "current_contract";
    private final String FIREBASE_CHILD_WEEK_START = "weekStartDate";
    public static final String FIREBASE_CHILD_TIMESHEETS = "timesheets";

    private final TimesheetListActivity mParentActivity = this;
    private ArrayList<Timesheet> mUserTimesheetsList;
    private RecyclerView mRecyclerView;
    private TimesheetsListAdapter mTimesheetsListAdapter;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mTimesheetDatabaseRef;
    private ProgressBar mLoadingIndicator;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Contract mCurrentContract;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentContract = savedInstanceState.getParcelable(CURRENT_CONTRACT);
        }
        setContentView(R.layout.activity_timesheet_list);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        mContext = this;
        mUserTimesheetsList = new ArrayList<>();

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mContext.getString(R.string.timesheets_title));
        }

        Intent intentThatStartedThisActivity = getIntent();

        try {
            if (intentThatStartedThisActivity != null) {
                mCurrentContract = intentThatStartedThisActivity.getParcelableExtra(Intent.EXTRA_TEXT);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ChildEventListener mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Adds all instances of the current users contracts to mUserTimesheets initially.
                // Then updates any changes after that. The mTimesheetsListAdapter is then updated to
                // update the UI.
                Timesheet timesheet = dataSnapshot.getValue(Timesheet.class);
                assert timesheet != null;
                timesheet.setId(dataSnapshot.getKey());
                mUserTimesheetsList.add(0, timesheet);
                mTimesheetsListAdapter.setTimesheetAdapterData(mUserTimesheetsList);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Timesheet updatedTimesheet = dataSnapshot.getValue(Timesheet.class);
                for (int i = 0; i < mUserTimesheetsList.size(); i++) {
                    assert updatedTimesheet != null;
                    if (mUserTimesheetsList.get(i).getId().matches(updatedTimesheet.getId())) {
                        mUserTimesheetsList.remove(i);
                        mUserTimesheetsList.add(i, updatedTimesheet);
                        break;
                    }
                }
                mTimesheetsListAdapter.setTimesheetAdapterData(mUserTimesheetsList);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                for (int i = 0; i < mUserTimesheetsList.size(); i++) {
                    if (mUserTimesheetsList.get(i).getId().matches(Objects.requireNonNull(dataSnapshot.getKey()))) {
                        mUserTimesheetsList.remove(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mFirebaseAuth = FirebaseAuth.getInstance();
        String mUserId = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        Query mTimesheetDatabaseQuery = mFirebaseDatabase.getReference()
                .child(FIREBASE_CHILD_USERS).child(mUserId)
                .child(FIREBASE_CHILD_TIMESHEETS)
                .child(mCurrentContract.getCompanyName())
                .orderByChild(FIREBASE_CHILD_WEEK_START);

        mTimesheetDatabaseQuery.addChildEventListener(mChildEventListener);

        // Find the RecyclerView and set a new @TimesheetListAdapter to it.
        mRecyclerView = findViewById(R.id.timesheet_list);
        mLoadingIndicator = findViewById(R.id.timesheet_list_loading_indicator);
        mTimesheetsListAdapter = new TimesheetsListAdapter(this,
                this,
                mCurrentContract,
                this,
                mLoadingIndicator);

        assert mRecyclerView != null;
        mRecyclerView.setAdapter(mTimesheetsListAdapter);
        setUpItemTouchHelper();


        mTimesheetDatabaseRef = mFirebaseDatabase.getReference()
                .child(FIREBASE_CHILD_USERS)
                .child(mUserId)
                .child(FIREBASE_CHILD_TIMESHEETS)
                .child(mCurrentContract.getCompanyName());


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startTimesheetDetailActivity = new Intent(mContext, TimesheetDetailActivity.class);
                startTimesheetDetailActivity.putExtra(WIDGET_CONTRACT_EXTRA, mCurrentContract);
                startActivity(startTimesheetDetailActivity);
            }
        });

        if (findViewById(R.id.timesheet_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

    }

    @Override
    public void onClick(Timesheet timesheet) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(CONTRACT_EXTRA,
                    mCurrentContract);
            if (timesheet != null) {
                arguments.putParcelable(TIMESHEET_EXTRA, timesheet);
            }
            Fragment fragment = new TimesheetDetailFragment();
            fragment.setArguments(arguments);
            mParentActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.timesheet_detail_container, fragment)
                    .commit();
        } else {
            Intent startTimesheetDetailActivity = new Intent(mContext, TimesheetDetailActivity.class);
            startTimesheetDetailActivity.putExtra(WIDGET_TIMESHEET_EXTRA, timesheet);
            startTimesheetDetailActivity.putExtra(WIDGET_CONTRACT_EXTRA, mCurrentContract);
            startActivity(startTimesheetDetailActivity);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CURRENT_CONTRACT, mCurrentContract);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(getResources().getColor(R.color.delete_red));
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                if (mTimesheetsListAdapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(Objects.requireNonNull(recyclerView), viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                mTimesheetsListAdapter.pendingRemoval(swipedPosition);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
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

    @Override
    public void onSendClick(Timesheet timesheet) {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        if (!timesheet.getSent()){
        timesheet.setSent(true);
        mTimesheetDatabaseRef.child(timesheet.getId()).setValue(timesheet);}
        PDFCreator pdfCreator = new PDFCreator(mContext, timesheet, mCurrentContract, mLoadingIndicator);
        try {
            pdfCreator.generatePdf();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



