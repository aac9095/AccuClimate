package retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Ayush on 07-07-2016.
 */
public interface OpenWeatherAPI {
    String ENDPOINT = "http://api.openweathermap.org";

    @GET("/data/2.5/forecast/daily?&mode=json&&cnt=7")
    Call<OpenWeather> getAPPID(
            @Query("q") String postal,
            @Query("units") String unit,
            @Query("APPID") String APPID);
}
