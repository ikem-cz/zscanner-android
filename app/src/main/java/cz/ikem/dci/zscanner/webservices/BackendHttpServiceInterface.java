package cz.ikem.dci.zscanner.webservices;

import com.google.gson.JsonObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface BackendHttpServiceInterface {
    @GET("api-zscanner/v2/documenttypes")
    Call<List<JsonObject>> getDocumentTypes();

    @GET("api-zscanner/v2/patients/search")
    Call<List<Patient>> searchPatients(@Query("query") String entered);

    @GET("api-zscanner/v2/patients/decode")
    Call<Patient> decodePatient(@Query("query") String code);

    @Multipart
    @POST("api-zscanner/v2/documents/summary")
    Call<ResponseBody> postDocumentSummary(
            @Part("correlation") RequestBody correlation,
            @Part("patid") RequestBody cardid,
            @Part("mode") RequestBody mode,
            @Part("type") RequestBody type,
            @Part("pages") RequestBody numPages,
            @Part("datetime") RequestBody datetime,
            @Part("name") RequestBody name,
            @Part("notes") RequestBody notes
    );

    @Multipart
    @POST("api-zscanner/v2/documents/page")
    Call<ResponseBody> postDocumentPage(
            @Part("correlation") RequestBody correlation,
            @Part("page") RequestBody pageNum,
            @Part List<MultipartBody.Part> images
    );
}
