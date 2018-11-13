package com.example.scott.timesheets_capstone.sync;

import android.content.Context;

import com.example.scott.timesheets_capstone.service.SubmittalReminderFirebaseJobService;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Trigger;
import java.util.Calendar;
import static com.firebase.jobdispatcher.Lifetime.FOREVER;

public class ReminderUtilities {

    private static final String REMINDER_JOB_TAG = "submittal_reminder_tag";

    private static boolean sInitialized;

    synchronized public static void scheduleSubmittalReminder(final Context context){

        if(sInitialized)return;

        GooglePlayDriver driver = new GooglePlayDriver(context);

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        Calendar now = Calendar.getInstance();
        Calendar friday = Calendar.getInstance();

        //Set the friday calendar to 6pm
        friday.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        friday.set(Calendar.HOUR_OF_DAY, 18);
        friday.set(Calendar.MINUTE, 0);
        friday.set(Calendar.SECOND, 0);
        friday.set(Calendar.MILLISECOND, 0);

        // calculate the time in milliseconds until Friday 6pm
        long diff = friday.getTimeInMillis() - now.getTimeInMillis();
        // if the time difference is less than zero then we need to calculate the time
        // until Friday next week instead
        if (diff < 0) {
            friday.add(Calendar.DAY_OF_WEEK, 7);
            diff = friday.getTimeInMillis() - now.getTimeInMillis();
        }

        int startSeconds = (int) (diff / 1000); // tell the start seconds
        int endSeconds = startSeconds + 300; // within 1 minute

        //Create a new job to send a reminder notification that is triggered every Friday at 6pm
        Job submittalReminderJob = dispatcher.newJobBuilder()
                .setService(SubmittalReminderFirebaseJobService.class)
                .setTag(REMINDER_JOB_TAG)
                .setLifetime(FOREVER)
                .setRecurring(false)
                .setTrigger(Trigger.executionWindow(
                        startSeconds,endSeconds))
                .setReplaceCurrent(true)
                .build();
        dispatcher.schedule(submittalReminderJob);
        sInitialized = true;
    }
}
