package com.aiwatch.postprocess;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface SmartthingsService {

    @PUT("switches/{command}")
    Call<ResponseBody> updateSwitch(@Path("command") String command, @Header("Authorization") String authHeader);
}
