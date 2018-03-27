package com.chrisking.publictransportapp.activities.journeyoptions;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.chrisking.publictransportapp.helpers.Shortcuts;

import transportapisdk.models.Leg;

/**
 * Created by ChrisKing on 2017/04/28.
 */

public class ModeHolder extends RecyclerView.ViewHolder {
    private ImageView mMode;
    private ConstraintLayout mLayout;

    public ModeHolder(View v) {
        super(v);

        mMode = (ImageView) v.findViewById(R.id.mode);
        mLayout = (ConstraintLayout) v.findViewById(R.id.layout);
    }

    public void bindLeg(Leg leg, boolean isFirstLeg, boolean isLastLeg) {
        if (isFirstLeg && isLastLeg)
            mLayout.setBackground(ContextCompat.getDrawable(ApplicationExtension.getContext(), R.drawable.mode_rounded_corners_both));
        else if (isFirstLeg)
            mLayout.setBackground(ContextCompat.getDrawable(ApplicationExtension.getContext(), R.drawable.mode_rounded_corners_left));
        else if (isLastLeg)
            mLayout.setBackground(ContextCompat.getDrawable(ApplicationExtension.getContext(), R.drawable.mode_rounded_corners_right));

        if (leg.getType().equals("Transit")) {
            if (leg.getLine() != null) {
                if (!isFirstLeg && !isLastLeg){
                    mLayout.setBackgroundColor(Color.parseColor(leg.getLine().getColour()));
                }
                else
                    mLayout.getBackground().setColorFilter(Color.parseColor(leg.getLine().getColour()), PorterDuff.Mode.SRC_ATOP);
                mMode.setImageResource(Shortcuts.mapModeImage24(leg.getLine().getMode()));
            }
        }
        else{
            if (!isFirstLeg && !isLastLeg){
                mLayout.setBackgroundColor(Color.parseColor("#808080"));
            }
            else
                mLayout.getBackground().setColorFilter(Color.parseColor("#808080"), PorterDuff.Mode.SRC_ATOP);
            mMode.setImageResource(Shortcuts.mapModeImage24("Walk"));
        }
    }
}
