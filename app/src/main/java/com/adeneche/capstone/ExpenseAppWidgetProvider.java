package com.adeneche.capstone;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;

public class ExpenseAppWidgetProvider extends AppWidgetProvider{
    public static final String ACTION_UPDATE_TOTAL_SPENT = "update.spent";
    public static final String EXTRA_AMOUNT="amount";

    private double totalSpent = -1;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("AppWidgetProvider", "onUpdate called on " + Arrays.toString(appWidgetIds) +
            " with totalSpent= " + totalSpent);

        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.expense_appwidget);

            Intent intent = new Intent(context, MainActivity.class);
            intent.setAction(MainActivity.ACTION_ADD_EXPENSE);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
            remoteViews.setTextViewText(R.id.widget_text, Utils.formatCurrency(totalSpent));

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (ACTION_UPDATE_TOTAL_SPENT.equals(action)) {
            totalSpent = intent.getDoubleExtra(EXTRA_AMOUNT, -1);
            Log.i("AppWidgetProvider", "update total spent to: " + totalSpent);

            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, ExpenseAppWidgetProvider.class);
            int[] appWidgetIds = mgr.getAppWidgetIds(cn);
            onUpdate(context, mgr, appWidgetIds);
        }

        super.onReceive(context, intent);
    }
}
