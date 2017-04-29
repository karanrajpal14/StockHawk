package com.karan.stockhawk.ui.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.karan.stockhawk.R;
import com.karan.stockhawk.ui.MainActivity;

public class WidgetService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WidgetService(String name) {
        super(name);
    }

    public WidgetService() {
        super(WidgetService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));

        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget_layout);
            remoteViews.setRemoteAdapter(R.id.widget_listView, new Intent(this, WidgetRemoteViews.class));

            Intent tapIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, tapIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.list_item_quote, pendingIntent);

            remoteViews.setEmptyView(R.id.widget_listView, R.id.widget_empty_textView);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_listView);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
