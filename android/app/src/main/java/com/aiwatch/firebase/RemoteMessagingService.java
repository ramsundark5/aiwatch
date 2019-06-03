package com.aiwatch.firebase;

import com.aiwatch.Logger;
import com.google.firebase.messaging.FirebaseMessagingService;

public class RemoteMessagingService extends FirebaseMessagingService {

    private static final Logger LOGGER = new Logger();
    private FirebaseUserDataManager firebaseUserDataManager = new FirebaseUserDataManager();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        LOGGER.d("Refreshed token: " + token);
        firebaseUserDataManager.sendTokenToDB(getApplicationContext(), token);
    }

}
