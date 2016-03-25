package com.example.nicholas.sunshine.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_forecast_textview,
                weatherArray);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView weeklyForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        weeklyForecast.setAdapter(adapter);

        HttpURLConnection urlConnection = null;
        BufferedReader inputReader = null;

        String forecastDataInJSON = null;

        try{
            String baseURL = "http://api.openweathermap.org/data/2.5/forecast/daily?q=sanfrancisco&mode=json&units=metric&cnt=7&appid=92671a566f8856fb90c72f10409aed80";
            URL apiURL = new URL(baseURL);

            // Request the OpenWeatherMap connection
            urlConnection = (HttpURLConnection) apiURL.openConnection();

            // GET method to get information from the API
            // Can't connect on main thread, need a background thread
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Input stream from the URL in JSON format
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            // InputStream is empty -- no information to get that or connection failed
            if(inputStream == null){
                return null;
            }
            inputReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line = inputReader.readLine()) != null) {
                buffer.append(line + '\n');
            }

            if(buffer.length() == 0) {
                return null;
            }
            // Put the information in the Json string
            forecastDataInJSON = buffer.toString();


        } catch (IOException e) {
            // If there's a problem with connecting to the API then print error to the TomCat Logs
            Log.e("PlaceholderFragment", "Error ", e);
            return null;
        } finally{
            if(urlConnection != null){

                // Close the connection once it's done filling the string info
                urlConnection.disconnect();
            }
            if(inputReader != null){
                try{
                    inputReader.close();
                } catch(final IOException e){
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }

        return rootView;
    }
}
