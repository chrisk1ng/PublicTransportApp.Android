package com.chrisking.publictransportapp.activities.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.chrisking.publictransportapp.R;

/**
 * The configuration screen for the {@link MyCommuteAppWidget MyCommuteAppWidget} AppWidget.
 */
public class MyCommuteAppWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "com.chrisking.publictransportapp.activities.widget.MyCommuteAppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private SharedPreferences mPrefs;

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = MyCommuteAppWidgetConfigureActivity.this;

            if (mPrefs.getString("homelocation", null) == null || mPrefs.getString("worklocation", null) == null) {
               // Intent clickIntent = new Intent(context, TripPlannerActivity.class);
                //startActivity(clickIntent);
                new AlertDialog.Builder(context)
                        .setTitle("Tip")
                        .setMessage("Before adding the widget, open the main app and navigate to My Commute to complete the setup.")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();

                return;
            }
            else{
                String homeLocation = mPrefs.getString("homelocation", null);
                String workLocation = mPrefs.getString("worklocation", null);

                String[] homeLatlong =  homeLocation.split(",");
                double homeLatitude = Double.parseDouble(homeLatlong[0]);
                double homeLongitude = Double.parseDouble(homeLatlong[1]);

                String[] workLatlong =  workLocation.split(",");
                double workLatitude = Double.parseDouble(workLatlong[0]);
                double workLongitude = Double.parseDouble(workLatlong[1]);

                if (homeLatitude == workLatitude &&
                        homeLongitude == workLongitude){

                    new AlertDialog.Builder(context)
                            .setTitle("Tip")
                            .setMessage("Your work and home locations are the same, please open the main app and navigate to My Commute to change them.")
                            .setPositiveButton(android.R.string.ok, null)
                            .show();

                    return;
                }
            }

            Intent serviceIntent = new Intent(context, WidgetRemoteFetchService.class);
            serviceIntent
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            startService(serviceIntent);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public MyCommuteAppWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        mPrefs = this.getSharedPreferences("settings", 0);

        setContentView(R.layout.my_commute_app_widget_configure);

        findViewById(R.id.getStartedButton).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
    }
}

