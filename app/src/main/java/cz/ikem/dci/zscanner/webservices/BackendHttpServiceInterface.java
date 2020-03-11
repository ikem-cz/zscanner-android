package cz.ikem.dci.zscanner.webservices;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Multipart;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Query;
import retrofit2.http.Field;

public interface BackendHttpServiceInterface {
    @GET("v3.1/departments")
    Call<List<JsonObject>> getDepartments();

    @GET("v3.1/documenttypes")
    Call<JsonObject> getDocumentTypes(@Query("department") String department);

    @GET("v3.1/folders/search")
    Call<List<Patient>> searchPatients(@Query("query") String entered);

    @GET("v3.1/folders/decode")
    Call<Patient> decodePatient(@Query("query") String code);


//    @POST("v3.1/documents/summary")
//    Call<JSONObject> postDocumentSummary(
////            @Body RequestBody body);
//        @Field("correlation") String correlation,
//        @Field("folderInternalId") String indernalId,
//        @Field("documentType") String type,
//        @Field("documentSubType") String subtype,
//        @Field("department") String department,
//        @Field("pages") String numPages,
//        @Field("datetime") String datetime
//    );


    @POST("v3.1/documents/summary")
    Call<RequestBody> postDocumentSummary(
            @Body JSONObject body);

    @Multipart
    @POST("v3.1/documents/page")
    Call<ResponseBody> postDocumentPage(
        @Part List<MultipartBody.Part> images,
        @Part("correlation") RequestBody correlation,
        @Part("pageIndex") RequestBody pageNum,
        @Part ("description") RequestBody additionalNote
    );

    @FormUrlEncoded
    @POST("v3.1/login")
    Call<ResponseBody> postLogin(
        @Field("username") String username,
        @Field("password") String password
    );

    @FormUrlEncoded
    @POST("v3.1/logout")
    Call<ResponseBody> postLogout(
            @Field("access_token") String access_token
    );

}
