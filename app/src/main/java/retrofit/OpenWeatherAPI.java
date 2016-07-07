package retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Ayush on 07-07-2016.
 */
public interface OpenWeatherAPI {
    String ENDPOINT = "http://api.openweathermap.org";

    @GET("data/2.5/forecast/daily?q=201301&mode=json&units=metric&cnt=7&APPID={APPID}")
    Call<OpenWeather> getAPPID(@Path("APPDID") String APPID);
}
