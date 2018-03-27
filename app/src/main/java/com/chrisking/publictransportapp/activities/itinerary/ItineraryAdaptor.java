package com.chrisking.publictransportapp.activities.itinerary;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;

import java.util.ArrayList;
import transportapisdk.models.Leg;

public class ItineraryAdaptor extends RecyclerView.Adapter<LegHolder>  {

    private ArrayList<Leg> mLegs;
    private boolean mIsCommute = false;
    private boolean mIsCommuteHome = false;

    ItineraryAdaptor(ArrayList<Leg> legs) {
        mLegs = legs;
    }

    @Override
    public LegHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_selected_itinerary_list_item, parent, false);

        mIsCommute = ((ApplicationExtension) parent.getContext().getApplicationContext()).getIsCommute();
        mIsCommuteHome = ((ApplicationExtension) parent.getContext().getApplicationContext()).getIsCommuteHome();

        return new LegHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(LegHolder holder, int position) {
        Leg leg = mLegs.get(position);

        boolean isFirstLeg = false;
        boolean isLastLeg = false;
        if (position == 0)
            isFirstLeg = true;
        if (position == mLegs.size() - 1)
            isLastLeg = true;

        holder.bindLeg(leg, position, isFirstLeg, isLastLeg, mIsCommute, mIsCommuteHome);
    }

    @Override
    public int getItemCount() {
        return mLegs.size();
    }
}
