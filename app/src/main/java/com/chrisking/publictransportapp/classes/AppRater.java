package com.chrisking.publictransportapp.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import com.chrisking.publictransportapp.R;

/**
 * Created by ChrisKing on 2017/04/29.
 */

public class AppRater {
    private final static String APP_PNAME = "com.chrisking.publictransportapp"; // Package Name
    private final static int LAUNCHES_UNTIL_PROMPT = 7; // Min number of launches

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
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

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
                showRateDialog(mContext, editor);
        }

        editor.apply();
    }

    private static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {

        new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.rate_title) + " " + mContext.getString(R.string.app_name))
                .setMessage(mContext.getString(R.string.rate_make_user_request_part_1) + " " + mContext.getString(R.string.app_name) + mContext.getString(R.string.rate_make_user_request_part_2))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
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
