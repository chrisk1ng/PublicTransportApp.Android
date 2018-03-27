package com.chrisking.publictransportapp.activities.queues;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.chrisking.publictransportapp.services.geofencing.GeofencePersistence;
import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.classes.NamedGeofence;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

/**
 * Created by ChrisKing on 2017/07/11.
 */

public class TaxiQueueConfigureAdaptor extends ArrayAdapter<NamedGeofence> {
    private ArrayList<NamedGeofence> list = new ArrayList<>();

    public TaxiQueueConfigureAdaptor(Context context, ArrayList<NamedGeofence> namedGeofences) {
        super(context, 0, namedGeofences);

        list = namedGeofences;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public NamedGeofence getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final NamedGeofence namedGeofence = getItem(position);
        final GeofencePersistence geofencePersistence = new GeofencePersistence(getContext());
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.rank_list_item, parent, false);
        }

        if (position % 2 == 1) {
            convertView.setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.colorLightGray));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.colorDarkGray));
        }

        // Lookup view for data population
        TextView stopNameTextView = (TextView) convertView.findViewById(R.id.stopNameTextView);
        Switch subscribeSwitch = (Switch) convertView.findViewById(R.id.subscribeSwitch);
        ImageView deleteImageView = (ImageView) convertView.findViewById(R.id.deleteImageView);

        stopNameTextView.setText(namedGeofence.name);
        subscribeSwitch.setChecked(namedGeofence.isSubscribed);
        subscribeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((Switch) view).isChecked()) {
                    FirebaseMessaging.getInstance().subscribeToTopic(namedGeofence.id);
                    namedGeofence.isSubscribed = true;
                }
                else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(namedGeofence.id);
                    namedGeofence.isSubscribed = false;
                }
                geofencePersistence.addNamedGeofence(namedGeofence);
            }
        });

        deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                geofencePersistence.remove(namedGeofence.id);

                if (namedGeofence.isSubscribed){
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(namedGeofence.id);
                }

                list.remove(position);
                notifyDataSetChanged();
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }
}
