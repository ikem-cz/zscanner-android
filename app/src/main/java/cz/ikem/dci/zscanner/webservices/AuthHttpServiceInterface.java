package cz.ikem.dci.zscanner.webservices;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthHttpServiceInterface {
    @POST("password")
    Call<JsonObject> postPassword(@Body JsonObject json);

    @POST("status")
    Call<JsonObject> getStatus(@Body JsonObject json);
}