package com.MMW.Catchup.api;

import com.MMW.Catchup.models.Directions;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GetDirectionInterface {


    /**
     * Generate token for forgot password...
     *
     * @param
     */
    @POST("/maps/api/directions/json?")
    Call<Directions> getDirections(@Query("origin") String origin,
                                   @Query("destination") String destination,
                                   @Query("key") String apiKey,
                                   @Query("alternatives") boolean alternatives);

}
