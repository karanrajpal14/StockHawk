package com.karan.stockhawk.ui.widget;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.karan.stockhawk.R;
import com.karan.stockhawk.data.Contract;
import com.karan.stockhawk.data.PrefUtils;
import com.karan.stockhawk.ui.MainActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class WidgetRemoteViews extends RemoteViewsService {
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat dollarFormat;
    private DecimalFormat percentageFormat;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            Cursor cursor;

            @Override
            public void onCreate() {
                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
                percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");
            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null)
                    if (!cursor.isClosed())
                        cursor.close();

                final long identityToken = Binder.clearCallingIdentity();
                cursor = getContentResolver().query(Contract.Quote.URI, null, null, null, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (cursor != null)
                    cursor.close();
                cursor = null;
            }

            @Override
            public int getCount() {
                if (cursor == null)
                    return 0;
                return cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int i) {
                if (cursor != null && cursor.moveToPosition(i)) {
                    RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);

                    views.setTextViewText(R.id.symbol, cursor.getString(Contract.Quote.POSITION_SYMBOL));
                    views.setTextViewText(R.id.price, dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));

                    float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                    float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                    if (rawAbsoluteChange > 0) {
                        views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                    } else {
                        views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                    }

                    String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                    String percentage = percentageFormat.format(percentageChange / 100);

                    if (PrefUtils.getDisplayMode(getApplicationContext()).equals(getString(R.string.pref_display_mode_absolute_key))) {
                        views.setTextViewText(R.id.change, change);
                    } else {
                        views.setTextViewText(R.id.change, percentage);
                    }

                    Intent tapIntent = new Intent(getApplicationContext(), MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, tapIntent, 0);
                    views.setOnClickPendingIntent(R.id.list_item_quote, pendingIntent);

                    return views;
                } else {
                    return null;
                }
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int i) {
                if (cursor != null && cursor.moveToPosition(i)) {
                    return cursor.getColumnIndex(Contract.Quote._ID);
                } else {
                    return i;
                }
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
