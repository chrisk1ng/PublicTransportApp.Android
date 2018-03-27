package com.chrisking.publictransportapp.activities.learnmore;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.about.AboutActivity;

public class LearnMoreActivity extends Fragment {

    public Button aboutButton;
    public TextView wimtWebTextView;
    public TextView developersTextView;
    public TextView privacyTextView;

    private void init(final View view, final Activity activity)
    {
        aboutButton = (Button) view.findViewById(R.id.about);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent push = new Intent(activity, AboutActivity.class);

                startActivity(push);
            }
        });

        wimtWebTextView = (TextView) view.findViewById(R.id.web);
        wimtWebTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.whereismytransport.com/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));

                startActivity(i);
            }
        });

        developersTextView = (TextView) view.findViewById(R.id.developers);
        developersTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://developer.whereismytransport.com/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));

                startActivity(i);
            }
        });

        privacyTextView = (TextView) view.findViewById(R.id.privacy);
        privacyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.whereismytransport.com/privacy-policy";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));

                startActivity(i);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_learn_more, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.title_activity_learn_more);

        init(getView(), getActivity());
    }
}
