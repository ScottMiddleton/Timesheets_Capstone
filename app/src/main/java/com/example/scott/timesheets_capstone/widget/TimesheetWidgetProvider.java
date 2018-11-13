package com.example.scott.timesheets_capstone.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import com.example.scott.timesheets_capstone.ContractListActivity;
import com.example.scott.timesheets_capstone.R;
import com.example.scott.timesheets_capstone.model.Timesheet;
import com.google.gson.Gson;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import static android.content.Context.MODE_PRIVATE;
import static com.example.scott.timesheets_capstone.ui.TimesheetDetailFragment.PREFS_NAME;
import static com.example.scott.timesheets_capstone.ui.TimesheetDetailFragment.TIMESHEET_SHARED_PREF;
/**
 * Implementation of App Widget functionality.
 */
public class TimesheetWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager) {

        Timesheet timesheet = getSharedPrefsTimesheet(context);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timesheet_widget_provider);
        assert timesheet != null;
        views.setTextViewText(R.id.week_commence_date_tvw, formatDateToString(timesheet.getWeekStartDate()));
        views.setTextViewText(R.id.total_hours_tvw, getTotalHours(timesheet));

        Intent intent = new Intent().setClass(context, ContractListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(new ComponentName(context.getPackageName(),TimesheetWidgetProvider.class.getName()), views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager);
        }
    }

    public static void updateWidgetText(Context context, AppWidgetManager appWidgetManager) {
        Log.e("Widget Provider", "updateWidgetText");
        updateAppWidget(context, appWidgetManager);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private static Timesheet getSharedPrefsTimesheet(Context mContext) {
        SharedPreferences mPrefs = mContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonTimesheet = mPrefs.getString(TIMESHEET_SHARED_PREF, "");
        if (!jsonTimesheet.equals("")) {
            return gson.fromJson(jsonTimesheet, Timesheet.class);
        } else {
            return null;
        }
    }

    private static String getTotalHours(Timesheet timesheet) {
        double totalHours = (timesheet.getMondayHrs() +
                timesheet.getTuesdayHrs() +
                timesheet.getWednesdayHrs() +
                timesheet.getThursdayHrs() +
                timesheet.getFridayHrs() +
                timesheet.getSaturdayHrs() +
                timesheet.getSundayHrs());
        return formatdecimal(totalHours);
    }

    private static String formatdecimal(double totalHours) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(false);
        return format.format(totalHours);
    }

    public static String formatDateToString(long milliSeconds) {
        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("dd/mm/yyyy");
        Date date = new Date(milliSeconds);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(date);
    }
}

