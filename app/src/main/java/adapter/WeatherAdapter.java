package adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ayush.sunshine.app.DetailActivity;
import com.example.ayush.sunshine.app.ForecastFragment;
import com.example.ayush.sunshine.app.R;
import com.example.ayush.sunshine.app.data.WeatherContract;

import java.util.ArrayList;

import util.Temp;

/**
 * Created by Ayush on 07-07-2016.
 */
public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    private ArrayList<String> mDataset;
    public CursorAdapter mCursorAdapter;
    private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        View v1;

        public ViewHolder(View itemView) {
            super(itemView);
            v1 = itemView.findViewById(R.id.list_item_forecast_textview);
        }
    }

    public void add(int position, String item) {
        mDataset.add(position, item);
        notifyItemInserted(position);
    }

    public void remove() {
        mDataset.clear();
        notifyDataSetChanged();
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public WeatherAdapter(Context context, Cursor c,int flags) {
        mContext = context;
        mCursorAdapter = new CursorAdapter(mContext,c,flags) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
                return view;
            }

            @Override
            public void bindView(View view, Context context, final Cursor cursor) {
                // our view is pretty simple here --- just a text view
                // we'll keep the UI functional with a simple (and slow!) binding.

                TextView tv = (TextView)view;
                tv.setText(convertCursorRowToUXFormat(cursor));
            }
        };
    }

    // Create new views (invoked by the layout manager)
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        mCursorAdapter.getCursor().moveToPosition(position); //EDITED: added this line as suggested in the comments below, thanks :)
        mCursorAdapter.bindView(holder.v1, mContext, mCursorAdapter.getCursor());
        mCursorAdapter.getCursor().moveToPosition(position);
        final Cursor cursor = mCursorAdapter.getCursor();
        final String text = (String) ((TextView)holder.v1).getText();
        holder.v1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,text,Toast.LENGTH_SHORT).show();
                if (!cursor.isClosed()) {
                    String locationSetting = Utility.getPreferredLocation(mContext);
                    Intent intent = new Intent(mContext.getApplicationContext(), DetailActivity.class)
                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(ForecastFragment.COL_WEATHER_DATE)
                            ));
                    mContext.startActivity(intent);
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor

        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

}
