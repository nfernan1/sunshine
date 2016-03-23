package com.example.nicholas.sunshine.app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        List<String> weatherArray = new ArrayList<String>();
        weatherArray.add("Today-Sunny-88/63");
        weatherArray.add("Tomorrow-Foggy-70/46");
        weatherArray.add("Weds-Cloudy-872/63");
        weatherArray.add("Thurs-Rainy-64/53");
        weatherArray.add("Friday-Foggy-70/46");
        weatherArray.add("Sat-Sunny-90/75");

        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}
