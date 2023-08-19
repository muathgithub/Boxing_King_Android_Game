package com.muathaj_salahj.boxingking.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.muathaj_salahj.boxingking.Adapters.FollowsListAdapter;
import com.muathaj_salahj.boxingking.Services.FollowsService;
import com.muathaj_salahj.boxingking.Validators.InputValidator;
import com.muathaj_salahj.boxingking.NotificationUtils.NotificationScheduler;
import com.muathaj_salahj.boxingking.R;
import com.muathaj_salahj.boxingking.Objects.User;

import java.util.ArrayList;


public class FollowsActivity extends AppCompatActivity implements View.OnClickListener {

    // declaring the needed variables
    private ArrayList<User> users;
    private FollowsListAdapter followsListAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private ListView followsListView;
    private Button followBtn;
    private EditText emailET;
    LocalBroadcastManager broadcastManager;
    IntentFilter intentFilter;
    private SharedPreferences sharedPref;
    private boolean byBackButton;
    public static final String FOLLOW_ACTION = "com.muathaj_salahj.boxingking.FOLLOW_ACTION";
    public static final String UNFOLLOW_ACTION = "com.muathaj_salahj.boxingking.UNFOLLOW_ACTION";


    // declaring and initializing a broad cast receiver for reviving the service broadcasts
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String actionString = intent.getAction();

            if (actionString.equals(FOLLOW_ACTION) || actionString.equals(UNFOLLOW_ACTION)) {
                String result = intent.getStringExtra("result");

                // if the service succeeded then update the listview (reload the followed users data)
                if (result == null) {

                    fillUserFollowsList();

                } else {

                    // if there is an exception toast it to the screen
                    Toast.makeText(FollowsActivity.this, result, Toast.LENGTH_LONG).show();
                    setInputsEnability(true);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follows);

        setTitle("Follows");

        // Hiding the action bar
//        if (getSupportActionBar() != null)
//            this.getSupportActionBar().hide();

        // initializing and getting the needed firebase instances for this activity functions
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        emailET = findViewById(R.id.emailETId);

        // getting the follow button and connecting listener to it
        followBtn = findViewById(R.id.followBtnId);
        followBtn.setOnClickListener(this);
        // getting the follows list for adding the users follows to it for displaying and managing them
        followsListView = findViewById(R.id.followsLVId);
        // setting and implementing OnItemLongClickListener for the listview
        initOnItemLongClickListener();


        // initializing the users ArrayList and its custom Adapter for displaying and managing the users
        users = new ArrayList<>();
        followsListAdapter = new FollowsListAdapter(this, users);
        followsListView.setAdapter(followsListAdapter);

        // initializing the intent filter for the FOLLOW_ACTION and UNFOLLOW_ACTION
        intentFilter = new IntentFilter();
        intentFilter.addAction(FOLLOW_ACTION);
        intentFilter.addAction(UNFOLLOW_ACTION);

        // register the broadcast receiver for the LocalBroadcast
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);

        // getting the default shared preferences to init the settings values in the activity view
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        byBackButton = false;

