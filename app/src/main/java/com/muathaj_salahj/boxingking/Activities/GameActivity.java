package com.muathaj_salahj.boxingking.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.muathaj_salahj.boxingking.Services.GameService;
import com.muathaj_salahj.boxingking.NotificationUtils.NotificationScheduler;
import com.muathaj_salahj.boxingking.R;

public class GameActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {


    // interval mills for 3 seconds for the count down ad the round time
    private final long INTERVAL_MILLS_3_SEC = 3000;

    // final variables (activity names) for for the use of navigation method
    private final String settingsActivityName = "Settings", loginActivityName = "Login", followsActivityName = "Follows";

    // variables to hold an access to the view components
    private TextView highestScoreTv, lastRoundHighestScoreTv, gameStatusTv;
    private Button startBtn;

    // variables for the game control (rounds, timings)
    private long currentTime, elapsedTime, measurementStartTime;
    private boolean isMeasuring, newScore;

    // sensor variable and sensor manager to get access to the sensor and to read its changes
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    // Needed variable for reading the sensor changes and calculating the acceleration and saving it each rond in addition to saving the highest user score
    private float x, y, z, acceleration, lastRoundMaxAcceleration, userHighestScore;

    // declaring a broad cast receiver for reviving the service broadcasts and intent filter
    LocalBroadcastManager broadcastManager;
    IntentFilter intentFilter;

    // shared preferences for getting the settings variables
    private SharedPreferences sharedPref;
    private boolean byMenuNavigation;

    // Final actions string for the services calls and broadcasts receiving
    public static final String LOGOUT_ACTION = "com.muathaj_salahj.boxingking.LOGOUT_ACTION";
    public static final String GET_SCORE_ACTION = "com.muathaj_salahj.boxingking.GET_SCORE_ACTION";
    public static final String UPDATE_SCORE_ACTION = "com.muathaj_salahj.boxingking.UPDATE_SCORE_ACTION";

    // declaring and initializing a broad cast receiver for reviving the service broadcasts
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("result");

