package com.example.scott.timesheets_capstone.utilities;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.example.scott.timesheets_capstone.ContractListActivity;
import com.example.scott.timesheets_capstone.R;
import com.example.scott.timesheets_capstone.TimesheetListActivity;
import com.example.scott.timesheets_capstone.model.Contract;
import com.google.gson.Gson;

import static android.content.Context.MODE_PRIVATE;
import static com.example.scott.timesheets_capstone.ui.TimesheetDetailFragment.CONTRACT_SHARED_PREF;
import static com.example.scott.timesheets_capstone.ui.TimesheetDetailFragment.PREFS_NAME;

public class NotificationUtils {

    private static final int TIMESHEET_REMINDER_NOTIFICATION_ID = 100;
    private static final int SUBMIT_TIMESHEET_REMINDER_INTENT_ID = 101;
    private static final String TIMESHEET_REMINDER_NOTIFICATION_CHANNEL_ID = "reminder_notifcation_channel";
    @SuppressLint("StaticFieldLeak")
    public static Context mContext;

    private static PendingIntent getContentIntent(Context context) {
        mContext = context;
        if (getSharedPrefsContract() != null) {
            Intent startTimesheetListActivity = new Intent(context, TimesheetListActivity.class);
            startTimesheetListActivity.putExtra(Intent.EXTRA_TEXT, getSharedPrefsContract());
            return PendingIntent.getActivity(
                    context,
                    SUBMIT_TIMESHEET_REMINDER_INTENT_ID,
                    startTimesheetListActivity,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            Intent startContractListActivity = new Intent(context, ContractListActivity.class);
            startContractListActivity.putExtra(Intent.EXTRA_TEXT, getSharedPrefsContract());
            return PendingIntent.getActivity(
                    context,
                    SUBMIT_TIMESHEET_REMINDER_INTENT_ID,
                    startContractListActivity,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    // This method will create a notification on a weekly basis to remind the user to submit their timehseet.
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void remindUserToSendTimesheet(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel mChannel = new NotificationChannel(
                TIMESHEET_REMINDER_NOTIFICATION_CHANNEL_ID,
                context.getResources().getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH);

        assert notificationManager != null;
        notificationManager.createNotificationChannel(mChannel);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, TIMESHEET_REMINDER_NOTIFICATION_CHANNEL_ID)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        .setSmallIcon(R.drawable.capstone_logo)
                        .setContentTitle(context.getString(R.string.timesheet_reminder_notification_title))
                        .setContentText(context.getString(R.string.timesheet_reminder_notification_body))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(
                                context.getString(R.string.timesheet_reminder_notification_body)))
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setContentIntent(getContentIntent(context))
                        .setAutoCancel(true);

        // If the build version is greater than JELLY_BEAN and lower than OREO,
        // set the notification's priority to PRIORITY_HIGH.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        // Trigger the notification by calling notify on the NotificationManager.
        // Pass in a unique ID of your choosing for the notification and notificationBuilder.build()
        notificationManager.notify(TIMESHEET_REMINDER_NOTIFICATION_ID, notificationBuilder.build());
    }

    private static Contract getSharedPrefsContract() {
        SharedPreferences mPrefs = mContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonContract = mPrefs.getString(CONTRACT_SHARED_PREF, "");
        if (!jsonContract.equals("")) {
            return gson.fromJson(jsonContract, Contract.class);
        } else {
            return null;
        }
    }

}
