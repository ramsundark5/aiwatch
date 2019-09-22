package com.aiwatch.postprocess;

import com.aiwatch.Logger;
import com.aiwatch.media.db.SettingsDao;
import com.aiwatch.models.Settings;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SmartthingsNotificationManager {

    private static final Logger LOGGER = new Logger();
    private static final String BASE_URL = "https://graph.api.smartthings.com";
    private SmartthingsService smartthingsService;


    public void notifyHub(){
        SettingsDao settingsDao = new SettingsDao();
        Settings settings = settingsDao.getSettings();
        String accessToken = settings.getSmartthingsAccessToken();
        if(accessToken == null || accessToken.isEmpty()){
            return;
        }
        String endpoint = settings.getSmartAppEndpoint();
        String smartAppUrl = endpoint + "/";//BASE_URL + endpoint + "/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(smartAppUrl)
                .build();
        smartthingsService = retrofit.create(SmartthingsService.class);
        String authToken = "Bearer " + accessToken;
        Call<ResponseBody> result = smartthingsService.updateSwitch("on", authToken);
        result.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int statusCode = response.code();
                if (response.isSuccessful()) {
                    LOGGER.d("smartthings hub notified succesfully with status code "+statusCode);
                }else{
                    LOGGER.d("smartthings hub notification returned failure response with status code "+statusCode);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                LOGGER.e(t, "Error notifying smartthings hub");
            }
        });
    }
}
