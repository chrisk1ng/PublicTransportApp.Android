package com.chrisking.publictransportapp.activities.search;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.chrisking.publictransportapp.R;

import java.util.ArrayList;

/**
 * Created by ChrisKing on 2017/04/29.
 */

public class AddressAdapter extends ArrayAdapter<Address> {



    public AddressAdapter(Context context, ArrayList<Address> addresses) {
        super(context, 0, addresses);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Address address = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.address_list_item, parent, false);
        }

        TextView addressTextView = (TextView) convertView.findViewById(R.id.address);
        TextView cityTextView = (TextView) convertView.findViewById(R.id.city);

        addressTextView.setText(address.getAddressLine(0));
        cityTextView.setText(address.getLocality());

        return convertView;
    }
}
