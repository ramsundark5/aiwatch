package com.aiwatch.firebase;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.common.AppConstants;
import com.aiwatch.models.Settings;
import com.aiwatch.media.db.SettingsDao;
import com.google.android.gms.tasks.Task;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.tasks.Tasks;

public class FirebaseAuthManager {

    private static final Logger LOGGER = new Logger();

    // Do not call this function from the main thread. Otherwise,
    // an IllegalStateException will be thrown.
    public FirebaseUser getFirebaseUser(Context context) {
        FirebaseUser firebaseUser = null;
        try{
            SettingsDao settingsDao = new SettingsDao();
            Settings settings = settingsDao.getSettings();
            if(settings == null || !settings.isGoogleAccountConnected() || settings.getGoogleRefreshToken() == null){
                return null;
            }
            String refreshToken = settings.getGoogleRefreshToken();
            if(refreshToken != null){
                LOGGER.d("firebaseAuthWithGoogle:" + refreshToken);
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String accessToken = refreshAccessToken(refreshToken);
                AuthCredential credential = GoogleAuthProvider.getCredential(null, accessToken);
                Task<AuthResult> authResultTask = mAuth.signInWithCredential(credential);
                AuthResult authResult = Tasks.await(authResultTask);
                firebaseUser = authResult.getUser();
            }
        }catch(Exception e){
            LOGGER.e(e, "Error getting firebaseauth user");
        }
        return firebaseUser;
    }

    public String refreshAccessToken(String refreshToken )  {
        String accessToken = null;
        try {
            TokenResponse response =
                    new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                            refreshToken, AppConstants.GOOOGLE_API_CLIENT_ID, null).execute();
            accessToken = response.getAccessToken();
            LOGGER.d("Access token: " + response.getAccessToken());
        } catch (Exception e) {
            LOGGER.e(e, "Error getting refresh token");
        }
        return accessToken;
    }
}
