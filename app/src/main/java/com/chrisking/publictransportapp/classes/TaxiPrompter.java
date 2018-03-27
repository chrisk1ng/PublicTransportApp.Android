package com.chrisking.publictransportapp.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.queues.TaxiQueues;
import com.chrisking.publictransportapp.classes.City;

/**
 * Created by ChrisKing on 2017/07/08.
 */

public class TaxiPrompter {
    private final static String APP_PNAME = "com.chrisking.publictransportapp";// Package Name

    private final static int LAUNCHES_UNTIL_PROMPT = 5;//Min number of launches

    public static void app_launched(Context mContext, FragmentManager fragmentManager) {


        /*City savedCity = new CityPersistence(mContext).getSavedCity();

        // If selected city doesn't have informal can stop here.
        if (!savedCity.getHasInformal()) { return ; }

        // If taxi queues has already been setup, can stop here.
        SharedPreferences settings = mContext.getSharedPreferences("settings", 0);
        if (settings.getBoolean(TaxiQueues.TAXI_QUEUES_SETUP_STORAGE_KEY, false)) { return; }

        // If use has elected out of the prompt, can stop here.
        SharedPreferences prefs = mContext.getSharedPreferences("taxiprompter", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;

        if (launch_count > LAUNCHES_UNTIL_PROMPT)
        {
            editor.putLong("launch_count", 0);
            launch_count = 0;
        }
        else
            editor.putLong("launch_count", launch_count);

        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            showTaxiQueuesDialog(mContext, fragmentManager, editor, savedCity);
        }

        editor.commit();*/
    }
    public static void showTaxiQueuesDialog(final Context mContext, final FragmentManager fragmentManager, final SharedPreferences.Editor editor, City city) {

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.taxiqueues_prompt_header)
                .setMessage(city.getName() + " " + city.getTaxiName() + mContext.getString(R.string.taxiqueues_prompt_description))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        fragmentManager.beginTransaction()
                            .add(R.id.content_frame, new TaxiQueues())
                            .addToBackStack(mContext.getString(R.string.title_activity_taxi_queues))
                            .commit();

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.do_not_show, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();

                        dialog.dismiss();
                    }
                })
                .setNeutralButton(R.string.not_now, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
