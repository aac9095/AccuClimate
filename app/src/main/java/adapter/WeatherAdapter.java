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
import android.widget.ImageView;
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

    private static int VIEW_TYPE_TODAY = 0;
    private static int VIEW_TYPE_FUTURE_DAY = 1;
    private Cursor mCursor;
    private Context mContext;
    private boolean mTwoPane;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView date,weather,min,max;
        ImageView icon;
        public ViewHolder(View itemView) {
            super(itemView);
            date =(TextView) itemView.findViewById(R.id.list_item_date_textview);
            weather = (TextView) itemView.findViewById(R.id.list_item_forecast_textview);
            min = (TextView) itemView.findViewById(R.id.list_item_low_textview);
            max = (TextView) itemView.findViewById(R.id.list_item_high_textview);
            icon = (ImageView) itemView.findViewById(R.id.list_item_icon);
        }
    }



    // Provide a suitable constructor (depends on the kind of dataset)
    public WeatherAdapter(Context context, Cursor c) {
        mContext = context;
        mCursor = c;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        Cursor cursor = mCursor;
        int layoutId = -1;
        if(viewType==VIEW_TYPE_TODAY)
            layoutId = R.layout.list_item_forecast_today;
        else if(viewType==VIEW_TYPE_FUTURE_DAY)
            layoutId = R.layout.list_item_forecast;
        View v = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Cursor cursor = mCursor;
        cursor.moveToPosition(position);
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        //Log.e("weather Id",""+weatherId);
        if(getItemViewType(cursor.getPosition())==VIEW_TYPE_TODAY)
            holder.icon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        else
            holder.icon.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
        long dateId = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        if(getItemViewType(cursor.getPosition())==VIEW_TYPE_TODAY)
            holder.date.setText(Utility.getFormattedMonthDay(mContext,dateId));
        else
            holder.date.setText(Utility.getDayName(mContext,dateId));
        String weatherDesc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        holder.weather.setText(weatherDesc);
        boolean isMetric = Utility.isMetric(mContext);
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);

        holder.max.setText(Utility.formatTemperature(mContext,high, isMetric));
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        holder.min.setText(Utility.formatTemperature(mContext,low, isMetric));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor= mCursor;
                cursor.moveToPosition(position);
                if (!cursor.isClosed()) {
                    String locationSetting = Utility.getPreferredLocation(mContext);
                    ((ForecastFragment.Callback) mContext)
                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(ForecastFragment.COL_WEATHER_DATE)
                            ));
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    @Override
    public int getItemViewType(int position) {
        if(position == VIEW_TYPE_TODAY && mTwoPane)
            return VIEW_TYPE_TODAY;
        else
            return VIEW_TYPE_FUTURE_DAY;
    }
    public void swapCursor(Cursor cursor){
        mCursor=cursor;
        notifyDataSetChanged();
    }

    public void setmTwoPane(boolean isTwoPane){
        mTwoPane=isTwoPane;
    }

    public Cursor getCursor(){
        return mCursor;
    }
}
