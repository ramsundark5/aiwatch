package com.aiwatch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.support.v4.content.ContextCompat;

import com.aiwatch.firebase.FirebaseUserDataManager;
import com.facebook.react.ReactActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends ReactActivity {

    private static final Logger LOGGER = new Logger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        startMonitoringService();
        registerFCMToken();
    }

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "aiwatch";
    }

    private void startMonitoringService(){
        try{
            Intent monitoringIntent = new Intent(getApplicationContext(), MonitoringService.class);
            ContextCompat.startForegroundService(getApplicationContext(), monitoringIntent);
            LOGGER.d("Invoking monitoring from main activity");
        }catch(Exception e){
            LOGGER.e("error starting monitoring service "+e.getMessage());
        }
    }

    private void registerFCMToken(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Task<InstanceIdResult> firebaseInstanceIdTask = FirebaseInstanceId.getInstance().getInstanceId();
                try {
                    InstanceIdResult instanceIdResult = Tasks.await(firebaseInstanceIdTask);
                    String token = instanceIdResult.getToken();
                    FirebaseUserDataManager firebaseUserDataManager = new FirebaseUserDataManager();
                    firebaseUserDataManager.registerToken(getApplicationContext(), token);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
