package com.chrisking.publictransportapp.activities.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.chrisking.publictransportapp.helpers.Shortcuts;

import java.util.ArrayList;
import transportapisdk.models.Itinerary;
import transportapisdk.models.Leg;

/**
 * Created by ChrisKing on 2017/05/06.
 */

public class WidgetListProvider implements RemoteViewsService.RemoteViewsFactory {
    public static final String ADDITIONAL_SETUP="com.chrisking.publictransportapp.ADDITIONAL_SETUP";
    public static final String ERROR_OR_NO_CONNECTION="com.chrisking.publictransportapp.ERROR_OR_NO_CONNECTION";
    public static final String DATA_FETCHED="com.chrisking.publictransportapp.DATA_FETCHED";
    public static final String OPEN_ITINERARY="com.chrisking.publictransportapp.OPEN_ITINERARY";
    public static final String HOME="com.chrisking.publictransportapp.HOME";

    private ArrayList<Itinerary> mListItemList = new ArrayList();
    private Context context = null;

    public WidgetListProvider(Context context, Intent intent) {
        this.context = context;

        populateListItem();
    }

    private void populateListItem() {
        mListItemList = (ArrayList<Itinerary>)
                WidgetRemoteFetchService.mListItemList
                        .clone();


        ApplicationExtension app = (ApplicationExtension) context.getApplicationContext();
        app.setItineraries(mListItemList);
    }
    @Override
    public void onDataSetChanged(){

    }

    @Override
    public int getViewTypeCount()
    {
        return 1;
    }

    @Override
    public void onCreate(){
        // In onCreate() you set up any connections / cursors to your data source. Heavy lifting,

        // for example downloading or creating content etc, should be deferred to onDataSetChanged()

        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
    }

    @Override
    public void onDestroy(){

    }

    @Override
    public boolean hasStableIds() {

        return true;

    }

    @Override
    public RemoteViews getLoadingView() {

        return null;

    }

    @Override
    public int getCount() {
        return mListItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
    *Similar to getView of Adapter where instead of View
    *we return RemoteViews
    *
    */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.my_commute_list_item);
        Itinerary itinerary = (Itinerary) mListItemList.get(position);

        Intent intent = new Intent();
        Bundle extras = new Bundle();

        extras.putInt(OPEN_ITINERARY, position);

        if (Shortcuts.userIsHome())
        {
            extras.putBoolean(HOME, true);
            remoteView.setTextViewText(R.id.tripHeader, context.getResources().getText(R.string.widget_trip_home));
        }
        else{
            extras.putBoolean(HOME, false);
            remoteView.setTextViewText(R.id.tripHeader, context.getResources().getText(R.string.widget_trip_work));
        }

        intent.putExtras(extras);
        remoteView.setOnClickFillInIntent(R.id.row, intent);

        remoteView.setTextViewText(R.id.departureTime, Shortcuts.isoDateTimeStringToTime(itinerary.getDepartureTime()));

        int count = 1;
        if (itinerary.getLegs() != null)
            for (Leg leg: itinerary.getLegs()) {
                boolean isFirstLeg = false;
                boolean isLastLeg = false;
                if (count == 1)
                    isFirstLeg = true;
                if (count == itinerary.getLegs().size())
                    isLastLeg = true;

                if (leg.getType().equals("Transit")) {
                    if (leg.getLine() != null) {
                        //if (!isFirstLeg && !isLastLeg){
                            remoteView.setInt(getLayoutId(count), "setBackgroundColor", Color.parseColor(leg.getLine().getColour()));
                        //}
                        //else
                        //    mLayout.getBackground().setColorFilter(Color.parseColor(leg.getLine().getColour()), PorterDuff.Mode.SRC_ATOP);
                        remoteView.setImageViewResource(getImageViewId(count), Shortcuts.mapModeImage24(leg.getLine().getMode()));
                    }
                }
                else{
                    //if (!isFirstLeg && !isLastLeg){
                        remoteView.setInt(getLayoutId(count), "setBackgroundColor", Color.parseColor("#808080"));
                    //}
                    //else
                    //    mLayout.getBackground().setColorFilter(Color.parseColor("#808080"), PorterDuff.Mode.SRC_ATOP);
                    remoteView.setImageViewResource(getImageViewId(count), Shortcuts.mapModeImage24("Walk"));
                }

                count++;
            }

        for (int i = 1 ;i <= 7; i++)
        {
            if (i >= count)
                remoteView.setViewVisibility(getLayoutId(i), View.GONE);
        }

        return remoteView;
    }

    private int getImageViewId(int count){
        if (count == 1)
            return R.id.imageView1;
        else if (count == 2)
            return R.id.imageView2;
        else if (count == 3)
            return R.id.imageView3;
        else if (count == 4)
            return R.id.imageView4;
        else if (count == 5)
            return R.id.imageView5;
        else if (count == 6)
            return R.id.imageView6;
        else
            return R.id.imageView7;
    }

    private int getLayoutId(int count){
        if (count == 1)
            return R.id.layout1;
        else if (count == 2)
            return R.id.layout2;
        else if (count == 3)
            return R.id.layout3;
        else if (count == 4)
            return R.id.layout4;
        else if (count == 5)
            return R.id.layout5;
        else if (count == 6)
            return R.id.layout6;
        else
            return R.id.layout7;
    }
}
