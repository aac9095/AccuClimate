package com.example.ayush.sunshine.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import adapter.WeatherAdapter;
import retrofit.OpenWeather;
import retrofit.OpenWeatherAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    static public String APPID = "ec248db971fd7d55ce5f8404a896e255";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements Callback<OpenWeather> {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ArrayList<String> arrayList = new ArrayList<String>();

            arrayList.add(0,"Mon 6/23 - Sunny - 31/17");
            arrayList.add(1,"Tue 6/24 - Foggy - 21/8");
            arrayList.add(2,"Wed 6/25 - Cloudy - 22/17");
            arrayList.add(3,"Thurs 6/26 - Rainy - 18/11");
            arrayList.add(4,"Fri 6/27 - Foggy - 21/10");
            arrayList.add(5,"Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18");
            arrayList.add(6,"Sun 6/29 - Sunny - 20/7");

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(OpenWeatherAPI.ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            // prepare call in Retrofit 2.0
            OpenWeatherAPI openWeatherAPI = retrofit.create(OpenWeatherAPI.class);

            Call<OpenWeather> call = openWeatherAPI.getAPPID(APPID);
            //asynchronous call
            call.enqueue(this);

            RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_forecast);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            WeatherAdapter mAdapter = new WeatherAdapter(arrayList);
            mRecyclerView.setAdapter(mAdapter);

            return rootView;
        }

        @Override
        public void onResponse(Call<OpenWeather> call, Response<OpenWeather> response) {
            int code = response.code();
            if (code == 200) {
                OpenWeather openWeather = response.body();
                Toast.makeText(getActivity(), "Got the user: " + openWeather.getCnt(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Did not work: " + String.valueOf(code), Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void onFailure(Call<OpenWeather> call, Throwable t) {
            Toast.makeText(getActivity(), "Nope", Toast.LENGTH_LONG).show();

        }

    }
}
