package com.example.scott.timesheets_capstone.service;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.example.scott.timesheets_capstone.widget.TimesheetWidgetProvider;

public class UpdateWidgetService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */

    public UpdateWidgetService() {
        super("UpdateWidgetService");
    }

    public static void startActionUpdateWidget(Context context) {
        Intent intent = new Intent(context, UpdateWidgetService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

        //Now update all widgets
        TimesheetWidgetProvider.updateWidgetText(this, appWidgetManager);
    }
}
