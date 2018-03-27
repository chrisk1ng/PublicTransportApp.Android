package com.chrisking.publictransportapp.activities.journeyoptions;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.journeyoptions.ModeHolder;

import java.util.ArrayList;
import transportapisdk.models.Leg;

/**
 * Created by ChrisKing on 2017/04/28.
 */

public class ModeAdaptor extends RecyclerView.Adapter<ModeHolder> {

    private ArrayList<Leg> mLegs;

    public ModeAdaptor(ArrayList<Leg> legs) {
        mLegs = legs;
    }

    @Override
    public ModeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mode_list_item, parent, false);

        return new ModeHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(ModeHolder holder, int position) {
        Leg leg = mLegs.get(position);

        boolean isFirstLeg = false;
        boolean isLastLeg = false;
        if (position == 0)
            isFirstLeg = true;
        if (position == mLegs.size() - 1)
            isLastLeg = true;

        holder.bindLeg(leg, isFirstLeg, isLastLeg);
    }

    @Override
    public int getItemCount() {
        return mLegs.size();
    }

}
