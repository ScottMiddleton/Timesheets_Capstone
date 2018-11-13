package com.example.scott.timesheets_capstone.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.example.scott.timesheets_capstone.sync.ReminderTasks;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SubmittalReminderFirebaseJobService extends com.firebase.jobdispatcher.JobService{
    private AsyncTask mBackgroundTask;

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(final com.firebase.jobdispatcher.JobParameters job) {
        mBackgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Context context = SubmittalReminderFirebaseJobService.this;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ReminderTasks.executeTask(context, ReminderTasks.ACTION_SUMBMITTAL_REMINDER);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                jobFinished(job, false);
            }
        };
        mBackgroundTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        if(mBackgroundTask != null)
            mBackgroundTask.cancel(true);
        return true;
    }
}
