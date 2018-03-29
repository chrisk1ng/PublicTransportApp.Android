package com.chrisking.publictransportapp.helpers;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import com.chrisking.publictransportapp.R;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ChrisKing on 2017/04/22.
 */

public final class Shortcuts {
    public final static String APP_PNAME = "com.chrisking.publictransportapp"; // Package Name

    public static void displayRegionUnSupportedTip(final Activity activity){
        new AlertDialog.Builder(activity)
                .setTitle(R.string.not_supported_tip_title)
                .setMessage(R.string.not_supported_tip_description)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(R.string.not_supported_tip_app_store, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                    }
                })
                .show();
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static Date convertIsoDateTimeStringToDate(String isoDateTime)
    {
        if (isNullOrWhitespace(isoDateTime))
            return null;

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        try
        {
            return dateFormatter.parse(isoDateTime);
        }
        catch (ParseException pe) {
            return null;
        }
    }

    public static String convertDateToIsoDateTimeString(Date date){
        SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT);
        outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        return outputFmt.format(date);
    }

    public static String mapModeToText(String mode) {
        switch (mode.toLowerCase()){
            case "bus":
                return ApplicationExtension.getContext().getResources().getString(R.string.mode_text_bus);
            case "air":
                return ApplicationExtension.getContext().getResources().getString(R.string.mode_text_air);
            case "coach":
                return ApplicationExtension.getContext().getResources().getString(R.string.mode_text_coach);
            case "ferry":
                return ApplicationExtension.getContext().getResources().getString(R.string.mode_text_ferry);
            case "lightrail":
                return ApplicationExtension.getContext().getResources().getString(R.string.mode_text_lightrail);
            case "rail":
                return ApplicationExtension.getContext().getResources().getString(R.string.mode_text_rail);
            case "sharetaxi":
                return ApplicationExtension.getContext().getResources().getString(R.string.mode_text_sharetaxi);
            case "subway":
                return ApplicationExtension.getContext().getResources().getString(R.string.mode_text_subway);
            case "walk":
                return ApplicationExtension.getContext().getResources().getString(R.string.mode_text_walk);
            default:
                return ApplicationExtension.getContext().getResources().getString(R.string.mode_text_default);
        }
    }

    public static int mapModeImage72(String mode) {
        switch (mode.toLowerCase()){
            case "bus":
                return R.drawable.bus72;
            case "air":
                return R.drawable.air72;
            case "coach":
                return R.drawable.coach72;
            case "ferry":
                return R.drawable.ferry72;
            case "lightrail":
                return R.drawable.lightrail72;
            case "rail":
                return R.drawable.rail72;
            case "sharetaxi":
                return R.drawable.sharetaxi72;
            case "subway":
                return R.drawable.subway72;
            case "walk":
                return R.drawable.walk72;
            default:
                return R.drawable.default72;
        }
    }

    public static int mapModeImage24(String mode) {
        switch (mode.toLowerCase()){
            case "bus":
                return R.drawable.bus24;
            case "air":
                return R.drawable.air24;
            case "coach":
                return R.drawable.coach24;
            case "ferry":
                return R.drawable.ferry24;
            case "lightrail":
                return R.drawable.lightrail24;
            case "rail":
                return R.drawable.rail24;
            case "sharetaxi":
                return R.drawable.sharetaxi24;
            case "subway":
                return R.drawable.subway24;
            case "walk":
                return R.drawable.walk24;
            default:
                return R.drawable.default24;
        }
    }

    public static String isoDateTimeStringToTime(String isoDateTime)
    {
        Date date = convertIsoDateTimeStringToDate(isoDateTime);

        SimpleDateFormat simpleDate = new SimpleDateFormat("HH:mm", Locale.ROOT);

        return simpleDate.format(date); //.replace(":", "h");
    }

    public static String timeUntil(String isoDateTime)
    {
        if (isNullOrWhitespace(isoDateTime))
            return "";

        Date date = convertIsoDateTimeStringToDate(isoDateTime);

        if (date == null)
            return "";

        long difference = date.getTime() - new Date().getTime();
        long seconds = difference / 1000;
        final int dayInSeconds = 86400;

        int years = (int) (seconds / (dayInSeconds*365));
        int months = (int) (seconds / (dayInSeconds*30));
        int weeks = (int) (seconds / (dayInSeconds*7));
        int days = (int) (seconds / dayInSeconds);
        int hours = (int) (seconds / (dayInSeconds / 24));
        int minutes = (int) (seconds / (dayInSeconds / 24 / 60));

        if (years > 0) {
            if (years > 2)
                return "in " + years + " years";
            else
                return "next year";
        }
        else if (months > 0) {
            if (months > 2)
                return "in " + months + " months";
            else
                return "next month";
        }
        else if (weeks > 0) {
            if (weeks > 2)
                return "in " + weeks + " weeks";
            else
                return "next week";
        }
        else if (days > 0) {
            if (days > 2)
                return "in " + days + " days";
            else if (days >= 1) {
                if (hours >= 10)
                    return "in a day and a half";
                else
                    return "tomorrow";
            }
        }
        else if (hours >= 2) {
            return "in " + hours + " hours";
        }
        else if (hours >= 1) {
            if (minutes > 30)
                return "in 1 hour " + (minutes - 60) + " minutes";
            else
                return "in about 1 hour";
        }
        else if (minutes >= 2) {
            return "in " + minutes + " minutes";
        }
        else if (minutes >= 1) {
            return "in a minute";
        }

        else if (years < 0) {
            if (years < -2)
                return years * -1 + " years ago";
            else
                return "last year";
        }
        else if (months < 0) {
            if (months < -2)
                return months * -1 + " months ago";
            else
                return "last month";
        }
        else if (weeks < 0) {
            if (weeks < -2)
                return weeks * -1 + " weeks ago";
            else
                return "last week";
        }
        else if (days < 0) {
            if (days < -2)
                return days * -1 + " days ago";
            else if (days <= -1) {
                if (hours <= -10)
                    return "a day and a half ago";
                else
                    return "yesterday";
            }
        }
        else if (hours <= -2) {
            return hours * -1 + " hours ago";
        }
        else if (hours <= -1) {
            if (minutes < -30)
                return "1 hour " + ((minutes * -1) - 60) + " minutes ago";
            else
                return "about 1 hour ago";
        }
        else if (minutes <= -2) {
            return minutes * -1 + " minutes ago";
        }
        else if (minutes < 0) {
            return "a minute ago";
        }

        return "now";
    }

    public static boolean userIsHome(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        return !(hour <= 20 && hour >= 10);
    }

    public static boolean colorIsBright(int color){
        if(android.R.color.transparent == color) return true;

        int[] rgb = {Color.red(color), Color.green(color), Color.blue(color)};

        int brightness =
                (int)Math.sqrt(
                        rgb[0] * rgb[0] * .299 +
                                rgb[1] * rgb[1] * .587 +
                                rgb[2] * rgb[2] * .114);

        return brightness >= 210;
    }

    public static boolean isNightTime(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        return !(hour <= 19 && hour >= 7);
    }

    public static boolean isNullOrWhitespace(String s) {
        return s == null || isWhitespace(s);
    }

    private static boolean isWhitespace(String s) {
        int length = s.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static int MapOperatorNameToGuideName(String taxiName) {
        switch (taxiName.toLowerCase()){

            case "dar es salaam taxis":
            case "daladala":
                //TODO: Dar es Salaam
                return R.raw.generic;
            case "combi":
            case "gabarone kombi's":
                //TODO: Gabarone Combis
                return R.raw.generic;
            case "kigali taxis":
                //TODO: Kigali Taxis
                return R.raw.generic;
            case "minibus":
            case "lusaka taxi project":
            case "ma bus":
                //TODO: Lusaka Taxis
                return R.raw.generic;
            case "trotro":
            case "g.p.r.t.u. (accra)":
                //TODO: Accra Taxis
                return R.raw.generic;
            case "matatu":
            case "nairobi matatus":
            case "digital matatus":
            case "kampala taxis":
                //TODO: Matatus, either Nairobi or Kampala
                return R.raw.generic;
            case "nelson mandela bay taxi":
            case "cape town taxi":
            case "durban taxi project":
            case "gauteng taxis":
            case "buffalo city taxi":
            case "taxi":
            default:
                //TODO: SA Taxis
                return R.raw.generic;
        }
    }
}