            if (result == null) {

                switch (intent.getAction()) {

                    case LOGOUT_ACTION:
                        navigateTo(loginActivityName);
                        break;

                    case GET_SCORE_ACTION:
                        userHighestScore = intent.getFloatExtra("score", 0.0f);
                        highestScoreTv.setText(String.format("%.2f", userHighestScore));
                        setInputsEnability(initAccelerationSensor());
                        break;

                    case UPDATE_SCORE_ACTION:
                        updateScoresView();
                        break;
                }

            } else {

                // if there is an exception toast it to the screen
                Toast.makeText(GameActivity.this, result, Toast.LENGTH_LONG).show();
                setInputsEnability(false);
            }
        }
    };


    /////////////////////////////////////////////////  Life Cycle Function /////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // setting the title of the activity in the action bar
        setTitle("Boxing King");

        // getting the view components of the activity
        highestScoreTv = findViewById(R.id.highestScoreTV);
        lastRoundHighestScoreTv = findViewById(R.id.lastRoundHighestScoreTV);
        gameStatusTv = findViewById(R.id.gameStatusTV);
        startBtn = findViewById(R.id.startBtn);

        // disapling all the inputs of the activity till we get the user score and check if the device has acceleration meter
        setInputsEnability(false);

        // connecting the button to the click listener
        startBtn.setOnClickListener(this);

        // initializing measuring variables
        isMeasuring = false;
        newScore = false;

        // initializing the intent filter for the Game Service Actions
        intentFilter = new IntentFilter();
        intentFilter.addAction(LOGOUT_ACTION);
        intentFilter.addAction(GET_SCORE_ACTION);
        intentFilter.addAction(UPDATE_SCORE_ACTION);

        // register the broadcast receiver for the LocalBroadcast
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);

        // getting the default shared preferences to init the settings values in the activity view
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        byMenuNavigation = false;

        // getting the user score using the game services
        startGameService(GET_SCORE_ACTION);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Cancel the repeating notification when the user returns to the application
        NotificationScheduler.cancelRepeatingNotification(this);

        byMenuNavigation = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        boolean notificationStatus = sharedPref.getBoolean(getString(R.string.notifications), getResources().getBoolean(R.bool.notifications));

        if(!byMenuNavigation && notificationStatus){

            // Start the repeating notification when the user exits the application
            NotificationScheduler.scheduleRepeatingNotification(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // unregister the sensor listener
        sensorManager.unregisterListener(this);
        // unregister the broadcast receiver form the LocalBroadcast
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }


    /////////////////////////////////////////////////  Game Starting Function /////////////////////////////////////////////////
    // This function starts the boxing game after counting down for 3 seconds
    public void startBoxingGame() {

        // The interval at which the countdown will update (1 second)
        long countdownInterval = 1000;

        // creating counting own timer
        new CountDownTimer(INTERVAL_MILLS_3_SEC, countdownInterval) {

            short secondsRemaining = 3;

            @Override
            public void onTick(long millisUntilFinished) {

                // Update the timer text with the remaining seconds
                gameStatusTv.setText(String.valueOf(secondsRemaining));
                secondsRemaining -= 1;
            }

            @Override
            public void onFinish() {

                // notifying the user that the game started
                gameStatusTv.setText("GO !");
                // calling the startNewMeasuring method to reset the measuring values for the new score
                // and to start the measuring using the acceleration sensor
                startNewMeasuring();
            }
        }.start();
    }

    /////////////////////////////////////////////////  Sensor Init, Observation And Management /////////////////////////////////////////////////

    // This function initiates the acceleration sensor
    // it checks if the sensor exists and if true register the sensor to the sensors listener
    // return true if the sensor available else false
    private boolean initAccelerationSensor() {

        // getting the accelerometerSensor using the system sensors manager
        sensorManager = getSystemService(SensorManager.class);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // checking if the device hasn't accelerometer sensor
        if (accelerometerSensor == null) {
            gameStatusTv.setText("No Acceleration Sensor");
            return false;
        }

        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        return true;
    }

    // This function handels the sensor changing values to
    // it calculates the acceleration for 3 seconds (round)
    // and it saves and updates the user score and the view with the round scores
    @Override
    public void onSensorChanged(SensorEvent event) {


        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            if (isMeasuring) {

                x = event.values[0]; // Acceleration force along the x axis
                y = event.values[1]; // Acceleration force along the y axis
                z = event.values[2]; // Acceleration force along the z axis

                // calculating the acceleration using the x, y, z values (victor norma)
                acceleration = (float) Math.sqrt(x * x + y * y + z * z);

                // subtracting the gravity acceleration
                acceleration -= SensorManager.GRAVITY_EARTH;
                // multiplying the acceleration with 10 for better user experience more motivation (score)
                acceleration = Math.abs(acceleration) * 10;

                // checking if the current acceleration is higher than the previous one if true
                // save the new one as the maximum round acceleration
                if (acceleration > lastRoundMaxAcceleration) {
                    lastRoundMaxAcceleration = acceleration;

                    // if the acceleration is bigger than the user score(acceleration) then save it and set newScore to true
                    // in order to update it when the round finishes in the firestore
                    if (lastRoundMaxAcceleration > userHighestScore) {

                        userHighestScore = lastRoundMaxAcceleration;
                        newScore = true;
                    }
                }

                // check if 3 seconds have passed (the round finished)
                // getting the current time then abstracting the game starting time from it
                currentTime = System.currentTimeMillis();
                elapsedTime = currentTime - measurementStartTime;

                // if three seconds passed stop the measuring
                if (elapsedTime >= INTERVAL_MILLS_3_SEC && isMeasuring) {
                    // stop the measuring
                    this.isMeasuring = false;
                    // notifying the user that the round finished
                    gameStatusTv.setText("Round Finished !");

                    // if the score is bigger than the user score then update the score in the firestore
                    // using the game service
                    if (newScore) {

                        if(sharedPref.getBoolean(getString(R.string.ringing), getResources().getBoolean(R.bool.ringing))){
                            playNewScoreSoundEffect();
                        }

                        startGameService(UPDATE_SCORE_ACTION);
                    }
                    // else just update the scores view with the new maximum score view (with animation)
                    else {

                        updateScoresView();
                    }
                }
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // not used (no need)
    }

    // reset the measuring variables and enable the measuring of the acceleration using the acceleration sensor
    private void startNewMeasuring() {

        measurementStartTime = System.currentTimeMillis();
        lastRoundMaxAcceleration = 0;
        newScore = false;
        isMeasuring = true;
    }

    /////////////////////////////////////////////////  Buttons OnClick Handler  /////////////////////////////////////////////////
    @Override
    public void onClick(View view) {

        // if the clicked button is the login button disable the inputs and start the boxing game
        if (view.getId() == R.id.startBtn) {
            setInputsEnability(false);
            startBoxingGame();
        }
    }

    /////////////////////////////////////////////////  Scores Animation  /////////////////////////////////////////////////

    // This function update the scores TextViews with the new scores after each round with animation
    private void updateScoresView() {

        final ValueAnimator animator = ValueAnimator.ofFloat(0, lastRoundMaxAcceleration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {

                lastRoundHighestScoreTv.setText(String.format("%.2f", animator.getAnimatedValue()));

                // if the score is higher than the user current score than update also the user highest score TextView
                if (newScore) {

                    highestScoreTv.setText(String.format("%.2f", animator.getAnimatedValue()));
                }
            }
        });

        // When the animation ends enable the inputs again
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                setInputsEnability(true);
            }
        });

        // the animation duration is 1.5 sec
        animator.setDuration(1500);
        animator.start();
    }

    /////////////////////////////////////////////////  Menu Options With Listeners  /////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflating the menu using the menu_main.xml
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle menu items selection
        switch (item.getItemId()) {

            case R.id.aboutMI:
                aboutAlertDialog();
                return true;

            case R.id.settingsMI:
                navigateTo(settingsActivityName);
                return true;

            case R.id.exitMI:
                exitAlertDialog();
                return true;

            case R.id.followsMI:
                navigateTo(followsActivityName);
                return true;

            case R.id.logoutMI:
                logoutAlertDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /////////////////////////////////////////////////  Menu Dialogs  /////////////////////////////////////////////////
    // This function displays about dialog as asked in the project requirements
    private void aboutAlertDialog() {
        String strDeviceOS = "Android OS " + Build.VERSION.RELEASE + " API " + Build.VERSION.SDK_INT;

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("About");
        dialog.setMessage("\nBoxing King ( 18.6.2023 )" + "\n\n" + strDeviceOS + "\n\n" + "By Muath Abu Jamal & Salah Jawabreh");

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // close this dialog
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // This function displays exit dialog with yes and no options
    private void exitAlertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Exit");
        dialog.setMessage("Are you sure ?");
        dialog.setCancelable(false);

        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // destroy this activity
                finish();
            }
        });

        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // close this dialog
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // This function displays logout dialog with yes and no options
    // if the user clicks yes it calls the suitable game service to log the user out
    private void logoutAlertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Logout");
        dialog.setMessage("Are you sure ?");
        dialog.setCancelable(false);

        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // disable the inputs and call the logout service
                setInputsEnability(false);
                startGameService(LOGOUT_ACTION);
            }
        });

        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // close this dialog
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /////////////////////////////////////////////////  Navigation And Inputs Management  /////////////////////////////////////////////////

    // This function used to navigate to other activities according to the menu selected item
    private void navigateTo(String activityName) {

        Intent intent;

        // setting byMenuNavigation to true to prevent running the notification repeater
        switch (activityName) {
            // navigate to the settings activity
            case settingsActivityName:
                intent = new Intent(GameActivity.this, SettingsActivity.class);
                byMenuNavigation = true;
                startActivity(intent);
                break;

            // navigate to the follows activity
            case followsActivityName:
                intent = new Intent(GameActivity.this, FollowsActivity.class);
                byMenuNavigation = true;
                startActivity(intent);
                break;

            // navigate to the login activity and finish the game activity
            case loginActivityName:
                intent = new Intent(GameActivity.this, LoginActivity.class);
                byMenuNavigation = true;
                startActivity(intent);
                finish();
                break;

        }

    }

    // enabling or disabling the inputs according to the processing status
    private void setInputsEnability(boolean enabilityStatus) {

        startBtn.setEnabled(enabilityStatus);
    }

    /////////////////////////////////////////////////  Game Services  /////////////////////////////////////////////////
    // This function starts a game service with the needed action and it sends the needed data according to the give action
    private void startGameService(String action) {

        Intent serviceIntent = new Intent(this, GameService.class);
        serviceIntent.putExtra("action", action);

        if (action.equals(UPDATE_SCORE_ACTION)) {
            serviceIntent.putExtra("score", userHighestScore);
        }

        this.startService(serviceIntent);
    }

    /////////////////////////////////////////////////  Sound Effects  /////////////////////////////////////////////////
    private void playNewScoreSoundEffect(){
        // running sound effect for new score
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.new_score_sound);
        mediaPlayer.start();
    }
}