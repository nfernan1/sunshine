package com.example.nicholas.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Lets fragment handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handles action bar item clicks
        // Home/Up button clicks are handled automatically as long as there's a parent activity in AndroidManifest.xml
        int itemID = item.getItemId();
        if(itemID == R.id.action_refresh){
            FetchWeatherTask weatherTask = new FetchWeatherTask();

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
//                weatherTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            weatherTask.execute("94043");
            return true;
        }

        // super calls the parent class method. In this case it's the MainActivity.java method
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        List<String> weatherArray = new ArrayList<String>();
//        weatherArray.add("Today-Sunny-88/63");
//        weatherArray.add("Tomorrow-Foggy-70/46");
//        weatherArray.add("Weds-Cloudy-872/63");
//        weatherArray.add("Thurs-Rainy-64/53");
//        weatherArray.add("Friday-Foggy-70/46");
//        weatherArray.add("Sat-Sunny-90/75");

        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weatherArray);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView weeklyForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        weeklyForecast.setAdapter(mForecastAdapter);
        weeklyForecast.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                String forecast = mForecastAdapter.getItem(position);
                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    // Set connection to API socket here
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String Log_tag = FetchWeatherTask.class.getSimpleName();

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */

        private String getReadableDateString(long time) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(Log_tag, "Forecast entry: " + s);
            }
            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader inputReader = null;

            String forecastDataInJSON = null;
            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {

                final String BASE_PARAM = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNIT_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "appid";

                // Basically concatenating strings together
                // Allows for dynamic appending ?
                Uri builtUri = Uri.parse(BASE_PARAM).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNIT_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();

                // URL takes a string as an argument
                URL url = new URL(builtUri.toString());

                Log.v(Log_tag, "Built String: " + builtUri.toString());

                // Request the OpenWeatherMap connection
                urlConnection = (HttpURLConnection) url.openConnection();

                // GET method to get information from the API
                // Can't connect on main thread, need a background thread
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Input stream from the URL in JSON format
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                // InputStream is empty -- no information to get that or connection failed
                if (inputStream == null) {
                    return null;
                }
                inputReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = inputReader.readLine()) != null) {
                    buffer.append(line + '\n');
                }

                if (buffer.length() == 0) {
                    return null;
                }
                // Put the information in the Json string
                forecastDataInJSON = buffer.toString();
//
//                // Print out JSON string (The weather data)
//                Log.v(Log_tag, "Forecast JSON string: " + forecastDataInJSON);

            } catch (IOException e) {
                // If there's a problem with connecting to the API then print error to the TomCat Logs
                Log.e(Log_tag, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {

                    // Close the connection once it's done filling the string info
                    urlConnection.disconnect();
                }
                if (inputReader != null) {
                    try {
                        inputReader.close();
                    } catch (final IOException e) {
                        Log.e(Log_tag, "Error closing stream", e);
                    }
                }
            }
            try {
                return getWeatherDataFromJson(forecastDataInJSON, numDays);
            } catch (JSONException e) {
                Log.e(Log_tag, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if(strings != null){
                mForecastAdapter.clear();
                mForecastAdapter.addAll(strings);
            }
        }
    }

}
