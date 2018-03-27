package com.chrisking.publictransportapp.activities.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.chrisking.publictransportapp.activities.itinerary.ItineraryViewActivity;
import com.chrisking.publictransportapp.R;

import static com.chrisking.publictransportapp.activities.widget.WidgetListProvider.ADDITIONAL_SETUP;
import static com.chrisking.publictransportapp.activities.widget.WidgetListProvider.DATA_FETCHED;
import static com.chrisking.publictransportapp.activities.widget.WidgetListProvider.ERROR_OR_NO_CONNECTION;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MyCommuteAppWidgetConfigureActivity MyCommuteAppWidgetConfigureActivity}
 */
public class MyCommuteAppWidget extends AppWidgetProvider {

    public static final String REFRESH_TAP = "com.chrisking.publictransportapp.REFRESH_TAP";

    @Override
    public void onUpdate(Context context, AppWidgetManager
            appWidgetManager,int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i = 0; i < N; i++) {
            Intent clickIntent = new Intent(context, ItineraryViewActivity.class);
            clickIntent.setAction(WidgetListProvider.OPEN_ITINERARY);

            PendingIntent clickPI = PendingIntent.getActivity(context, 0,
                    clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            RemoteViews remoteViews = startWidgetRefresh(context, appWidgetIds[i]);
            remoteViews.setPendingIntentTemplate(R.id.listViewWidget, clickPI);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i],
                    R.id.listViewWidget);
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private RemoteViews startWidgetRefresh(Context context, int appWidgetId){
        Intent serviceIntent = new Intent(context, WidgetRemoteFetchService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                appWidgetId);
        context.startService(serviceIntent);

        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.my_commute_app_widget);

        remoteViews.setViewVisibility(R.id.loaderLayout, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.listViewWidget, View.GONE);
        remoteViews.setViewVisibility(R.id.empty_view, View.GONE);
        remoteViews.setViewVisibility(R.id.additional_setup_view, View.GONE);
        remoteViews.setViewVisibility(R.id.error_or_no_connection_view, View.GONE);

        return remoteViews;
    }

    private RemoteViews updateWidgetAdditionalSetup(Context context, int appWidgetId){
        Intent serviceIntent = new Intent(context, WidgetRemoteFetchService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                appWidgetId);
        context.startService(serviceIntent);

        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.my_commute_app_widget);

        remoteViews.setViewVisibility(R.id.loaderLayout, View.GONE);
        remoteViews.setViewVisibility(R.id.listViewWidget, View.GONE);
        remoteViews.setViewVisibility(R.id.empty_view, View.GONE);
        remoteViews.setViewVisibility(R.id.additional_setup_view, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.error_or_no_connection_view, View.GONE);

        Intent intent = new Intent(context, getClass());
        intent.setAction(REFRESH_TAP);
        remoteViews.setOnClickPendingIntent(R.id.refreshImage, PendingIntent.getBroadcast(context, 0, intent, 0));

        return remoteViews;
    }

    private RemoteViews updateWidgetError(Context context, int appWidgetId){
        Intent serviceIntent = new Intent(context, WidgetRemoteFetchService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                appWidgetId);
        context.startService(serviceIntent);

        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.my_commute_app_widget);

        remoteViews.setViewVisibility(R.id.loaderLayout, View.GONE);
        remoteViews.setViewVisibility(R.id.empty_view, View.GONE);
        remoteViews.setViewVisibility(R.id.listViewWidget, View.GONE);
        remoteViews.setViewVisibility(R.id.additional_setup_view, View.GONE);
        remoteViews.setViewVisibility(R.id.error_or_no_connection_view, View.VISIBLE);

        Intent intent = new Intent(context, getClass());
        intent.setAction(REFRESH_TAP);
        remoteViews.setOnClickPendingIntent(R.id.refreshImage, PendingIntent.getBroadcast(context, 0, intent, 0));

        return remoteViews;
    }

    private RemoteViews updateWidgetListView(Context context, int appWidgetId) {
        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.my_commute_app_widget);

        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, WidgetService.class);
        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        //don't know its purpose to me right now
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //setting adapter to listview of the widget
        remoteViews.setRemoteAdapter(R.id.listViewWidget, svcIntent);
        //setting an empty view in case of no data
        remoteViews.setEmptyView(R.id.listViewWidget, R.id.empty_view);
        remoteViews.setViewVisibility(R.id.loaderLayout, View.GONE);
        remoteViews.setViewVisibility(R.id.listViewWidget, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.additional_setup_view, View.GONE);
        remoteViews.setViewVisibility(R.id.error_or_no_connection_view, View.GONE);

        Intent intent = new Intent(context, getClass());
        intent.setAction(REFRESH_TAP);
        remoteViews.setOnClickPendingIntent(R.id.refreshImage, PendingIntent.getBroadcast(context, 0, intent, 0));

        return remoteViews;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName watchWidget = new ComponentName(context, MyCommuteAppWidget.class);

        if (intent.getAction().equals(DATA_FETCHED)) {
            RemoteViews remoteViews = updateWidgetListView(context, appWidgetId);

            appWidgetManager.updateAppWidget(watchWidget, remoteViews);
        }
        else if (intent.getAction().equals(REFRESH_TAP)){
            RemoteViews remoteViews = startWidgetRefresh(context, appWidgetId);

            appWidgetManager.updateAppWidget(watchWidget, remoteViews);
        }
        else if (intent.getAction().equals(ADDITIONAL_SETUP)){
            RemoteViews remoteViews = updateWidgetAdditionalSetup(context, appWidgetId);

            appWidgetManager.updateAppWidget(watchWidget, remoteViews);
        }
        else if (intent.getAction().equals(ERROR_OR_NO_CONNECTION)){
            RemoteViews remoteViews = updateWidgetError(context, appWidgetId);

            appWidgetManager.updateAppWidget(watchWidget, remoteViews);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

