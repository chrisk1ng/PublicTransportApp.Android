package com.chrisking.publictransportapp.activities.whereto;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.helpers.Shortcuts;

import java.util.Calendar;
import java.util.Date;

import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import transportapisdk.models.Profile;
import transportapisdk.models.TimeType;

/**
 * Created by ChrisKing on 2017/05/01.
 */

public class FilterOverlay extends Activity {
    private SharedPreferences mPrefs;
    private Profile mSelectedProfile = Profile.ClosestToTime;
    private TimeType mSelectedTimeType = TimeType.DepartAfter;
    private Button mSetFilterButton;
    private Button mClearFilterButton;
    private TimePicker mTimePicker;
    private DatePicker mDatePicker;

    private void init(){
        mDatePicker = (DatePicker) findViewById(R.id.datePicker);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, 168);
        mDatePicker.setMaxDate(cal.getTimeInMillis());
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, -24);
        mDatePicker.setMinDate(cal.getTimeInMillis());

        mTimePicker = (TimePicker) findViewById(R.id.timePicker);
        mTimePicker.setIs24HourView(true);

        mPrefs = this.getSharedPreferences("settings", 0);

        String sProfile = mPrefs.getString("profile", null);

        mSetFilterButton = (Button) findViewById(R.id.setFilter);
        mSetFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                if (mSelectedProfile == Profile.FewestTransfers)
                    i.putExtra("Profile", "FewestTransfers");
                else
                    i.putExtra("Profile", "ClosestToTime");

                if (mSelectedTimeType == TimeType.ArriveBefore)
                        i.putExtra("TimeType", "ArriveBefore");
                else
                        i.putExtra("TimeType", "DepartAfter");

                i.putExtra("Time", getIsoDateTimeUTC());

                setResult(RESULT_OK, i);
                finish();
            }
        });

        mClearFilterButton = (Button) findViewById(R.id.clearFilter);
        mClearFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();

                setResult(RESULT_CANCELED, i);
                finish();
            }
        });


        SegmentedButtonGroup segmentedButtonGroupProfile = (SegmentedButtonGroup) findViewById(R.id.segmentedButtonGroupProfile);
        segmentedButtonGroupProfile.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if (position == 0){
                    mSelectedProfile = Profile.ClosestToTime;
                }
                else{
                    mSelectedProfile = Profile.FewestTransfers;
                }
            }
        });
        if (sProfile.equals("fewest_transfers")) {
            segmentedButtonGroupProfile.setPosition(1, false);
            mSelectedProfile = Profile.FewestTransfers;
        }

        SegmentedButtonGroup segmentedButtonGroupTimeType = (SegmentedButtonGroup) findViewById(R.id.segmentedButtonGroupTimeType);
        segmentedButtonGroupTimeType.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if (position == 0){
                    mSelectedTimeType = TimeType.DepartAfter;
                }
                else{
                    mSelectedTimeType = TimeType.ArriveBefore;
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private String getIsoDateTimeUTC(){
        Calendar calendar = Calendar.getInstance();

        if (Build.VERSION.SDK_INT >= 23 ) {
            calendar.set(Calendar.AM_PM, mTimePicker.getHour() < 12 ? Calendar.AM : Calendar.PM);
            calendar.set(Calendar.HOUR, mTimePicker.getHour());
            calendar.set(Calendar.MINUTE, mTimePicker.getMinute());
        }
        else {
            calendar.set(Calendar.AM_PM, mTimePicker.getCurrentHour() < 12 ? Calendar.AM : Calendar.PM);
            calendar.set(Calendar.HOUR, mTimePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());
        }

        calendar.set(Calendar.YEAR, mDatePicker.getYear());
        calendar.set(Calendar.MONTH, mDatePicker.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, mDatePicker.getDayOfMonth());

        Date time = calendar.getTime();

        return Shortcuts.convertDateToIsoDateTimeString(time);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_overlay);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout(width, (int) (height * 0.6));
        getWindow().setGravity(Gravity.BOTTOM);

        init();
    }
}
