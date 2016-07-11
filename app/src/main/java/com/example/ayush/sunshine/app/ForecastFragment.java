package com.example.ayush.sunshine.app;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ayush.sunshine.app.data.WeatherContract;

import java.util.ArrayList;

import adapter.Utility;
import adapter.WeatherAdapter;
/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int FORECAST_LOADER = 0;
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    public View rootView;
    public ArrayList<String> arrayList;
    private String postal;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private WeatherAdapter mAdapter;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    static public final int COL_WEATHER_DESC = 2;
    static public final int COL_WEATHER_MAX_TEMP = 3;
    static public final int COL_WEATHER_MIN_TEMP = 4;
    static public final int COL_LOCATION_SETTING = 5;
    static public final int COL_WEATHER_CONDITION_ID = 6;
    static public final int COL_COORD_LAT = 7;
    static public final int COL_COORD_LONG = 8;
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
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_forecast);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new WeatherAdapter(getActivity(), null, 0);
        updateWeather();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
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
        postal = preferences.getString(getString(R.string.pref_location_key)
                ,getString(R.string.pref_location_default));
        //Toast.makeText(getActivity(),LOG_TAG+" updatedWeather()",Toast.LENGTH_SHORT).show();
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getContext());
        mRecyclerView.setAdapter(mAdapter);
    }


//    @Override
//    public void onStart() {
//        super.onStart();
//        updateWeather();
//    }

    @Override
    public void onResume() {
        super.onResume();
        updateWeather();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
             String locationSetting = Utility.getPreferredLocation(getActivity());

                     // Sort order:  Ascending, by date.
                             String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE +  " ASC";
             Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                             locationSetting, System.currentTimeMillis());

                     return new CursorLoader(getActivity(),
                             weatherForLocationUri,
                             FORECAST_COLUMNS,
                             null,
                             null,
                             sortOrder);
         }

             @Override
     public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
             mAdapter.mCursorAdapter.swapCursor(cursor);
         }

             @Override
     public void onLoaderReset(Loader<Cursor> cursorLoader) {
             mAdapter.mCursorAdapter.swapCursor(null);
         }

    public void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER,null,this);
    }

}
