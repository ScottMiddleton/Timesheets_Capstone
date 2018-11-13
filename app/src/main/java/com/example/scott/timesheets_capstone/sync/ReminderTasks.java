package com.example.scott.timesheets_capstone.sync;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.example.scott.timesheets_capstone.utilities.NotificationUtils;

public class ReminderTasks {

    public static final String ACTION_SUMBMITTAL_REMINDER = "submittal_reminder";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void executeTask(Context context, String action){
        if(ACTION_SUMBMITTAL_REMINDER.equals(action)){
            issueSubmittalReminder(context);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void issueSubmittalReminder(Context context){
        NotificationUtils.remindUserToSendTimesheet(context);
    }
}
