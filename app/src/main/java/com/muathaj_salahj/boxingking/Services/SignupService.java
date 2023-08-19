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
import com.google.firebase.firestore.FirebaseFirestore;
import com.muathaj_salahj.boxingking.Activities.SignupActivity;
import com.muathaj_salahj.boxingking.Objects.User;

public class SignupService extends Service {

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
                String name = intent.getStringExtra("name");
                String email = intent.getStringExtra("email");
                String password = intent.getStringExtra("password");

                // Signup a new user using the firebase APIs and Saving his information in a new document in the users collection
                signUpWithEmailAndPassword(name, email, password);
            }
        }).start();

        return START_STICKY;
    }


    private void signUpWithEmailAndPassword(String name, String email, String password) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // signup a new user by email and password using the firebase APIs
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                // if the signup finished sucecfully in the Authentication side of firebase
                // then create a new document in the firestore users collection for saveing the new user information
                if (task.isSuccessful()) {

                    createNewUserDocument(name, task.getResult().getUser().getEmail());

                } else {
                    // if the task failed return the error message to the signup activity
                    String result =  task.getException().getMessage();

                    Intent intent = new Intent(SignupActivity.SIGNUP_ACTION);
                    intent.putExtra("result", result);
                    LocalBroadcastManager.getInstance(SignupService.this).sendBroadcast(intent);
                    stopSelf();
                }
            }
        });
    }

    private void createNewUserDocument(String name, String email) {

        // creating new User object for adding it to the firestore
        User newUser = new User(name, email, 0.0f, null);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Adding new User document in the users collection in the firestore
        db.collection("users").document(email).set(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                String result = null;

                // if the process failed return the error message
                if(!task.isSuccessful()){

                    result = task.getException().getMessage();
                }

                // sending the task result for the signup activity null in success error message in failure
                Intent intent = new Intent(SignupActivity.SIGNUP_ACTION);
                intent.putExtra("result", result);
                LocalBroadcastManager.getInstance(SignupService.this).sendBroadcast(intent);
                stopSelf();
            }
        });
    }
}