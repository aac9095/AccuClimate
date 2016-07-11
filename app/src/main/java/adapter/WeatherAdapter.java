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
    private static final int VIEW_TYPE_COUNT = 2;
    public CursorAdapter mCursorAdapter;
    private Context mContext;

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
    public WeatherAdapter(Context context, Cursor c,int flags) {
        mContext = context;
        mCursorAdapter = new CursorAdapter(mContext,c,flags) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                if(cursor.getPosition()==-1)
                    cursor.moveToFirst();
                int viewType = getItemViewType(cursor.getPosition());
                int layoutId = -1;
                if(viewType==VIEW_TYPE_TODAY)
                    layoutId = R.layout.list_item_forecast_today;
                else if(viewType==VIEW_TYPE_FUTURE_DAY)
                    layoutId = R.layout.list_item_forecast;
                View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
                return view;
            }

            @Override
            public void bindView(View itemView, Context context, final Cursor cursor) {
                // our view is pretty simple here --- just a text view
                // we'll keep the UI functional with a simple (and slow!) binding.

                TextView date,weather,min,max;
                ImageView icon;

                date =(TextView) itemView.findViewById(R.id.list_item_date_textview);
                weather = (TextView) itemView.findViewById(R.id.list_item_forecast_textview);
                min = (TextView) itemView.findViewById(R.id.list_item_low_textview);
                max = (TextView) itemView.findViewById(R.id.list_item_high_textview);
                icon = (ImageView) itemView.findViewById(R.id.list_item_icon);

                int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
                icon.setImageResource(R.drawable.ic_launcher);
                long dateId = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
                date.setText(Utility.getDayName(mContext,dateId));
                String weatherDesc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
                weather.setText(weatherDesc);
                boolean isMetric = Utility.isMetric(mContext);
                double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
                max.setText(Utility.formatTemperature(high, isMetric) + "/");
                double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
                min.setText(Utility.formatTemperature(low, isMetric));
            }

            @Override
            public int getViewTypeCount() {
                return VIEW_TYPE_COUNT;
            }

            @Override
            public int getItemViewType(int position) {
                Log.e("getItemViewType: ",""+position);
                if(position == VIEW_TYPE_TODAY)
                    return VIEW_TYPE_TODAY;
                else
                    return VIEW_TYPE_FUTURE_DAY;
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
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
        Cursor cursor = mCursorAdapter.getCursor();
        cursor.moveToPosition(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = mCursorAdapter.getCursor();
                cursor.moveToPosition(position);
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

    @Override
    public int getItemViewType(int position) {
        return mCursorAdapter.getItemViewType(position);
    }
}
