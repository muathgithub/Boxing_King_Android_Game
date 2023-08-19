package com.muathaj_salahj.boxingking.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.muathaj_salahj.boxingking.Activities.GameActivity;

public class GameService extends Service {

    // declaring the needed variables
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @Override
    public void onCreate() {
        super.onCreate();

        // initializing and getting the needed firebase instances for this activity functions
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    public GameService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // execute the task through new thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                // getting the needed data for the task from the intent extras
                String action = intent.getStringExtra("action");
                float score = intent.getFloatExtra("score", 0.0f);

                // call the suitable function according to the given action
                switch (action) {

                    case GameActivity.LOGOUT_ACTION:
                        logout();
                        break;

                    case GameActivity.GET_SCORE_ACTION:
                        getCurrentUserScore();
                        break;

                    case GameActivity.UPDATE_SCORE_ACTION:
                        updateCurrentUserScore(score);
                        break;

                    default:
                        stopSelf();
                        break;
                }
            }
        }).start();

        return START_STICKY;
    }

    // This function updates the new score of the user in the firestore
    private void updateCurrentUserScore(float newScore) {

        // update the user score in the user document in the users collection
        db.collection("users").document(currentUser.getEmail()).update("score", newScore).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                String result = null;
                Intent intent = new Intent(GameActivity.UPDATE_SCORE_ACTION);

                if (!task.isSuccessful()) {

                    // if the task failed return the error message for the follows activity
                    result = task.getException().getMessage();

                }

                intent.putExtra("result", result);
                LocalBroadcastManager.getInstance(GameService.this).sendBroadcast(intent);
                stopSelf();
            }
        });

    }

    // This function signs out the current user using the firebase auth object
    private void logout() {

        firebaseAuth.signOut();

        Intent intent = new Intent(GameActivity.LOGOUT_ACTION);
        intent.putExtra("result", (String) null);
        LocalBroadcastManager.getInstance(GameService.this).sendBroadcast(intent);
        stopSelf();
    }

    // This function gets the current user score for the game from the fire store users collection
    private void getCurrentUserScore() {

        db.collection("users").document(currentUser.getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                String result = null;
                Intent intent = new Intent(GameActivity.GET_SCORE_ACTION);

                // if the task succeeded then set the score in the intent extras
                if (task.isSuccessful()) {

                    float currUserHighestScore;

                    try{
                        currUserHighestScore = ((Double) task.getResult().getData().get("score")).floatValue();
                    } catch (ClassCastException e){
                        currUserHighestScore = ((Long) task.getResult().getData().get("score")).floatValue();
                    }

                    intent.putExtra("score", currUserHighestScore);

                } else {

                    // if the task failed return the error message for the follows activity
                    result = task.getException().getMessage();
                }

                intent.putExtra("result", result);
                LocalBroadcastManager.getInstance(GameService.this).sendBroadcast(intent);
                stopSelf();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}