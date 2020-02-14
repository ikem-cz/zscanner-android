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
    @GET("/medicalc/v3.1/departments")
    Call<List<JsonObject>> getDepartments();

    @GET("/medicalc/v3.1/documenttypes")
    Call<List<JsonObject>> getDocumentTypes();

    @GET("/medicalc/v3.1/folders/search")
    Call<List<Patient>> searchPatients(@Query("query") String entered);

    @GET("/medicalc/v3.1/folders/decode")
    Call<Patient> decodePatient(@Query("query") String code);

    @Multipart
    @POST("/medicalc/v3.1/documents/summary")
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
    @POST("/medicalc/v3.1/documents/page")
    Call<ResponseBody> postDocumentPage(
            @Part ("page") List<MultipartBody.Part> images,
            @Part("correlation") RequestBody correlation,
            @Part("pageIndex") RequestBody pageNum,
            @Part ("description") RequestBody additionalNote
    );
}
