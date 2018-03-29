package com.chrisking.publictransportapp.activities.itinerary;

import android.content.Intent;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.map.ViewOnMapActivity;
import com.chrisking.publictransportapp.activities.operatorguide.OperatorGuideActivity;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.chrisking.publictransportapp.helpers.Shortcuts;
import com.flurry.android.FlurryAgent;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.android.rides.RideRequestButtonCallback;
import com.uber.sdk.rides.client.ServerTokenSession;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.uber.sdk.rides.client.error.ApiError;

import transportapisdk.models.Distance;
import transportapisdk.models.Fare;
import transportapisdk.models.Leg;
import transportapisdk.models.Waypoint;

class LegHolder extends RecyclerView.ViewHolder {
    private int mLegPosition;
    private ImageView mMode;
    private TextView mAgencyName;
    private TextView mLineName;
    private TextView mVehicle;
    private TextView mStartTime;
    private TextView mEndTime;
    private TextView mStartLocation;
    private TextView mEndLocation;
    private ConstraintLayout mInnerLayout;
    private TextView mDurationHeader;
    private TextView mDuration;
    private TextView mApproxCost;
    private TextView mApproxCostHeader;
    private TextView mServiceAlerts;
    private TextView mServiceAlertsHeader;
    private ImageView mMapButton;
    private LinearLayout mLeftLineLayout;
    private LinearLayout mRightLineLayout;
    private RideRequestButton mUberButton;
    private String mStartPoint;
    private String mEndPoint;

    private Button mGuideButton;

    LegHolder(View v) {
        super(v);

        mMode = (ImageView) v.findViewById(R.id.mode);
        mAgencyName = (TextView) v.findViewById(R.id.agencyName);
        mLineName = (TextView) v.findViewById(R.id.lineName);
        mVehicle = (TextView) v.findViewById(R.id.vehicleDetails);
        mDurationHeader = (TextView) v.findViewById(R.id.durationHeader);
        mInnerLayout = (ConstraintLayout) v.findViewById(R.id.innerCard);
        mStartTime = (TextView) v.findViewById(R.id.startTime);
        mEndTime = (TextView) v.findViewById(R.id.endTime);
        mStartLocation = (TextView) v.findViewById(R.id.startLocation);
        mEndLocation = (TextView) v.findViewById(R.id.endLocation);
        mDuration = (TextView) v.findViewById(R.id.duration);
        mApproxCost = (TextView) v.findViewById(R.id.cost);
        mApproxCostHeader = (TextView) v.findViewById(R.id.costHeader);
        mServiceAlerts = (TextView) v.findViewById(R.id.serviceAlerts);
        mServiceAlertsHeader = (TextView) v.findViewById(R.id.serviceAlertsHeader);
        mMapButton = (ImageView) v.findViewById(R.id.map);
        mLeftLineLayout = (LinearLayout) v.findViewById(R.id.leftLine);
        mRightLineLayout = (LinearLayout) v.findViewById(R.id.rightLine);
        mUberButton = (RideRequestButton) v.findViewById(R.id.uberButton);
        mGuideButton = (Button) v.findViewById(R.id.guideButton);
    }


