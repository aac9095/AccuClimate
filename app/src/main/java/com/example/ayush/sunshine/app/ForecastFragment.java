package com.example.ayush.sunshine.app;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ayush.sunshine.app.data.WeatherContract;
import com.example.ayush.sunshine.app.sync.SunshineSyncAdapter;

import java.util.ArrayList;

import adapter.Utility;
import adapter.WeatherAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,SharedPreferences.OnSharedPreferenceChangeListener{

    private static final int FORECAST_LOADER = 0;
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    public View rootView;
    public ArrayList<String> arrayList;
    private String postal;
    private RecyclerView mRecyclerView;
    private TextView emptyView;
    private LinearLayoutManager mLayoutManager;
    private WeatherAdapter mAdapter;
    public static int mPosition = RecyclerView.NO_POSITION;
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



    private static final String SELECTED_KEY = "selected_position";

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
        emptyView = (TextView) rootView.findViewById(R.id.empty_view);
        arrayList = new ArrayList<String>();
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_forecast);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new WeatherAdapter(getActivity(), null);
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        updateWeather();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
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
        else if(id == R.id.action_map){
            openPreferredLocationInMap();
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateWeather(){
        SunshineSyncAdapter.syncImmediately(getActivity());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        sp.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        sp.unregisterOnSharedPreferenceChangeListener(this);
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
        if(cursor.getCount()>0){
            emptyView.setVisibility(View.GONE);
            mAdapter.swapCursor(cursor);
            if (mPosition != RecyclerView.NO_POSITION) {
                // If we don't need to restart the loader, and there's a desired position to restore
                // to, do so now.
                mRecyclerView.smoothScrollToPosition(mPosition);
            }
        }
        else{
            updateEmptyView();
        }
    }

    @Override
     public void onLoaderReset(Loader<Cursor> cursorLoader) {
             mAdapter.swapCursor(null);
         }

    public void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER,null,this);
    }

    public void setOnType(boolean viewType){
        if(mAdapter!=null)
            mAdapter.setmTwoPane(viewType);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_location_status_key))){
            updateEmptyView();
        }
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if ( null != mAdapter ) {
            Cursor c = mAdapter.getCursor();
            if ( null != c ) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_COORD_LAT);
                String posLong = c.getString(COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);
                Log.e(LOG_TAG,geoLocation.toString());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }
    private void updateEmptyView(){
        @SunshineSyncAdapter.LocationStatus int location = Utility.getLocationStatus(getContext());
        switch (location){
            case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN :
                emptyView.setText(getString(R.string.empty_forecast_list_server_down));
                break;
            case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID :
                emptyView.setText(getString(R.string.empty_forecast_list_server_error));
                break;
            case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                emptyView.setText(getString(R.string.empty_forecast_list_location_invalid));
                break;
            default:
                if(!Utility.isNetworkAvailable(getContext())){
                    emptyView.setText(getString(R.string.no_connection));
                }
        }
    }
}
