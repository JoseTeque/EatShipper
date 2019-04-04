package m.google.eatshipper.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface IGeoCordinates {

    @GET("maps/api/geocode/json")
    Call<String> getGeocode(@Query("address")String address, @Query("key")String key);

    @GET
    Call<String> ObtenerRuta(@Url String url);
}
