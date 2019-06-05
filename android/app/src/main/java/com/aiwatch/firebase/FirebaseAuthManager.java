package com.aiwatch.firebase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.aiwatch.Logger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.android.gms.tasks.Tasks;

public class FirebaseAuthManager {

    private static final Logger LOGGER = new Logger();
    private static final long TIMEOUT_API = 10;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    public FirebaseUser getFirebaseUser(Context context) {
        try{
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Executor mExecutor = Executors.newSingleThreadExecutor();
            LOGGER.d("firebaseAuthWithGoogle:" + account.getId());
            Future<AuthResult> authResultFuture = executorService.submit(new Callable(){
                public AuthResult call() {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    Task<AuthResult> authResultTask = mAuth.signInWithCredential(credential);
                    AuthResult authResult = null;
                    try{
                        authResult = Tasks.await(authResultTask, TIMEOUT_API, TIME_UNIT);
                    }catch(Exception e){
                        LOGGER.e(e, "error getting authresult");
                    }
                    return authResult;
                }
            });
            AuthResult authResult = authResultFuture.get();
            return authResult.getUser();

            /*FirebaseAuth mAuth = FirebaseAuth.getInstance();
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(mExecutor, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                LOGGER.d("FirebaseUser:" + user);
                            }
                        }
                    });
           return null;*/
        }catch(Exception e){
            LOGGER.e(e, "error getting firebaseauth user");
        }
        return null;
    }
}
