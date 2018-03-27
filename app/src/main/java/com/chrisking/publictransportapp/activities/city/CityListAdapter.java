package com.chrisking.publictransportapp.activities.city;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.classes.City;
import java.util.ArrayList;
import android.support.annotation.NonNull;

public class CityListAdapter extends ArrayAdapter<City> {

    CityListAdapter(Context context, ArrayList<City> cities) {
        super(context, 0, cities);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        City city = getItem(position);

        if (city == null)
            return convertView;

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.city_list_item, parent, false);
        }

        if (position % 2 == 1) {
            convertView.setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.colorLightGray));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.colorDarkGray));
        }

        // Lookup view for data population
        TextView cityNameTextView = (TextView) convertView.findViewById(R.id.cityNameTextView);
        TextView countryNameTextView = (TextView) convertView.findViewById(R.id.countryNameTextView);
        ImageView shareTaxiImageView = (ImageView) convertView.findViewById(R.id.shareTaxiImageView);

        cityNameTextView.setText(city.getName());
        countryNameTextView.setText(city.getCountry());

        if (city.getHasInformal()){
            shareTaxiImageView.setVisibility(View.VISIBLE);
        }
        else{
            shareTaxiImageView.setVisibility(View.INVISIBLE);
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
