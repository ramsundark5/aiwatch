package com.aiwatch.cloud.gdrive;

import android.content.Context;

import com.aiwatch.Logger;
import com.aiwatch.firebase.FirebaseAuthManager;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.gson.GsonFactory;


public class GdriveManager {

    private static final Logger LOGGER = new Logger();

    public static GDriveServiceHelper getGDriveServiceHelper(Context context){
        GDriveServiceHelper driveServiceHelper = null;
        try{
            FirebaseAuthManager firebaseAuthManager = new FirebaseAuthManager();
            TokenResponse tokenResponse = firebaseAuthManager.getGoogleAccessToken();
            GoogleCredential credential = new GoogleCredential().setFromTokenResponse(tokenResponse);
            com.google.api.services.drive.Drive googleDriveService =
                    new com.google.api.services.drive.Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName("aiwatch")
                            .build();
            driveServiceHelper = new GDriveServiceHelper(googleDriveService);
        } catch(Exception e){
            LOGGER.e(e, "Error getting gdriveservice helper");
        }
        return driveServiceHelper;
    }
}
