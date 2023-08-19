package com.muathaj_salahj.boxingking.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.muathaj_salahj.boxingking.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // This line causes a problem, it duplicxates teh next activity
        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Hiding the action bar
        if(getSupportActionBar() != null)
            this.getSupportActionBar().hide();

        // Filling the splash screen with the students information and the submittion date
        fillSplashTextInfo();

        // for testing purpouse
        // FirebaseAuth.getInstance().signOut();

        // moving to the next activity. if the user logged in in go to the main activity else go to login activity
        intentAfterLogInCheck();
    }

    // THis function checks if the user logged in if true it moves to the main activity else it moves to the log in activity
    // it uses thread for that check and the thread waits for one second before he moves to the next activity
    private void intentAfterLogInCheck() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                // Initializing the app settings if it the first time of running the app
                initSettingsIfFirstTime();

                Intent intent;

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                // Check if the user is already signed in
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {

                    // Start the main activity if the user is signed in
                    intent = new Intent(SplashActivity.this, GameActivity.class);
                } else {

                    // Start the sign-in activity if the user is not signed in
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }

                try {
                    Thread.sleep(1000); // wait for 1 sec
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                startActivity(intent);

                // Finish the splash screen activity
                finish();
            }
        }).start();
    }

    // filling the students information and the submettion date  in the splashScreen
    private void fillSplashTextInfo(){

        final String studentsInfoStr = "Muath Abu Jamal\n\n\nSalah Jawabreh\n";
        final String submissionDateStr = "18.6.2023";

        TextView studentsInfoTV = findViewById(R.id.studentsInfoTVId);
        TextView submissionDateTV = findViewById(R.id.submissionDateTVId);

        studentsInfoTV.setText(studentsInfoStr);
        submissionDateTV.setText(submissionDateStr);
    }

    // THis function initializes the settings of the application if its the first time
    // that the app runs or if the settings deleted for any reason
    private void initSettingsIfFirstTime(){

        // getting the default shared preferences to save the settings values
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // initializing the settings by the default values if they don't exist
        // we check all the three settings for more confidence
        if(!sharedPref.contains(getString(R.string.ringing)) || !sharedPref.contains(getString(R.string.notifications)) || !sharedPref.contains(getString(R.string.hours_number))){

            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putBoolean(getString(R.string.ringing), getResources().getBoolean(R.bool.ringing));
            editor.putBoolean(getString(R.string.notifications), getResources().getBoolean(R.bool.notifications));
            editor.putInt(getString(R.string.hours_number), getResources().getInteger(R.integer.hours_number));
            editor.apply();
        }
    }
}