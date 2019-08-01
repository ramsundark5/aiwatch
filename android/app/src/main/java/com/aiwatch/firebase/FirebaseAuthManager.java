package com.aiwatch.firebase;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.models.Settings;
import com.aiwatch.media.db.SettingsDao;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
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
            if(settings == null || !settings.isGoogleAccountConnected()){
                return null;
            }
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            if(account != null){
                LOGGER.d("firebaseAuthWithGoogle:" + account.getId());
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                Task<AuthResult> authResultTask = mAuth.signInWithCredential(credential);
                AuthResult authResult = Tasks.await(authResultTask);
                firebaseUser = authResult.getUser();
            }
        }catch(Exception e){
            LOGGER.e(e, "Error getting firebaseauth user");
        }
        return firebaseUser;
    }
}
