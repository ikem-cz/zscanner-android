package cz.ikem.dci.zscanner.webservices;

import com.google.gson.JsonObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
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

    @Multipart
    @POST("v3.1/documents/summary")
    Call<ResponseBody> postDocumentSummary(
        @Part("correlation") RequestBody correlation,
        @Part("folderInternalId") RequestBody cardid,
        @Part("documentType") RequestBody type,
        @Part("documentSubType") RequestBody subtype,
        @Part("pages") RequestBody numPages,
        @Part("datetime") RequestBody datetime,
        @Part("name") RequestBody name
    );

    @Multipart
    @POST("v3.1/documents/")
    Call<ResponseBody> postDocumentPage(
        @Part ("page") List<MultipartBody.Part> images,
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
