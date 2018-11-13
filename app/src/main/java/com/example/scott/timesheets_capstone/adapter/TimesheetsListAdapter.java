package com.example.scott.timesheets_capstone.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scott.timesheets_capstone.R;
import com.example.scott.timesheets_capstone.model.Contract;
import com.example.scott.timesheets_capstone.model.Timesheet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.view.View.GONE;
import static com.example.scott.timesheets_capstone.ContractListActivity.FIREBASE_CHILD_USERS;
import static com.example.scott.timesheets_capstone.TimesheetListActivity.FIREBASE_CHILD_TIMESHEETS;

public class TimesheetsListAdapter
        extends RecyclerView.Adapter<TimesheetsListAdapter.ViewHolder> {

    public static final String APPROVED = "approved";
    private static final String REJECTED = "rejected";
    public static final String NULL = "null";

    private ArrayList<Timesheet> mTimesheetsArray;
    private List<String> itemsPendingRemoval;
    private final TimesheetOnClickHandler mClickHandler;
    private Context mContext;
    private DatabaseReference mDatabaseRef;
    private SendButtonOnClickHandler mSendButtonClickHandler;
    private ProgressBar mLoadingIndicator;

    private Handler handler = new Handler(); // hanlder for running delayed runnables
    private HashMap<Timesheet, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be

    public TimesheetsListAdapter(Context context, TimesheetOnClickHandler mClickHandler,
                                 Contract contract,
                                 SendButtonOnClickHandler sendButtonClickHandler,
                                 ProgressBar loadingIndicator) {
        mContext = context;
        itemsPendingRemoval = new ArrayList<>();
        this.mClickHandler = mClickHandler;
        mLoadingIndicator = loadingIndicator;
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mSendButtonClickHandler = sendButtonClickHandler;
        String mUserId = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();
        mDatabaseRef = mFirebaseDatabase.getReference()
                .child(FIREBASE_CHILD_USERS)
                .child(mUserId)
                .child(FIREBASE_CHILD_TIMESHEETS)
                .child(contract.getCompanyName());
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mWeekCommenceTv;
        TextView mTotalHoursTv;
        TextView mSendButtonTv;
        ConstraintLayout mDeleteButton;
        CardView mSendbutton;
        ImageView mApprovalStamp;
        ImageView mRejectedStamp;

        ViewHolder(View view) {
            super(view);
            mWeekCommenceTv = view.findViewById(R.id.week_commence_date_tv);
            mTotalHoursTv =  view.findViewById(R.id.total_hours_tv);
            mSendButtonTv = view.findViewById(R.id.list_item_send_button_tv);
            mDeleteButton = view.findViewById(R.id.delete_button);
            mApprovalStamp = view.findViewById(R.id.approval_stamp);
            mRejectedStamp = view.findViewById(R.id.rejected_stamp);
            mSendbutton = view.findViewById(R.id.send_button_cardview);
            mSendbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    Timesheet mCurrentTimesheet = mTimesheetsArray.get(adapterPosition);
                    mSendButtonClickHandler.onSendClick(mCurrentTimesheet);
                }
            });

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Timesheet currentTimesheet = mTimesheetsArray.get(adapterPosition);
            mClickHandler.onClick(currentTimesheet);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.timesheet_list_content;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        final Timesheet timesheetInstance = mTimesheetsArray.get(position);
        String totalHours = getTotalHours(timesheetInstance);
        viewHolder.mTotalHoursTv.setText(totalHours);
        viewHolder.mWeekCommenceTv.setText(formatDateToString(timesheetInstance.getWeekStartDate()));
        if (timesheetInstance.getSent()) {
            viewHolder.mSendButtonTv.setText(mContext.getString(R.string.resend));
        } else {
            viewHolder.mSendButtonTv.setText(mContext.getString(R.string.send));
        }
        if (itemsPendingRemoval.contains(timesheetInstance.getId())) {
            // we need to show the "undo" state of the row
            viewHolder.mDeleteButton.setVisibility(View.VISIBLE);
            viewHolder.mSendbutton.setVisibility(GONE);

            viewHolder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (timesheetInstance.getSent()) {
                        Toast.makeText(mContext, mContext.getString(R.string.toast_cannot_be_deleted), Toast.LENGTH_LONG).show();
                    } else {
                        mDatabaseRef.child(timesheetInstance.getId()).removeValue();
                        remove(mTimesheetsArray.indexOf(timesheetInstance));
                    }
                }
            });
        } else {
            // we need to show the "normal" state
            switch (timesheetInstance.getApproved()) {
                case APPROVED:
                    viewHolder.itemView.setBackgroundColor(Color.WHITE);
                    viewHolder.mApprovalStamp.setVisibility(View.VISIBLE);
                    viewHolder.mRejectedStamp.setVisibility(View.INVISIBLE);
                    viewHolder.mDeleteButton.setVisibility(GONE);
                    viewHolder.mSendbutton.setVisibility(GONE);
                    break;
                case NULL:
                    viewHolder.mApprovalStamp.setVisibility(View.INVISIBLE);
                    viewHolder.mRejectedStamp.setVisibility(View.INVISIBLE);
                    viewHolder.itemView.setBackgroundColor(Color.WHITE);
                    viewHolder.mDeleteButton.setVisibility(GONE);
                    viewHolder.mDeleteButton.setOnClickListener(null);
                    viewHolder.mSendbutton.setVisibility(View.VISIBLE);
                    break;
                case REJECTED:
                    viewHolder.itemView.setBackgroundColor(Color.WHITE);
                    viewHolder.mApprovalStamp.setVisibility(View.INVISIBLE);
                    viewHolder.mRejectedStamp.setVisibility(View.VISIBLE);
                    viewHolder.mDeleteButton.setVisibility(GONE);
                    viewHolder.mSendbutton.setVisibility(GONE);
                    break;
            }
        }
           mLoadingIndicator.setVisibility(GONE);
        }

    @Override
    public int getItemCount() {
        if (mTimesheetsArray != null) {
            return mTimesheetsArray.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void setTimesheetAdapterData(ArrayList<Timesheet> data) {
        mTimesheetsArray = data;
        notifyDataSetChanged();
    }

    private String getTotalHours(Timesheet timesheet) {
        double totalHours = (timesheet.getMondayHrs() +
                timesheet.getTuesdayHrs() +
                timesheet.getWednesdayHrs() +
                timesheet.getThursdayHrs() +
                timesheet.getFridayHrs() +
                timesheet.getSaturdayHrs() +
                timesheet.getSundayHrs());
        return formatdecimal(totalHours);
    }

    private String formatdecimal(double totalHours) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(false);
        return format.format(totalHours);
    }

    public interface TimesheetOnClickHandler {
        void onClick(Timesheet timesheet);
    }

    public boolean isPendingRemoval(int position) {
        Timesheet timesheet = mTimesheetsArray.get(position);
        return itemsPendingRemoval.contains(timesheet.getId());
    }

    public void pendingRemoval(int position) {
        final Timesheet timesheet = mTimesheetsArray.get(position);
        final int mPosition = position;
        if (!itemsPendingRemoval.contains(timesheet.getId())) {
            itemsPendingRemoval.add(timesheet.getId());
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    if (itemsPendingRemoval.contains(timesheet.getId())) {
                        cancelDelete(mPosition);
                    }

                }
            };
            handler.postDelayed(pendingRemovalRunnable, 2000);
            pendingRunnables.put(timesheet, pendingRemovalRunnable);
        }
    }

    private void remove(int position) {
        Timesheet timesheet = mTimesheetsArray.get(position);
        if (itemsPendingRemoval.contains(timesheet.getId())) {
            itemsPendingRemoval.remove(timesheet.getId());
        }
        if (mTimesheetsArray.contains(timesheet)) {
            mTimesheetsArray.remove(position);
            notifyItemRemoved(position);
        }
    }

    public interface SendButtonOnClickHandler {
        void onSendClick(Timesheet timesheet);
    }

    private void cancelDelete(int position) {
        Timesheet timesheetInstance = mTimesheetsArray.get(position);
        Runnable pendingRemovalRunnable = pendingRunnables.get(timesheetInstance);
        pendingRunnables.remove(timesheetInstance);
        if (pendingRemovalRunnable != null) handler.removeCallbacks(pendingRemovalRunnable);
        itemsPendingRemoval.remove(timesheetInstance.getId());
        // this will rebind the row in "normal" state
        notifyItemChanged(mTimesheetsArray.indexOf(timesheetInstance));
    }

    private String formatDateToString(long milliSeconds) {
        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat(mContext.getString(R.string.date_format));
        Date date = new Date(milliSeconds);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(date);
    }

}
