package com.muathaj_salahj.boxingking.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.muathaj_salahj.boxingking.Activities.LoginActivity;

public class LoginService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // execute the task through new thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                // getting the needed data for the task from the intent extras
                String email = intent.getStringExtra("email");
                String password = intent.getStringExtra("password");

                loginWithEmailAndPassword(email, password);
            }
        }).start();

        return START_STICKY;
    }

    private void loginWithEmailAndPassword(String email, String password) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // login using the email and password through the firebase authentication APIs
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        String result = null;

                        // if the process failed return the error message
                        if (!task.isSuccessful()) {

                            result = task.getException().getMessage();
                        }

                        // sending the task result for the login activity null in success error message in failure
                        Intent intent = new Intent(LoginActivity.LOGIN_ACTION);
                        intent.putExtra("result", result);
                        LocalBroadcastManager.getInstance(LoginService.this).sendBroadcast(intent);

                        stopSelf();
                    }
                });
    }

}