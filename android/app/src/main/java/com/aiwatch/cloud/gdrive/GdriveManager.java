package com.aiwatch.cloud.gdrive;

import android.content.Context;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import java.util.Collections;


public class GdriveManager {

    public static GDriveServiceHelper getGDriveServiceHelper(Context context){
        GDriveServiceHelper driveServiceHelper = null;
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if(account == null){
            throw new RuntimeException("Google drive account not associated");
        }
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());
        com.google.api.services.drive.Drive googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("aiwatch")
                        .build();
        driveServiceHelper = new GDriveServiceHelper(googleDriveService);
        return driveServiceHelper;
    }
}
