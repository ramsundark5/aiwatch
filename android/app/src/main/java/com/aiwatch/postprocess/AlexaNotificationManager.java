package com.aiwatch.postprocess;

import com.aiwatch.Logger;
import com.aiwatch.media.db.SettingsDao;
import com.aiwatch.models.AlarmEvent;
import com.aiwatch.models.AlexaNotifyRequest;
import com.aiwatch.models.Settings;

import retrofit2.Retrofit;

public class AlexaNotificationManager {

    private static final Logger LOGGER = new Logger();
    private static final String BASE_URL = "https://api.notifymyecho.com/";

    public static void notifyAlexa(AlarmEvent alarmEvent){
        try{
            SettingsDao settingsDao = new SettingsDao();
            Settings settings = settingsDao.getSettings();
            String accessToken = settings.getAlexaToken();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .build();
            AlexaService alexaService = retrofit.create(AlexaService.class);
            if( accessToken != null){
                AlexaNotifyRequest alexaNotifyRequest = new AlexaNotifyRequest();
                alexaNotifyRequest.setAccessCode(accessToken);
                //alexaNotifyRequest.setTitle("Aiwatch detected new event at "+alarmEvent.getDate().getHours() + "." + alarmEvent.getDate().getMinutes());
                alexaNotifyRequest.setNotification(alarmEvent.getMessage());
                alexaService.notifyAlexa(alexaNotifyRequest);
            }
        }catch (Exception e){
            LOGGER.e(e, "Error notifying alexa");
        }
    }
}
