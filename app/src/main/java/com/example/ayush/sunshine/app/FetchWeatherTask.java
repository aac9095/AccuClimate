/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.ayush.sunshine.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.example.ayush.sunshine.app.data.WeatherContract;
import com.example.ayush.sunshine.app.data.WeatherContract.WeatherEntry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import adapter.WeatherAdapter;
import retrofit.OpenWeather;
import retrofit.OpenWeatherAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import util.City;
import util.Coord;
import util.ListArray;
import util.Temp;
import util.Weather;

public class FetchWeatherTask implements Callback<OpenWeather> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private Gson gson;
    private Retrofit retrofit;

    private WeatherAdapter mForecastAdapter;
    private final Context mContext;
    private String postal;

    public FetchWeatherTask(Context context) {
        mContext = context;
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl(OpenWeatherAPI.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        updateWeather();
    }



    private boolean DEBUG = true;

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */


    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        // Students: First, check if the location with this city name exists in the db
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI
        long locationId;
        Cursor locationCursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues locationValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            // Finally, insert location data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    locationValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            locationId = ContentUris.parseId(insertedUri);
        }

        locationCursor.close();
        // Wait, that worked?  Yes!
        return locationId;
    }

    /*
        Students: This code will allow the FetchWeatherTask to continue to return the strings that
        the UX expects so that we can continue to test the application even once we begin using
        the database.
     */

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    //locationSetting -> postal code
    private void getWeatherDataFromRetrofit(OpenWeather openWeather, String locationSetting){

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        locationSetting=postal;
        //JSONObject forecastJson = new JSONObject(forecastJsonStr);
        //JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        List<ListArray> listArray = openWeather.getListArray();
        
        //JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        City city = openWeather.getCity();
        String cityName = city.getName();

        //JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
        Coord coord = city.getCoord();
        double cityLatitude = coord.getLat();
        double cityLongitude = coord.getLon();

        long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

        // Insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(listArray.size());

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

        for(int i = 0; i < listArray.size(); i++) {
            // These are the values that will be collected.
            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            String description;
            int weatherId;

            // Get the JSON object representing the day
            ListArray dayForecast = listArray.get(i);

            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);

            pressure = dayForecast.getPressure();
            humidity = dayForecast.getHumidity();
            windSpeed = dayForecast.getSpeed();
            windDirection = dayForecast.getDeg();

            // Description is in a child array called "weather", which is 1 element long.
            // That element also contains a weather code.
            //JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            Weather weather = dayForecast.getWeather().get(0);
            description = weather.getMain();
            weatherId = weather.getId();

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            //JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            Temp temp = dayForecast.getTemp();
            high = temp.getMax();
            low = temp.getMin();

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherEntry.COLUMN_DATE, dateTime);
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            cVVector.add(weatherValues);
        }

        int inserted = 0;
        // add to database
        if ( cVVector.size() > 0 ) {
            // Student: call bulkInsert to add the weatherEntries to the database here
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, cvArray);
        }
        Log.d(LOG_TAG, "FetchWeatherTask Complete. " + inserted + " Inserted");

    }

    @Override
    public void onResponse(Call<OpenWeather> call, Response<OpenWeather> response) {
        int code = response.code();
        if (code == 200) {
            OpenWeather openWeather = response.body();
            //Toast.makeText(mContext,"Count: "+openWeather.getCnt(),Toast.LENGTH_SHORT).show();
            getWeatherDataFromRetrofit(openWeather,postal);
        } else {
            Toast.makeText(mContext, "Did not work: " + String.valueOf(code), Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onFailure(Call<OpenWeather> call, Throwable t) {
        Toast.makeText(mContext, "Nope", Toast.LENGTH_LONG).show();
        Log.e("Throwable ",t.toString());
    }

    public void updateWeather(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        postal = preferences.getString(mContext.getString(R.string.pref_location_key)
                ,mContext.getString(R.string.pref_location_default));
        String unit = preferences.getString(mContext.getString(R.string.pref_units_key)
                ,mContext.getString(R.string.pref_units_metric));
        
        // prepare call in Retrofit 2.0
        OpenWeatherAPI openWeatherAPI = retrofit.create(OpenWeatherAPI.class);

        Call<OpenWeather> call = openWeatherAPI.getAPPID(postal,unit,MainActivity.APPID);
        //asynchronous call
        call.enqueue(this);
    }
    
}