        // filling the List view with the users that the current user follow
        fillUserFollowsList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregister the broadcast receiver form the LocalBroadcast
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    private void fillUserFollowsList() {

        // getting the followed user info using new thread and filling it in the users array the notify the listview using the UI thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                // disapling the input elements till the fill process done
                setInputsEnability(false);

                // clearing the users list because this function also used as refresh function
                users.clear();
                // notifying the adapter for the data change (clear)
                notifyListViewByUIThread();

                // Getting the array of the users emails whom the current user follows from the current user document in the users collection
                db.collection("users").document(currentUser.getEmail())
                        .get().addOnCompleteListener(task -> {

                            // if the task succeeded then fetch the users full documents and fill them in the users arrayList so we can display them in the listView
                            if (task.isSuccessful()) {

                                ArrayList<String> currUserFollows = (ArrayList<String>) task.getResult().getData().get("follows");

                                // checking if the user follows other users
                                if (!currUserFollows.isEmpty()) {

                                    // getting the users documents and filling the in the users array with adapter notifying
                                    getAndFillFollowsDocuments(currUserFollows);
                                } else {

                                    setInputsEnability(true);
                                }

                            } else {

                                // If the task fails toast the exception to the screen and enabling the input element
                                Toast.makeText(FollowsActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                setInputsEnability(true);
                            }
                        });
            }
        }).start();
    }

    private void getAndFillFollowsDocuments(ArrayList<String> currUserFollows) {

        // fetching the users full documents and fill them in the users arrayList so we can display them in the listView
        db.collection("users").whereIn(FieldPath.documentId(), currUserFollows)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        // if the task succeded fill the users objects in the users array then notify the adapter
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot currDocumentSnapshot : task.getResult()) {

                                users.add(currDocumentSnapshot.toObject(User.class));
                            }

                            // notifying the adapter with the data set changes
                            notifyListViewByUIThread();

                        } else {

                            // If the task fails toast the exception to the screen
                            Toast.makeText(FollowsActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }

                        // enabling the input elements
                        setInputsEnability(true);
                    }
                });
    }


    @Override
    public void onClick(View view) {

        // handeling the click on the follow button
        if (view.getId() == R.id.followBtnId) {

            // disapling the input elements till the follow process done
            setInputsEnability(false);

            // getting the entered email
            String email = emailET.getText().toString().trim().toLowerCase();

            // checking if the inputs are valid null if valid else error message and return
            String validityResult = InputValidator.isValidFollowInputs(email);
            if (validityResult != null) {
                Toast.makeText(this, validityResult, Toast.LENGTH_LONG).show();
                setInputsEnability(true);
                return;
            }

            // checking if the user(email) is already followed
            if (isAlreadyFollowed(email)) {
                Toast.makeText(this, "This User Is Already Followed", Toast.LENGTH_LONG).show();
                setInputsEnability(true);
                return;
            }
            // checking if the user(email) is the current user email
            else if (email.equals(currentUser.getEmail())) {
                Toast.makeText(this, "You Can't Follow Yourself", Toast.LENGTH_LONG).show();
                setInputsEnability(true);
                return;
            }

            // follow by email using FollowsService
            startFollowService(email, FOLLOW_ACTION);
        }
    }

    // This function starts the follow service and it sends the email to it using intent extras
    private void startFollowService(String email, String action) {

        Intent serviceIntent = new Intent(this, FollowsService.class);
        serviceIntent.putExtra("email", email);
        serviceIntent.putExtra("action", action);
        this.startService(serviceIntent);
    }


    // This function checks if the current user already follows the entered email
    private boolean isAlreadyFollowed(String email) {

        for (User user : users) {

            if (user.getEmail().equals(email)) {
                return true;
            }
        }

        return false;
    }

    // This function sets an OnItemLongClickListener on the follows list so we can display an alert
    // for the user then according to his choice we delete the image or not
    private void initOnItemLongClickListener() {

        // setting and initializing the OnItemLongClickListener
        followsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int userIndex, long l) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                setInputsEnability(false);
                                startFollowService(users.get(userIndex).getEmail(), UNFOLLOW_ACTION);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(FollowsActivity.this);
                builder.setTitle("Unfollow").setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                return true;
            }
        });
    }


    // This function changes the inputs enability status for the inputs according
    // to the user usage (false during un/follow process, true otherwise)
    private void setInputsEnability(boolean enabilityStatus) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emailET.setEnabled(enabilityStatus);
                followBtn.setEnabled(enabilityStatus);
                followsListView.setEnabled(enabilityStatus);
            }
        });

    }

    // notifying the listview for the data set change using the UI thread (from the new thread)
    private void notifyListViewByUIThread() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                followsListAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Cancel the repeating notification when the user returns to the application
        NotificationScheduler.cancelRepeatingNotification(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        boolean notificationStatus = sharedPref.getBoolean(getString(R.string.notifications), getResources().getBoolean(R.bool.notifications));

        if(!byBackButton && notificationStatus){

            // Start the repeating notification when the user exits the application
            NotificationScheduler.scheduleRepeatingNotification(this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // setting byBackButton to true to prevent running the notification repeater
        byBackButton = true;
    }
}