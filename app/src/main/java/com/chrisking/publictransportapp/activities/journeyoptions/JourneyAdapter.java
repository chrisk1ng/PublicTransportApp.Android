package com.chrisking.publictransportapp.activities.journeyoptions;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chrisking.publictransportapp.activities.itinerary.ItineraryViewActivity;
import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.map.ViewOnMapActivity;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.chrisking.publictransportapp.helpers.Shortcuts;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import transportapisdk.models.Itinerary;
import transportapisdk.models.Leg;
import transportapisdk.models.Waypoint;

/**
 * Created by ChrisKing on 2017/04/22.
 */

public class JourneyAdapter extends ArrayAdapter<Itinerary> {

    final String mWalkFarNightText = "- it's far, so stay safe at night or choose a different trip";
    final String mWalkFarDayText = "- it's far, but at least it's light out";

    public JourneyAdapter(Context context, ArrayList<Itinerary> itineraries) {
        super(context, 0, itineraries);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Itinerary itinerary = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.itinerary_list_item, parent, false);
        }
        final ArrayList<Leg> mArrayOflegs = new ArrayList<>();
        mArrayOflegs.addAll(itinerary.getLegs());
        ModeAdaptor adapter = new ModeAdaptor(mArrayOflegs);
        // Lookup view for data population
        TextView departureTime = (TextView) convertView.findViewById(R.id.departureTime);
        TextView startTime = (TextView) convertView.findViewById(R.id.startTime);
        TextView endTime = (TextView) convertView.findViewById(R.id.endTime);
        TextView cost = (TextView) convertView.findViewById(R.id.cost);
        RecyclerView modes = (RecyclerView) convertView.findViewById(R.id.modes);

        // Populate the data into the template view using the data object
        startTime.setText(Shortcuts.isoDateTimeStringToTime(itinerary.getDepartureTime()));
        endTime.setText(Shortcuts.isoDateTimeStringToTime(itinerary.getArrivalTime()));
        departureTime.setText(Shortcuts.timeUntil(itinerary.getDepartureTime()));
        cost.setText(getApproxCost(itinerary));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(super.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        modes.setLayoutManager(linearLayoutManager);
        modes.setAdapter(adapter);

        // Lookup view for data population
        Button selectJourneyButton = (Button) convertView.findViewById(R.id.selectJourney);
        Button showOnMapButton = (Button) convertView.findViewById(R.id.showOnMap);
        // Cache row position inside the button using `setTag`
        selectJourneyButton.setTag(position);
        showOnMapButton.setTag(position);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout list = (LinearLayout)view.findViewById(R.id.overviewLayout);

                if (list.getVisibility() == View.GONE){
                    FlurryAgent.logEvent("SeeOverview");

                    for (int i = 0; i < mArrayOflegs.size(); i++) {
                        Leg leg = mArrayOflegs.get(i);

                        boolean first = false;
                        boolean last = false;
                        Leg nextLeg = null;
                        Leg previousLeg = null;

                        if (i == 0){
                            first = true;
                        }
                        if (i == mArrayOflegs.size() - 1){
                            last = true;
                        }

                        if (!first){
                            previousLeg = mArrayOflegs.get(i - 1);
                        }
                        if (!last){
                            nextLeg = mArrayOflegs.get(i + 1);
                        }

                        View item = LayoutInflater.from(view.getContext()).inflate(R.layout.activity_overview_list_item, null);

                        ImageView mode = (ImageView) item.findViewById(R.id.modeImageView);
                        TextView descriptionTextView = (TextView) item.findViewById(R.id.descriptionTextView);

                        if (leg.getType().equals("Transit")) {
                            if (leg.getLine() != null) {
                                mode.setImageResource(Shortcuts.mapModeImage72(leg.getLine().getMode()));

                                descriptionTextView.setText(buildTransitText(leg, first, last, previousLeg));
                            }
                        }
                        else {
                            mode.setImageResource(Shortcuts.mapModeImage72("Walk"));

                            descriptionTextView.setText(buildWalkingText(leg, first, last, nextLeg));
                        }

                        mode.setColorFilter(ContextCompat.getColor(view.getContext()
                                ,R.color.colorBlack));

                        list.addView(item);
                    }

                    list.setVisibility(View.VISIBLE);
                }
                else{
                    list.removeAllViews();
                    list.setVisibility(View.GONE);
                }
            }
        });

        // Attach the click event handler
        selectJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlurryAgent.logEvent("SelectJourney");

                int position = (Integer) view.getTag();
                // Access the row position here to get the correct data item
                Itinerary selectedItinerary = getItem(position);

                Context context = JourneyAdapter.super.getContext();

                Intent push = new Intent(context, ItineraryViewActivity.class);

                ApplicationExtension app = (ApplicationExtension) context.getApplicationContext();
                app.setItinerary(selectedItinerary);

                context.startActivity(push);
            }
        });
        showOnMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlurryAgent.logEvent("ShowItineraryOnMap");

                int position = (Integer) view.getTag();
                // Access the row position here to get the correct data item
                Itinerary selectedItinerary = getItem(position);

                Context context = JourneyAdapter.super.getContext();

                Intent push = new Intent(context, ViewOnMapActivity.class);

                ApplicationExtension app = (ApplicationExtension) context.getApplicationContext();
                app.setItinerary(selectedItinerary);

                context.startActivity(push);
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    private String getApproxCost(Itinerary itinerary) {
        double cost = 0.0;
        String currency = "";

        for (Leg leg: itinerary.getLegs()) {
            if (leg.getType().equals("Transit")) {
                if (leg.getFare() != null) {
                    if (leg.getFare().getCost() != null) {
                        cost += leg.getFare().getCost().getAmount();
                        currency = leg.getFare().getCost().getCurrencyCode();
                    }
                }
            }
        }

        return currency + " " + String.format("%.2f", cost);
    }

    private Spannable buildTransitText(Leg leg, boolean first, boolean last, Leg previousLeg) {
        if (first && !last){
            return buildFirstLegTransitText(leg);
        }
        else if ((first && last) || last){
            return buildLastOrOnlyLegTransitText(leg);
        }
        else{
            return buildMiddleLegTransitText(leg, previousLeg);
        }
    }

    private Spannable buildLastOrOnlyLegTransitText(Leg leg) {
        String transitText = "At ";
        List<Waypoint> waypoints = leg.getWaypoints();
        Waypoint first = waypoints.get(0);
        String depatureTimeFormatted = Shortcuts.isoDateTimeStringToTime(first.getDepartureTime());
        int lineColorAttribute = Color.parseColor(leg.getLine().getColour());
        String lineName = leg.getLine().getName();

        String darkText1 = depatureTimeFormatted;
        String darkText2 = null;
        String darkText3 = null;
        String lineText;
        String stopText = null;

        transitText += depatureTimeFormatted;

        if (isEndWaypointAStop(leg)){
            Waypoint last = waypoints.get(waypoints.size() - 1);
            String agencyName = last.getStop().getAgency().getName();
            String mode = Shortcuts.mapModeToText(leg.getLine().getMode());

            String destinationStop = last.getStop().getName();

            if (mode == "bus"){
                transitText += ", get on the ";
            }
            else{
                transitText += ", board the ";
            }

            transitText += agencyName + " ";
            transitText += mode + " ";

            darkText2 = agencyName + " " + mode;

            if (leg.getVehicle() != null && leg.getVehicle().getDesignation() != null)
            {
                transitText += leg.getVehicle().getDesignation() + " ";

                darkText3 = leg.getVehicle().getDesignation();
            }

            transitText += "along ";
            transitText += lineName;
            transitText += "until ";
            transitText += destinationStop + " ";
            transitText += "to reach your destination";

            lineText = lineName;
            stopText = destinationStop;
        }
        else{
            transitText += ", hail the next share taxi running ";
            transitText += lineName;
            transitText += "and tell the driver your destination";

            lineText = lineName;
        }

        transitText += ".";

        Spannable sb = new SpannableString( transitText );
        if (darkText1 != null) {
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    transitText.indexOf(darkText1),
                    transitText.indexOf(darkText1) + darkText1.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (darkText2 != null){
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    transitText.indexOf(darkText2),
                    transitText.indexOf(darkText2) + darkText2.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (darkText3 != null){
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    transitText.indexOf(darkText3),
                    transitText.indexOf(darkText3) + darkText3.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (lineText != null){
            sb.setSpan(new ForegroundColorSpan(lineColorAttribute),
                    transitText.indexOf(lineText),
                    transitText.indexOf(lineText) + lineText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (stopText != null){
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    transitText.indexOf(stopText),
                    transitText.indexOf(stopText) + stopText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return sb;
    }

    private Spannable buildMiddleLegTransitText(Leg leg, Leg previousLeg) {
        String transitText = "";
        List<Waypoint> waypoints = leg.getWaypoints();
        Waypoint first = waypoints.get(0);
        String depatureTimeFormatted = Shortcuts.isoDateTimeStringToTime(first.getDepartureTime());
        int lineColorAttribute = Color.parseColor(leg.getLine().getColour());
        String lineName = leg.getLine().getName();

        String darkText1 = null;
        String darkText2 = null;
        String darkText3 = null;
        String lineText;
        String stopText = null;

        if (previousLeg.getType() == "Transit"){
            transitText += "At " + depatureTimeFormatted;

            darkText1 = depatureTimeFormatted;
        }

        if (isEndWaypointAStop(leg)){
            Waypoint last = waypoints.get(waypoints.size() - 1);
            String agencyName = last.getStop().getAgency().getName();
            String mode = Shortcuts.mapModeToText(leg.getLine().getMode());

            String destinationStop = last.getStop().getName();

            if (mode == "bus"){
                if (previousLeg.getType() == "Transit"){
                    transitText += ", get on the ";
                }
                else{
                    transitText += "Get on the ";
                }
            }
            else{
                if (previousLeg.getType() == "Transit"){
                    transitText += ", board the ";
                }
                else{
                    transitText += "Board the ";
                }
            }

            transitText += agencyName + " ";
            transitText += mode + " ";
            darkText2 = agencyName + " " + mode;

            if (leg.getVehicle() != null && leg.getVehicle().getDesignation() != null)
            {
                transitText += leg.getVehicle().getDesignation() + " ";

                darkText3 = leg.getVehicle().getDesignation();
            }

            transitText += "along ";
            transitText += lineName + " ";
            transitText += "until ";
            transitText += destinationStop;

            lineText = lineName;
            stopText = destinationStop;
        }
        else{
            transitText += ", hail the next share taxi running ";
            transitText += lineName;

            lineText = lineName;
        }

        transitText += ".";

        Spannable sb = new SpannableString( transitText );
        if (darkText1 != null) {
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    transitText.indexOf(darkText1),
                    transitText.indexOf(darkText1) + darkText1.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (darkText2 != null){
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    transitText.indexOf(darkText2),
                    transitText.indexOf(darkText2) + darkText2.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (darkText3 != null){
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    transitText.indexOf(darkText3),
                    transitText.indexOf(darkText3) + darkText3.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (lineText != null){
            sb.setSpan(new ForegroundColorSpan(lineColorAttribute),
                    transitText.indexOf(lineText),
                    transitText.indexOf(lineText) + lineText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (stopText != null){
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    transitText.indexOf(stopText),
                    transitText.indexOf(stopText) + stopText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return sb;
    }

    private Spannable buildFirstLegTransitText(Leg leg) {
        String transitText = "At ";
        int lineColorAttribute = Color.parseColor(leg.getLine().getColour());
        String lineName = leg.getLine().getName();

        List<Waypoint> waypoints = leg.getWaypoints();
        Waypoint first = waypoints.get(0);
        String depatureTimeFormatted = Shortcuts.isoDateTimeStringToTime(first.getDepartureTime());
        transitText += depatureTimeFormatted;

        String darkText1 = depatureTimeFormatted;
        String darkText2 = null;
        String darkText3 = null;
        String lineText;
        String stopText = null;

        if (isEndWaypointAStop(leg)){
            Waypoint last = waypoints.get(waypoints.size() - 1);
            String agencyName = last.getStop().getAgency().getName();
            String mode = Shortcuts.mapModeToText(leg.getLine().getMode());

            String destinationStop = last.getStop().getName();

            if (mode == "bus"){
                transitText += ", get on the ";
            }
            else{
                transitText += ", board the ";
            }

            transitText += agencyName + " ";
            transitText += mode + " ";

            darkText2 = agencyName + " " + mode;

            if (leg.getVehicle() != null && leg.getVehicle().getDesignation() != null)
            {
                transitText += leg.getVehicle().getDesignation() + " ";

                darkText3 = leg.getVehicle().getDesignation();
            }

            transitText += "along ";
            transitText += lineName + " ";
            transitText += "until ";
            transitText += destinationStop;

            lineText = lineName;
            stopText = destinationStop;
        }
        else{
            transitText += ", hail the next share taxi running ";
            transitText += lineName + " ";

            lineText = lineName;
        }

        transitText += ".";

        Spannable sb = new SpannableString( transitText );
        if (darkText1 != null) {
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    transitText.indexOf(darkText1),
                    transitText.indexOf(darkText1) + darkText1.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (darkText2 != null){
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    transitText.indexOf(darkText2),
                    transitText.indexOf(darkText2) + darkText2.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (darkText3 != null){
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    transitText.indexOf(darkText3),
                    transitText.indexOf(darkText3) + darkText3.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (lineText != null){
            sb.setSpan(new ForegroundColorSpan(lineColorAttribute),
                    transitText.indexOf(lineText),
                    transitText.indexOf(lineText) + lineText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (stopText != null){
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    transitText.indexOf(stopText),
                    transitText.indexOf(stopText) + stopText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return sb;
    }

    private Spannable buildWalkingText(Leg leg, boolean first, boolean last, Leg nextLeg) {
        if (first && !last){
            return buildFirstLegWalkingText(leg);
        }
        else if ((first && last) || last){
            return buildLastOrOnlyWalkingLegText(leg);
        }
        else{
            return buildMiddleWalkingText(leg, nextLeg);
        }
    }

    private Spannable buildMiddleWalkingText(Leg leg, Leg nextLeg) {
        boolean isToStop = isEndWaypointAStop(leg);
        boolean isNightTime = Shortcuts.isNightTime();
        int distance = leg.getDistance().getValue();
        int waitingTime = getWaitingTime(leg, nextLeg);
        List<Waypoint> waypoints = leg.getWaypoints();
        Waypoint last = waypoints.get(waypoints.size() - 1);
        String walkingTime = getDurationInMinutes(leg);
        String walkingText = "";

        String darkText1 = null;
        String darkText2 = null;
        String darkText3 = null;

        if (isToStop){
            String agencyName = last.getStop().getAgency().getName();
            String stopName = last.getStop().getName();

            if (distance < 200){
                walkingText += "Make a quick transfer to the ";
                walkingText += agencyName + " " + stopName + " ";
                walkingText += "stop.";

                darkText1 = agencyName + " " + stopName;
            }
            else if (distance < 1000){
                if (waitingTime < 2){
                    walkingText += "Take a brisk walk to the ";
                    walkingText += agencyName + " " + stopName + " ";
                    walkingText += "stop.";

                    darkText1 = agencyName + " " + stopName;
                }
                else if (waitingTime < 5){
                    walkingText += "Stroll to the ";
                    walkingText += agencyName + " " + stopName + " ";
                    walkingText += "stop.";

                    darkText1 = agencyName + " " + stopName;
                }
                else{
                    walkingText += "Stroll to the ";
                    walkingText += agencyName + " " + stopName + " ";

                    darkText1 = agencyName + " " + stopName;

                    if(isNightTime){
                        walkingText += "stop and stay vigilant at night while you wait about ";
                    }
                    else{
                        walkingText += "stop and wait about ";
                    }
                    walkingText += String.valueOf(waitingTime) + "min";
                    walkingText += ".";

                    darkText2 = String.valueOf(waitingTime) + "min";
                }
            }
            else{
                if (waitingTime < 2){
                    walkingText += "Take a ";
                    walkingText += String.valueOf(walkingTime) + "min ";
                    walkingText += "walk to the ";
                    walkingText += agencyName + " " + stopName + " ";

                    darkText1 = agencyName + " " + stopName;
                    darkText2 = String.valueOf(walkingTime) + "min";

                    if(isNightTime){
                        walkingText += "stop. It’s far, so stay safe at night or choose a different trip.";
                    }
                    else{
                        walkingText += "stop. It’s far, but at least it’s light out.";
                    }
                }
                else if (waitingTime < 5){
                    walkingText += "Take a ";
                    walkingText += String.valueOf(walkingTime) + "min ";
                    walkingText += "stroll to the ";
                    walkingText += agencyName + " " + stopName + " ";

                    darkText1 = agencyName + " " + stopName;
                    darkText2 = String.valueOf(walkingTime) + "min";

                    if(isNightTime){
                        walkingText += "stop. It’s far, so stay safe at night or choose a different trip.";
                    }
                    else{
                        walkingText += "stop. It’s far, but at least it’s light out.";
                    }
                }
                else{
                    walkingText += "Take a ";
                    walkingText += String.valueOf(walkingTime) + "min ";
                    walkingText += "stroll to the ";
                    walkingText += agencyName + " " + stopName + " ";

                    darkText1 = agencyName + " " + stopName;
                    darkText2 = String.valueOf(walkingTime) + "min";

                    if(isNightTime){
                        walkingText += "stop and stay vigilant at night while you wait about ";
                    }
                    else{
                        walkingText += "stop and wait about ";
                    }
                    walkingText += String.valueOf(waitingTime) + "min";
                    walkingText += ".";

                    darkText3 = String.valueOf(waitingTime) + "min";

                    if(isNightTime){
                        walkingText += "It’s far, so stay safe at night or choose a different trip.";
                    }
                    else{
                        walkingText += "It’s far, but at least it’s light out.";
                    }
                }
            }
        }
        else{
            if (distance < 200){
                walkingText += "Quickly transfer to the sidewalk where the share taxis pass.";
            }
            else if (distance < 1000){
                if (waitingTime < 2){
                    walkingText += "Take a brisk walk to the sidewalk where the share taxis pass.";
                }
                else if (waitingTime < 5){
                    walkingText += "Stroll to the sidewalk where the share taxis pass.";
                }
                else{
                    walkingText += "Stroll to the sidewalk where the share taxis pass ";
                    if(isNightTime){
                        walkingText += "and stay vigilant at night while you wait about ";
                    }
                    else{
                        walkingText += "and wait about ";
                    }
                    walkingText += String.valueOf(waitingTime) + "min";
                    walkingText += ".";
                }
            }
            else{
                if (waitingTime < 2){
                    walkingText += "Take a ";
                    walkingText += String.valueOf(walkingTime) + "min ";
                    walkingText += "walk to the sidewalk where the share taxis pass. ";

                    if(isNightTime){
                        walkingText += "It’s far, so stay safe at night or choose a different trip.";
                    }
                    else{
                        walkingText += "It’s far, but at least it’s light out.";
                    }
                }
                else if (waitingTime < 5){
                    walkingText += "Take a ";
                    walkingText += String.valueOf(walkingTime) + "min ";
                    walkingText += "stroll to the sidewalk where the share taxis pass. ";

                    if(isNightTime){
                        walkingText += "It’s far, so stay safe at night or choose a different trip.";
                    }
                    else{
                        walkingText += "It’s far, but at least it’s light out.";
                    }
                }
                else{
                    walkingText += "Take a ";
                    walkingText += String.valueOf(walkingTime) + "min ";
                    walkingText += "stroll to the sidewalk where the share taxis pass ";
                    if(isNightTime){
                        walkingText += "and stay vigilant at night while you wait about ";
                    }
                    else{
                        walkingText += "and wait about ";
                    }
                    walkingText += String.valueOf(waitingTime) + "min";
                    walkingText += ".";

                    if(isNightTime){
                        walkingText += "It’s far, so stay safe at night or choose a different trip.";
                    }
                    else{
                        walkingText += "It’s far, but at least it’s light out.";
                    }
                }
            }
        }

        Spannable sb = new SpannableString( walkingText );
        if (darkText1 != null) {
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    walkingText.indexOf(darkText1),
                    walkingText.indexOf(darkText1) + darkText1.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (darkText2 != null){
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    walkingText.indexOf(darkText2),
                    walkingText.indexOf(darkText2) + darkText2.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (darkText3 != null){
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    walkingText.indexOf(darkText3),
                    walkingText.indexOf(darkText3) + darkText3.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return sb;
    }

    private Spannable buildLastOrOnlyWalkingLegText(Leg leg) {
        if (leg.getDistance().getValue() < 200){
            return new SpannableString("Take a short walk to reach your destination.");
        }
        else if (leg.getDistance().getValue() < 1000){
            String legDuration = getDurationInMinutes(leg);
            String walkingText = "Walk to your destination in about ";
            walkingText += legDuration;
            walkingText += ".";

            Spannable sb = new SpannableString( walkingText );
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    walkingText.indexOf(legDuration),
                    walkingText.indexOf(legDuration) + legDuration.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        }
        else{
            String legDuration = getDurationInMinutes(leg);
            String walkingText = "Take a ";
            walkingText += legDuration + " ";
            walkingText += "walk to your destination";

            if (Shortcuts.isNightTime()){
                walkingText += mWalkFarNightText;
            }
            else{
                walkingText += mWalkFarDayText;
            }
            walkingText += ".";

            Spannable sb = new SpannableString( walkingText );
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    walkingText.indexOf(legDuration),
                    walkingText.indexOf(legDuration) + legDuration.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return sb;
        }
    }

    private Spannable buildFirstLegWalkingText(Leg leg) {
        List<Waypoint> waypoints = leg.getWaypoints();
        Waypoint last = waypoints.get(waypoints.size() - 1);

        String darkText1 = null;
        String darkText2;
        String walkingText = "Walk to the ";
        String depatureTimeFormatted = Shortcuts.isoDateTimeStringToTime(last.getDepartureTime());

        if (isEndWaypointAStop(leg)){

            String agencyName = last.getStop().getAgency().getName();
            String stopName = last.getStop().getName();

            walkingText += agencyName + " " + stopName + " ";
            walkingText += "stop by ";
            walkingText += depatureTimeFormatted;

            darkText1 = agencyName + " " + stopName;
            darkText2 = depatureTimeFormatted;
        }
        else{
            walkingText += "sidewalk where the share taxis pass by ";
            walkingText += depatureTimeFormatted;

            darkText2 = depatureTimeFormatted;
        }

        if (Shortcuts.isNightTime()){
            if (leg.getDistance().getValue() > 1000){
                walkingText += mWalkFarNightText;
            }
        }

        walkingText += ".";

        Spannable sb = new SpannableString( walkingText );
        if (darkText1 != null) {
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    walkingText.indexOf(darkText1),
                    walkingText.indexOf(darkText1) + darkText1.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (darkText2 != null){
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(super.getContext(), R.color.colorBlack)),
                    walkingText.indexOf(darkText2),
                    walkingText.indexOf(darkText2) + darkText2.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return sb;
    }

    private String getDurationInMinutes(Leg leg) {
        String time = String.valueOf(leg.getDuration() / 60);

        return time + "min";
    }

    private int getWaitingTime(Leg leg, Leg nextLeg) {
        String arrivalDateTime = leg.getWaypoints()
                .get(leg.getWaypoints().size() - 1)
                .getArrivalTime();

        String departureDateTime = nextLeg.getWaypoints()
                .get(0)
                .getDepartureTime();

        Date date1 = Shortcuts.convertIsoDateTimeStringToDate(arrivalDateTime);
        Date date2 = Shortcuts.convertIsoDateTimeStringToDate(departureDateTime);
        long diff = date1.getTime() - date2.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;

        return (int)minutes;
    }

    private boolean isEndWaypointAStop(Leg leg) {
        List<Waypoint> waypoints = leg.getWaypoints();
        if (waypoints != null & waypoints.size() > 0){
            Waypoint last = waypoints.get(waypoints.size() - 1);
            if (last.getStop() != null){
                return true;
            }
        }

        return false;
    }
}

