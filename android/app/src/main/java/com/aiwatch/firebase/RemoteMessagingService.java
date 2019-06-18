package com.aiwatch.firebase;

import com.aiwatch.Logger;
import com.aiwatch.R;
import com.aiwatch.media.db.AlarmEvent;
import com.aiwatch.postprocess.NotificationManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import org.json.JSONObject;

import java.util.Date;
import java.util.Map;

public class RemoteMessagingService extends FirebaseMessagingService {

    private static final Logger LOGGER = new Logger();
    private Gson gson;
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        LOGGER.d("Refreshed token: " + token);
        FirebaseUserDataDao firebaseUserDataDao = new FirebaseUserDataDao();
        firebaseUserDataDao.registerFCMToken(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try{
            gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()))
                    .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (date, type, jsonSerializationContext) -> new JsonPrimitive(date.getTime()))
                    .create();
            if (remoteMessage.getData() != null) {
                Map<String, String> params = remoteMessage.getData();
                JSONObject jsonObject = new JSONObject(params);
                AlarmEvent alarmEvent = gson.fromJson(jsonObject.toString(), AlarmEvent.class);
                int smallIcon = R.drawable.ic_person_detect;
                if(alarmEvent.getMessage().contains("Animal")){
                    smallIcon = R.drawable.ic_animal_detect;
                }else if(alarmEvent.getMessage().contains("Vehicle")){
                    smallIcon = R.drawable.ic_vehicle_detect;
                }
                NotificationManager.sendStringNotification(getApplicationContext(), alarmEvent.getMessage(), smallIcon);
                //NotificationManager.sendImageNotification(getApplicationContext(), alarmEvent);
            }
        }catch (Exception e){
            LOGGER.e(e, "Error sending local notification.");
        }

    }
}
