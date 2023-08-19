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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.muathaj_salahj.boxingking.Activities.FollowsActivity;

public class FollowsService extends Service {

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
                String action = intent.getStringExtra("action");
                String email = intent.getStringExtra("email");

                // call the suitable function according to the given action
                switch (action) {

                    case FollowsActivity.FOLLOW_ACTION:
                        addEmailAfterExistenceCheck(email);
                        break;

                    case FollowsActivity.UNFOLLOW_ACTION:
                        removeEmailFromUserFollowsList(email);
                        break;

                    default:
                        stopSelf();
                        break;
                }
            }
        }).start();

        return START_STICKY;
    }

    private void addEmailAfterExistenceCheck(String email) {

        // checking if there is a user account with the entered email in the users collection in firestore
        db.collection("users").document(email).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        String result = null;
                        Intent intent = new Intent(FollowsActivity.FOLLOW_ACTION);

                        if (task.isSuccessful()) {

                            DocumentSnapshot toFollowUserSnapshot = task.getResult();

                            // if the user exists then add its email to the current user follows list in firestore
                            if (toFollowUserSnapshot.exists()) {

                                addEmailToUserFollowsList(toFollowUserSnapshot.getData().get("email").toString());
                            } else {

                                // if the user exists return the error message for the follows activity
                                result = "This User Doesn't Exist";
                                intent.putExtra("result", result);
                                LocalBroadcastManager.getInstance(FollowsService.this).sendBroadcast(intent);
                                stopSelf();
                            }

                        } else {

                            // if the task failed return the error message for the follows activity
                            result = task.getException().getMessage();
                            intent.putExtra("result", result);
                            LocalBroadcastManager.getInstance(FollowsService.this).sendBroadcast(intent);
                            stopSelf();
                        }
                    }
                });
    }

    private void addEmailToUserFollowsList(String email) {

        // adding the user email that the current user want to follow to the current user follows emails list
        db.collection("users").document(currentUser.getEmail()).update("follows", FieldValue.arrayUnion(email)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                String result = null;

                // if the adding process failed return the error message
                if (!task.isSuccessful()) {

                    result = task.getException().getMessage();
                }

                // sending the task result for the follows activity null in success error message in failure
                Intent intent = new Intent(FollowsActivity.FOLLOW_ACTION);
                intent.putExtra("result", result);
                LocalBroadcastManager.getInstance(FollowsService.this).sendBroadcast(intent);
                stopSelf();
            }
        });

    }

    private void removeEmailFromUserFollowsList(String toUnfollowEmail) {

        // removing the user email from the current user follows emails list in firebase
        db.collection("users").document(currentUser.getEmail())
                .update("follows", FieldValue.arrayRemove(toUnfollowEmail))
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        String result = null;

                        // if the adding process failed return the error message
                        if (!task.isSuccessful()) {

                            result = task.getException().getMessage();
                        }

                        // sending the task result for the follows activity null in success error message in failure
                        Intent intent = new Intent(FollowsActivity.UNFOLLOW_ACTION);
                        intent.putExtra("result", result);
                        LocalBroadcastManager.getInstance(FollowsService.this).sendBroadcast(intent);
                        stopSelf();
                    }
                });

    }
}