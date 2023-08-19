package com.muathaj_salahj.boxingking.Activities;

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
import com.muathaj_salahj.boxingking.Services.LoginService;
import com.muathaj_salahj.boxingking.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    // declaring the needed variables
    private Button loginBtn;
    private Button signupBtn;
    private EditText emailET;
    private EditText passwordET;
    LocalBroadcastManager broadcastManager;
    IntentFilter intentFilter;
    public static final String LOGIN_ACTION = "com.muathaj_salahj.boxingking.LOGIN_ACTION";

    // declaring and initializing a broad cast receiver for reviving the service broadcasts
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(LOGIN_ACTION)) {
                String result = intent.getStringExtra("result");

                // if the service succeeded then navigate to the game activity
                if (result == null) {

                    Intent navigationIntent = new Intent(LoginActivity.this, GameActivity.class);
                    startActivity(navigationIntent);
                    finish();
                } else {

                    // if there is an exception toast it to the screen
                    Toast.makeText(LoginActivity.this, result, Toast.LENGTH_LONG).show();
                    setInputsEnability(true);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hiding the action bar
        if (getSupportActionBar() != null)
            this.getSupportActionBar().hide();

        // getting the login and the signup buttons and connecting listener to them
        loginBtn = findViewById(R.id.loginBtnId);
        signupBtn = findViewById(R.id.signupBtnId);
        loginBtn.setOnClickListener(this);
        signupBtn.setOnClickListener(this);

        // getting the email and password editTexts
        emailET = findViewById(R.id.emailETID);
        passwordET = findViewById(R.id.passwordETID);

        // initializing the intent filter for the LOGIN_ACTION
        intentFilter = new IntentFilter();
        intentFilter.addAction(LOGIN_ACTION);

        // register the broadcast receiver for the LocalBroadcast
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    // This functions handels the buttons clicks
    @Override
    public void onClick(View v) {

        // if the clicked button is the login button
        if (v.getId() == R.id.loginBtnId) {
            // disapling the inputs elements till the login routine finishes
            setInputsEnability(false);

            // getting the entered email and password
            String email = emailET.getText().toString().trim().toLowerCase();
            String password = passwordET.getText().toString().trim();

            // checking if the inputs are valid null if valid else error message and return
            String validityResult = InputValidator.isValidLoginInputs(email, password);
            if (validityResult != null) {
                Toast.makeText(this, validityResult, Toast.LENGTH_LONG).show();
                setInputsEnability(true);
                return;
            }

            // login with email and password using login service
            startLoginService(email, password);
        }

        // if the clicked button is the signup button move to the signup activity
        else if (v.getId() == R.id.signupBtnId) {

            // disapling the input elements till the user return to the login activity onResume() enables them again
            // so the user cannot click the signup button twice which runs the signup activity multiple times can be replaced with signupBtn.setEnabled(false)
            setInputsEnability(false);

            // moving to sign up activity
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // enabling the input elements again can be replaced with signupBtn.setEnabled(true) if you disabled just the signup button
        setInputsEnability(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregister the broadcast receiver form the LocalBroadcast
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    // This function starts the login service and it sends the email and password to it using intent extras
    private void startLoginService(String email, String password) {

        Intent serviceIntent = new Intent(this, LoginService.class);
        serviceIntent.putExtra("email", email);
        serviceIntent.putExtra("password", password);
        this.startService(serviceIntent);
    }


    // This function changes the inputs enability status for the inputs according
    // to the user usage (false during login process, true otherwise)
    private void setInputsEnability(boolean enabilityStatus) {

        emailET.setEnabled(enabilityStatus);
        passwordET.setEnabled(enabilityStatus);
        loginBtn.setEnabled(enabilityStatus);
        signupBtn.setEnabled(enabilityStatus);
    }

}