    public void bindLeg(final Leg leg, int position, boolean isFirstLeg, boolean isLastLeg, boolean isCommute, boolean isCommuteHome) {
        mLegPosition = position;

        if (isFirstLeg)
            mLeftLineLayout.setVisibility(View.INVISIBLE);

        if (isLastLeg)
            mRightLineLayout.setVisibility(View.INVISIBLE);

        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryAgent.logEvent("ShowLegOnMap");
                Intent push = new Intent(ApplicationExtension.getContext(), ViewOnMapActivity.class);
                push.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                push.putExtra("leg", mLegPosition);
                ApplicationExtension.getContext().startActivity(push);
            }
        });

        mGuideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent push = new Intent(ApplicationExtension.getContext(), OperatorGuideActivity.class);
                push.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String agencyName = leg.getLine().getAgency().getName();
                push.putExtra(Intent.EXTRA_TEXT, agencyName);
                ApplicationExtension.getContext().startActivity(push);
            }
        });

        mServiceAlerts.setVisibility(View.INVISIBLE);
        mServiceAlertsHeader.setVisibility(View.INVISIBLE);

        mDuration.setText(getTripDuration(leg.getDuration()));

        if (leg.getWaypoints() != null){
            Waypoint firstWayPoint = getFirstWayPoint(leg);
            Waypoint lastWayPoint = getLastWayPoint(leg);
            mStartTime.setText(Shortcuts.isoDateTimeStringToTime(firstWayPoint.getDepartureTime()));
            mEndTime.setText(Shortcuts.isoDateTimeStringToTime(lastWayPoint.getArrivalTime()));

            if (isStop(firstWayPoint)){
                mStartLocation.setText(firstWayPoint.getStop().getName());
                mStartPoint = firstWayPoint.getStop().getGeometry().toString();
            }
            else if (isLocation(firstWayPoint)){
                mStartPoint = firstWayPoint.getLocation().getGeometry().toString();
                if (isFirstLeg) {
                    if (!isCommute)
                        mStartLocation.setText(ApplicationExtension.getContext().getResources().getString(R.string.itinerary_view_list_item_your_location));
                    else if (isCommute && isCommuteHome)
                        mStartLocation.setText(R.string.itinerary_view_list_item_work);
                    else if (isCommute && !isCommuteHome)
                        mStartLocation.setText(R.string.itinerary_view_list_item_home);
                }
                else
                    mStartLocation.setText(firstWayPoint.getLocation().getAddress());
            }
            else if (isHail(firstWayPoint)){
                mStartPoint = firstWayPoint.getHail().getGeometry().toString();
                mStartLocation.setText(ApplicationExtension.getContext().getResources().getString(R.string.itinerary_view_list_item_hail_catch_taxi));
            }

            if (isStop(lastWayPoint)){
                mEndLocation.setText(lastWayPoint.getStop().getName());
                mEndPoint = lastWayPoint.getStop().getGeometry().toString();
            }
            else if (isLocation(lastWayPoint)){
                mEndPoint = lastWayPoint.getLocation().getGeometry().toString();
                if (isLastLeg) {
                    if (!isCommute)
                        mEndLocation.setText(lastWayPoint.getLocation().getAddress());
                    else if (isCommute && isCommuteHome)
                        mEndLocation.setText(R.string.itinerary_view_list_item_home);
                    else if (isCommute && !isCommuteHome)
                        mEndLocation.setText(R.string.itinerary_view_list_item_work);
                }
                else
                    mEndLocation.setText(lastWayPoint.getLocation().getAddress());
            }
            else if (isHail(lastWayPoint)){
                mEndPoint = lastWayPoint.getHail().getGeometry().toString();
                mEndLocation.setText(ApplicationExtension.getContext().getResources().getString(R.string.itinerary_view_list_item_hail_catch_taxi));
            }
        }

        if (leg.getType().equals("Transit")) {
            mDurationHeader.setText(ApplicationExtension.getContext().getResources().getString(R.string.itinerary_view_list_item_duration_header));
            mApproxCost.setText(getApproxCost(leg.getFare()));

            mApproxCost.setVisibility(View.VISIBLE);
            mApproxCostHeader.setVisibility(View.VISIBLE);

            mVehicle.setText("");
            mUberButton.setVisibility(View.GONE);

            if (leg.getLine() != null) {
                if (leg.getLine().getAgency() != null)
                    mAgencyName.setText(leg.getLine().getAgency().getName());

                mInnerLayout.setBackgroundColor(getBackgroundColor(leg.getLine().getColour()));
                mLineName.setText(leg.getLine().getName());



                String mode = leg.getLine().getMode();
                if (mode.toLowerCase().equals("sharetaxi")){
                    mGuideButton.setVisibility(View.VISIBLE);
                } else {
                    mGuideButton.setVisibility(View.GONE);
                }

                mMode.setImageResource(Shortcuts.mapModeImage72(mode));

                if (leg.getVehicle() != null){
                    if (leg.getVehicle().getDesignation() != null)
                        mVehicle.setText(Shortcuts.mapModeToText(mode) + " " + leg.getVehicle().getDesignation());
                }
            }
        }
        else if (leg.getType().equals("Walking")) {
            mInnerLayout.setBackgroundColor(Color.parseColor("#808080"));
            mApproxCost.setVisibility(View.INVISIBLE);
            mApproxCostHeader.setVisibility(View.INVISIBLE);
            mAgencyName.setText(ApplicationExtension.getContext().getResources().getString(R.string.itinerary_view_list_item_walking));
            mDurationHeader.setText(ApplicationExtension.getContext().getResources().getString(R.string.itinerary_view_list_item_duration_header_walking));
            mLineName.setText(getWalkingDistance(leg.getDistance()));
            mVehicle.setText("");
            mMode.setImageResource(Shortcuts.mapModeImage72("Walk"));

            if (leg.getDuration() / 60 > 10){


                RideParameters rideParams = new RideParameters.Builder()
                        .setPickupLocation(Double.parseDouble(mStartPoint.split(",")[0]), Double.parseDouble(mStartPoint.split(",")[1]), mStartLocation.getText().toString(), null)
                        .setDropoffLocation(Double.parseDouble(mEndPoint.split(",")[0]), Double.parseDouble(mEndPoint.split(",")[1]), mEndLocation.getText().toString(), null)
                        .build();

                SessionConfiguration config = new SessionConfiguration.Builder()
                        .setClientId("VOSERAQG7NlEBpTr7lQUsGlcH8VysTLo") //This is necessary
                        .setServerToken("r9TPmPknIvXb1wZbCYqAbM3bgGpDVGwUkFalzzce")
                        .setRedirectUri("https://chrisk1ng.github.io/") //This is necessary if you'll be using implicit grant
                        .build();

                ServerTokenSession session = new ServerTokenSession(config);

                RideRequestButtonCallback callback = new RideRequestButtonCallback() {

                    @Override
                    public void onRideInformationLoaded() {

                    }

                    @Override
                    public void onError(ApiError apiError) {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                };

                mUberButton.setRideParameters(rideParams);
                mUberButton.setSession(session);
                mUberButton.setCallback(callback);
                mUberButton.loadRideInformation();
                mUberButton.setVisibility(View.VISIBLE);
            }
            else{
                mUberButton.setVisibility(View.GONE);
            }
        }
    }

    private String getWalkingDistance(Distance distance){
        if (distance == null){
            return  ApplicationExtension.getContext().getResources().getString(R.string.distance_unknown);
        }

        return "~" + distance.getValue() + distance.getUnit();
    }

    private String getTripDuration(Integer duration) {
        if (duration == null){
            return ApplicationExtension.getContext().getResources().getString(R.string.duration_unknown);
        }

        int minutes = duration / 60;
        if (minutes <= 1){
            return "~1 " + ApplicationExtension.getContext().getResources().getString(R.string.duration_minute);
        }

        return "~" + minutes + " " + ApplicationExtension.getContext().getResources().getString(R.string.duration_minutes);
    }

    private String getApproxCost(Fare fare) {
        if (fare == null) {
            return ApplicationExtension.getContext().getResources().getString(R.string.fare_unknown);
        }

        if (fare.getCost() == null){
            return fare.getDescription();
        }

        return fare.getCost().getCurrencyCode() + " " + fare.getCost().getAmount();
    }

    private Waypoint getFirstWayPoint(Leg leg){
        if (leg.getWaypoints() == null)
            return null;

        return leg.getWaypoints().get(0);
    }

    private Waypoint getLastWayPoint(Leg leg){
        if (leg.getWaypoints() == null)
            return null;

        return leg.getWaypoints().get(leg.getWaypoints().size() - 1);
    }

    private boolean isStop(Waypoint waypoint){
        return waypoint.getStop() != null;
    }

    private boolean isLocation(Waypoint waypoint){
        return waypoint.getLocation() != null;
    }

    private boolean isHail(Waypoint waypoint){
        return waypoint.getHail() != null;
    }

    private int getBackgroundColor(String lineColor){
        int color = Color.parseColor(lineColor);

        if (Shortcuts.colorIsBright(color)){
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);

            float[] hsb = new float[3];
            Color.RGBToHSV(red, green, blue, hsb);

            hsb[2] = 0.85f;

            int outputColor = Color.HSVToColor(hsb);
            red = Color.red(outputColor);
            green = Color.green(outputColor);
            blue = Color.blue(outputColor);
            System.out.println(red + ", " + green + ", " + blue);

            return Color.HSVToColor(hsb);
        }
        else{
            return color;
        }
    }
}
