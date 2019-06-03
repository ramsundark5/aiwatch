package com.aiwatch.firebase;

import android.content.Context;
import com.aiwatch.Logger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class FirebaseAuthManager {

    private static final Logger LOGGER = new Logger();

    public FirebaseUser getFirebaseUser(Context context) {
        try{
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            LOGGER.d("firebaseAuthWithGoogle:" + account.getId());
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            AuthResult authResult = mAuth.signInWithCredential(credential).getResult();
            FirebaseUser firebaseUser = authResult.getUser();
            return firebaseUser;
        }catch(Exception e){
            LOGGER.e(e, "error getting firebaseauth user");
        }
        return null;
    }
}
