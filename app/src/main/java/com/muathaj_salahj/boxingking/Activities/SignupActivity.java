package com.muathaj_salahj.boxingking.Activities;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.muathaj_salahj.boxingking.Validators.InputValidator;
import com.muathaj_salahj.boxingking.R;
import com.muathaj_salahj.boxingking.Services.SignupService;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    // declaring the needed variables
    private Button signupBtn;
    private EditText nameET;
    private EditText emailET;
    private EditText passwordET;
    private  boolean isProcessing;
    LocalBroadcastManager broadcastManager;
    IntentFilter intentFilter;
    public static final String SIGNUP_ACTION = "com.muathaj_salahj.boxingking.SIGNUP_ACTION";

    // declaring and initializing a broad cast receiver for reviving the service broadcasts
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SIGNUP_ACTION)) {
                String result = intent.getStringExtra("result");

                // if the service succeeded then navigate to the game activity
                if (result == null) {

                    Intent navigationIntent = new Intent(SignupActivity.this, GameActivity.class);
                    // these flags destroy all the previous activities in the stack
                    // and keep just the new created activity in the stack which is the Main Activity
                    navigationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(navigationIntent);
                    finish();
                } else {

                    // if there is an exception toast it to the screen
                    Toast.makeText(SignupActivity.this, result, Toast.LENGTH_LONG).show();
                    setInputsEnability(true);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Hiding the action bar
        if (getSupportActionBar() != null)
            this.getSupportActionBar().hide();

        // getting the signup button and connecting listener to it
        signupBtn = findViewById(R.id.signupBtnId);
        signupBtn.setOnClickListener(this);

        // getting the signup inputs editTexts
        nameET = findViewById(R.id.nameETID);
        emailET = findViewById(R.id.emailETID);
        passwordET = findViewById(R.id.passwordETID);

        // initializing the intent filter for the SIGNUP_ACTION
        intentFilter = new IntentFilter();
        intentFilter.addAction(SIGNUP_ACTION);

        // register the broadcast receiver for the LocalBroadcast
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);

        // this variable is for allowing or preventing the back operation
        // (back button) according to the status of the signup process
        isProcessing = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregister the broadcast receiver form the LocalBroadcast
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onBackPressed() {

        // if the sign up process didn't finish don't go back to the login page to prevent authentication errors
        if (isProcessing){
            Toast.makeText(SignupActivity.this, "Please Wait Till The Signup Finishes", Toast.LENGTH_LONG).show();
            return;
        }

        super.onBackPressed();
        // moving to the previous activity which is the login activity when the user clicks on the back button
        // without creating it again this is necessery when the user sets the application
        // in the stop state (background) and it returns to is later
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        intent.setFlags(FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {

        // handeling the click on the signup button
        if (v.getId() == R.id.signupBtnId) {

            // disapling the inputs elements till the signup routine finishes
            setInputsEnability(false);

            // getting the entered name, email and password
            String name = nameET.getText().toString().trim();
            String email = emailET.getText().toString().trim().toLowerCase();
            String password = passwordET.getText().toString().trim();

            // checking if the inputs are valid null if valid else error message and return
            String validityResult = InputValidator.isValidSignupInputs(name, email, password);
            if (validityResult != null) {
                Toast.makeText(this, validityResult, Toast.LENGTH_LONG).show();
                setInputsEnability(true);
                return;
            }

            // signup with email and password using signup service
            startSignupService(name, email, password);
        }
    }


    // This function changes the inputs enability status for the inputs according
    // to the user usage (false during signup process, true otherwise)
    private void setInputsEnability(boolean enabilityStatus) {

        nameET.setEnabled(enabilityStatus);
        emailET.setEnabled(enabilityStatus);
        passwordET.setEnabled(enabilityStatus);
        signupBtn.setEnabled(enabilityStatus);
        isProcessing = !enabilityStatus;
    }


    // This function starts the login service and it sends the email and password to it using intent extras
    private void startSignupService(String name, String email, String password) {

        Intent serviceIntent = new Intent(this, SignupService.class);
        serviceIntent.putExtra("name", name);
        serviceIntent.putExtra("email", email);
        serviceIntent.putExtra("password", password);
        this.startService(serviceIntent);
    }
}