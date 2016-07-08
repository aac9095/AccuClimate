package com.example.ayush.sunshine.app;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import adapter.WeatherAdapter;
import retrofit.OpenWeather;
import retrofit.OpenWeatherAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment implements Callback<OpenWeather> {

    public View rootView;
    public ArrayList<String> arrayList;
    private String APPID ;
    private String postal;
    private String unit;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private WeatherAdapter mAdapter;
    private Gson gson;
    private Retrofit retrofit;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        arrayList = new ArrayList<String>();

        APPID = getArguments().getString("appID");
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl(OpenWeatherAPI.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_forecast);

        updateWeather();

        return rootView;
    }

    @Override
    public void onResponse(Call<OpenWeather> call, Response<OpenWeather> response) {
        int code = response.code();
        if (code == 200) {
            OpenWeather openWeather = response.body();
            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();
            arrayList.clear();
            for(int i=0;i<openWeather.getCnt();i++){
                long dateTime;
                String day,weather;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);
                weather= String.valueOf(day)+" - " + openWeather.getListArray().get(i).getWeather().get(0).getMain()
                        + " - " + Math.round(openWeather.getListArray().get(i).getTemp().getMax()) + "/"
                        + Math.round(openWeather.getListArray().get(i).getTemp().getMin());
                arrayList.add(i,weather);
            }

            setRecyclerView(arrayList);

        } else {
            Toast.makeText(getActivity(), "Did not work: " + String.valueOf(code), Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onFailure(Call<OpenWeather> call, Throwable t) {
        Toast.makeText(getActivity(), "Nope", Toast.LENGTH_LONG).show();
        Log.e("Throwable ",t.toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_refresh) {
            Toast.makeText(getActivity(), "Refresh", Toast.LENGTH_SHORT).show();
            updateWeather();
        }
        else if(id == R.id.action_settings)
            startActivity(new Intent(getActivity(),SettingsActivity.class));
        else if(id == R.id.action_map){
            Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",
                    postal).build();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(geoLocation);
            if(intent.resolveActivity(getActivity().getPackageManager())!=null)
                startActivity(intent);
            else
                Log.e("Could not call ",postal);
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateWeather(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        postal = preferences.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        unit = preferences.getString(getString(R.string.temperatureUnits_key),getString(R.string.temperateUnit_default));

        // prepare call in Retrofit 2.0
        OpenWeatherAPI openWeatherAPI = retrofit.create(OpenWeatherAPI.class);

        Call<OpenWeather> call = openWeatherAPI.getAPPID(postal,unit,APPID);
        //asynchronous call
        call.enqueue(this);
    }

    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    private void setRecyclerView(ArrayList<String> arrayList){

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new WeatherAdapter(arrayList);
            mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWeather();
    }
}
