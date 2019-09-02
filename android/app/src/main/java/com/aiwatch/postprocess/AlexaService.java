package com.aiwatch.postprocess;
import com.aiwatch.models.AlexaNotifyRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AlexaService {

    @POST("v1/NotifyMe")
    Call<ResponseBody> notifyAlexa(@Body AlexaNotifyRequest alexaNotifyRequest);
}
