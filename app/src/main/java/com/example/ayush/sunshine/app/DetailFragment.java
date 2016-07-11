package com.example.ayush.sunshine.app;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ayush.sunshine.app.data.WeatherContract;

import adapter.Utility;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;
    private static final String[] FORECAST_COLUMNS = {
                             WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                             WeatherContract.WeatherEntry.COLUMN_DATE,
                             WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                             WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                             WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                     };

     // these constants correspond to the projection defined above, and must change if the
     // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;

    private String weather;
    private ShareActionProvider mShareActionProvider;
    private String mForecast;
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detailfragment,menu);
        MenuItem item = menu.findItem(R.id.share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(),SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public Intent createShareForecastIntent(){
        Intent mShareIntent = new Intent(Intent.ACTION_SEND);
        mShareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        mShareIntent.setType("text/plain");
        mShareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + " #Sunshine");
        return mShareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        Intent intent = getActivity().getIntent();
        if (intent==null)
            return null;

        return new CursorLoader(getActivity(),
                intent.getData(),
                FORECAST_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
                     if (!data.moveToFirst()) { return; }

                             String dateString = Utility.formatDate(
                                     data.getLong(COL_WEATHER_DATE));

                             String weatherDescription =
                                     data.getString(COL_WEATHER_DESC);

                             boolean isMetric = Utility.isMetric(getActivity());

                             String high = Utility.formatTemperature(
                                     data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

                             String low = Utility.formatTemperature(
                                     data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

                             mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

                             TextView detailTextView = (TextView)getView().findViewById(R.id.detail_view);
                     detailTextView.setText(mForecast);

                             // If onCreateOptionsMenu has already happened, we need to update the share intent now.
                                     if (mShareActionProvider != null) {
                             mShareActionProvider.setShareIntent(createShareForecastIntent());
                         }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

}
