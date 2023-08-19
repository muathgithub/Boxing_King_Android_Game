package com.muathaj_salahj.boxingking.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.muathaj_salahj.boxingking.NotificationUtils.NotificationScheduler;
import com.muathaj_salahj.boxingking.R;

public class SettingsActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    // declaring the needed variables
    int seekBarStep = 1;
    int seekBarMaxValue = 4;
    int seekBarMinValue = 1;
    private Switch ringSw;
    private Switch notificationsSw;
    private TextView hoursTV;
    private SeekBar hoursSB;
    private int currSeekBarValue;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private boolean byBackButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setting the title in the action bar
        setTitle("Settings");

        // getting the default shared preferences to init the settings values in the activity view
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();
        byBackButton = false;

        // getting the activity view components
        ringSw = findViewById(R.id.ringSw);
        notificationsSw = findViewById(R.id.notificationsSw);
        hoursTV = findViewById(R.id.hoursTV);
        hoursSB = findViewById(R.id.hoursSB);

        // setting the maximum of the notifications hours seek bar
        hoursSB.setMax((seekBarMaxValue - seekBarMinValue) / seekBarStep);

        // setting listeners for the view components
        hoursSB.setOnSeekBarChangeListener(this);
        ringSw.setOnCheckedChangeListener(this);
        notificationsSw.setOnCheckedChangeListener(this);

        initSettings();
    }

    // initializing the settings views with the saved settings values
    private void initSettings(){
        // getting the number of ours for the notifications repeating
        currSeekBarValue = sharedPref.getInt(getString(R.string.hours_number), getResources().getInteger(R.integer.hours_number));
        // getting the notification switch status on/off
        boolean notificationSwStatus = sharedPref.getBoolean(getString(R.string.notifications), getResources().getBoolean(R.bool.notifications));

        if(!notificationSwStatus){
            hoursSB.setEnabled(false);
        }

        // setting the settings view according to the saved values in the shared preferences
        ringSw.setChecked(sharedPref.getBoolean(getString(R.string.ringing), getResources().getBoolean(R.bool.ringing)));
        notificationsSw.setChecked(notificationSwStatus);
        hoursSB.setProgress((currSeekBarValue / seekBarStep) - 1);
        hoursTV.setText(String.format("Each %d %s", currSeekBarValue, currSeekBarValue == 1? "Hour" : "Hours"));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

        if(seekBar.getId() == hoursSB.getId()){

            currSeekBarValue = seekBarMinValue + (progress * seekBarStep);
            hoursTV.setText(String.format("Each %d %s", currSeekBarValue, currSeekBarValue == 1? "Hour" : "Hours"));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // saving the notification hours number after the user stops takes his hand off the seekbar
        editor.putInt(getString(R.string.hours_number), currSeekBarValue);
        editor.apply();
    }

    // This listener is for saving the values of the ringing and receiving notification settings
    // according to the switches status on / off
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checkValue) {

        if(compoundButton.getId() == ringSw.getId()){

            editor.putBoolean(getString(R.string.ringing), checkValue);
            editor.apply();
        }

        else if(compoundButton.getId() == notificationsSw.getId()){

            hoursSB.setEnabled(checkValue);
            editor.putBoolean(getString(R.string.notifications), checkValue);
            editor.apply();
        }
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
        byBackButton = true;
    }
}