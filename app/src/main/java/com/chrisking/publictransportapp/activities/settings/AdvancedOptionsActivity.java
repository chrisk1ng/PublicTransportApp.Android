package com.chrisking.publictransportapp.activities.settings;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.chrisking.publictransportapp.R;

import co.ceryle.segmentedbutton.SegmentedButtonGroup;

public class AdvancedOptionsActivity extends Fragment {

    private SharedPreferences mPrefs;
    private TextView mProfileDescription;
    private Switch mBusSwitch;
    private Switch mLightRailSwitch;
    private Switch mSubwaySwitch;
    private Switch mRailSwitch;
    private Switch mFerrySwitch;
    private Switch mCoachSwitch;
    private Switch mShareTaxiSwitch;

    private void init(final View view, final Activity activity){
        mPrefs = activity.getSharedPreferences("settings", 0);
        mProfileDescription = (TextView) view. findViewById(R.id.profileDescription);
        mBusSwitch = (Switch) view.findViewById(R.id.bus);
        mLightRailSwitch = (Switch) view.findViewById(R.id.lightrail);
        mSubwaySwitch = (Switch) view.findViewById(R.id.subway);
        mRailSwitch = (Switch) view.findViewById(R.id.rail);
        mFerrySwitch = (Switch) view.findViewById(R.id.ferry);
        mCoachSwitch = (Switch) view.findViewById(R.id.coach);
        mShareTaxiSwitch = (Switch) view.findViewById(R.id.sharetaxi);

        SegmentedButtonGroup segmentedButtonGroup = (SegmentedButtonGroup) view.findViewById(R.id.segmentedButtonGroup);
        segmentedButtonGroup.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if (position == 0){
                    saveSetting("profile", "closest_to_time");
                    mProfileDescription.setText(R.string.advanced_closest_to_time_description);
                }
                else{
                    saveSetting("profile", "fewest_transfers");
                    mProfileDescription.setText(R.string.advanced_fewest_transfers_description);
                }

                Toast.makeText(activity, R.string.advanced_profile_saved, Toast.LENGTH_SHORT).show();
            }
        });

        if (getStringSetting("profile").equals("closest_to_time")){
            segmentedButtonGroup.setPosition(0, false);
            mProfileDescription.setText(R.string.advanced_closest_to_time_description);
        }
        else if (getStringSetting("profile").equals("fewest_transfers")){
            segmentedButtonGroup.setPosition(1, false);
            mProfileDescription.setText(R.string.advanced_fewest_transfers_description);
        }

        mBusSwitch.setChecked(getBooleanSetting("bus"));
        mLightRailSwitch.setChecked(getBooleanSetting("lightrail"));
        mSubwaySwitch.setChecked(getBooleanSetting("subway"));
        mRailSwitch.setChecked(getBooleanSetting("rail"));
        mFerrySwitch.setChecked(getBooleanSetting("ferry"));
        mCoachSwitch.setChecked(getBooleanSetting("coach"));
        mShareTaxiSwitch.setChecked(getBooleanSetting("sharetaxi"));

        mBusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveBooleanSetting("bus", isChecked);
                Toast.makeText(activity, R.string.advanced_modes_saved, Toast.LENGTH_SHORT).show();
            }
        });

        mLightRailSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveBooleanSetting("lightrail", isChecked);
                Toast.makeText(activity, R.string.advanced_modes_saved, Toast.LENGTH_SHORT).show();
            }
        });

        mSubwaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveBooleanSetting("subway", isChecked);
                Toast.makeText(activity, R.string.advanced_modes_saved, Toast.LENGTH_SHORT).show();
            }
        });

        mRailSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveBooleanSetting("rail", isChecked);
                Toast.makeText(activity, R.string.advanced_modes_saved, Toast.LENGTH_SHORT).show();
            }
        });

        mFerrySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveBooleanSetting("ferry", isChecked);
                Toast.makeText(activity, R.string.advanced_modes_saved, Toast.LENGTH_SHORT).show();
            }
        });

        mCoachSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveBooleanSetting("coach", isChecked);
                Toast.makeText(activity, R.string.advanced_modes_saved, Toast.LENGTH_SHORT).show();
            }
        });

        mShareTaxiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveBooleanSetting("sharetaxi", isChecked);
                Toast.makeText(activity, R.string.advanced_modes_saved, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_advanced_options, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.title_activity_advanced);

        init(getView(), getActivity());
    }

    private boolean getBooleanSetting(String key) {
        return mPrefs.getBoolean(key, false);
    }

    private String getStringSetting(String key) {
        return mPrefs.getString(key, null);
    }

    private void saveBooleanSetting(String key, boolean value){
        SharedPreferences.Editor editor = mPrefs.edit();

        editor.putBoolean(key, value);

        editor.commit();
    }

    private void saveSetting(String key, String value){
        SharedPreferences.Editor editor = mPrefs.edit();

        editor.putString(key, value);

        editor.commit();
    }
}
