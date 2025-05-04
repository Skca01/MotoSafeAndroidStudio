package com.example.motosafe5;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MotoSafePrefs";
    private static final String SYSTEM_PHONE_NUMBER_KEY = "systemPhoneNumber";
    private static final String OWNER_PHONE_NUMBER_KEY = "ownerPhoneNumber";
    private static final String ALERTS_STATE_KEY = "alertsState";
    private static final String SLEEP_MODE_STATE_KEY = "sleepModeState";

    private EditText systemNumberInput;
    private EditText ownerNumberInput;
    private TextView settingsResponseText;
    private ToggleButton alertToggleButton;
    private Button sleepModeButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        systemNumberInput = findViewById(R.id.systemNumberInput);
        ownerNumberInput = findViewById(R.id.ownerNumberInput);
        settingsResponseText = findViewById(R.id.settingsResponseText);

        Button saveSystemNumberButton = findViewById(R.id.saveSystemNumberButton);
        Button saveOwnerNumberButton = findViewById(R.id.saveOwnerNumberButton);

        alertToggleButton = findViewById(R.id.alertToggleButton);
        sleepModeButton = findViewById(R.id.sleepModeButton);

        // Restore saved phone numbers
        String savedSystemNumber = sharedPreferences.getString(SYSTEM_PHONE_NUMBER_KEY, "");
        String savedOwnerNumber = sharedPreferences.getString(OWNER_PHONE_NUMBER_KEY, "");
        systemNumberInput.setText(savedSystemNumber);
        ownerNumberInput.setText(savedOwnerNumber);

        // Restore alerts state
        boolean alertsEnabled = sharedPreferences.getBoolean(ALERTS_STATE_KEY, true);
        alertToggleButton.setChecked(alertsEnabled);
        updateAlertButtonState(alertsEnabled);

        // Check sleep mode state
        boolean isSleepMode = sharedPreferences.getBoolean(SLEEP_MODE_STATE_KEY, false);
        updateSleepModeButton(isSleepMode);

        // System Number Save Button Listener
        saveSystemNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String systemNumber = systemNumberInput.getText().toString().trim();

                // Validate system number
                if (validatePhoneNumber(systemNumber)) {
                    // Save system phone number to SharedPreferences WITHOUT sending SMS
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(SYSTEM_PHONE_NUMBER_KEY, systemNumber);
                    editor.apply();

                    updateCommandResponse("System contact number saved");
                }
            }
        });

        // Owner Number Save Button Listener
        saveOwnerNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String systemNumber = systemNumberInput.getText().toString().trim();
                String ownerNumber = ownerNumberInput.getText().toString().trim();

                // Validate both phone numbers before sending
                if (validatePhoneNumbers(systemNumber, ownerNumber)) {
                    // Save owner phone number to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(OWNER_PHONE_NUMBER_KEY, ownerNumber);
                    editor.apply();

                    // Send SMS to system number to set owner number
                    sendCommand(systemNumber, "setnumber " + ownerNumber);
                }
            }
        });

        // Alerts toggle button listener
        alertToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAlertsOn = alertToggleButton.isChecked();

                // Save alerts state
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(ALERTS_STATE_KEY, isAlertsOn);
                editor.apply();

                // Update button state
                updateAlertButtonState(isAlertsOn);

                // Send alerts command to SYSTEM number
                sendCommand(systemNumberInput.getText().toString().trim(),
                        isAlertsOn ? "alerts on" : "alerts off");
            }
        });

        // Sleep mode button listener
        sleepModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean currentSleepState = sharedPreferences.getBoolean(SLEEP_MODE_STATE_KEY, false);
                boolean newSleepState = !currentSleepState;

                // Save sleep mode state
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(SLEEP_MODE_STATE_KEY, newSleepState);
                editor.apply();

                // Send appropriate command to SYSTEM number
                String command = newSleepState ? "sleep" : "wake";
                sendCommand(systemNumberInput.getText().toString().trim(), command);

                // Update button appearance
                updateSleepModeButton(newSleepState);
            }
        });
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Basic phone number validation
        if (!phoneNumber.matches("\\+?[0-9]{10,14}")) {
            Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validatePhoneNumbers(String systemNumber, String ownerNumber) {
        // Check if system number is set
        if (systemNumber.isEmpty()) {
            Toast.makeText(this, "Please enter system phone number first", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if numbers are empty
        if (ownerNumber.isEmpty()) {
            Toast.makeText(this, "Please enter owner phone number", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if numbers are different
        if (systemNumber.equals(ownerNumber)) {
            Toast.makeText(this, "System and Owner phone numbers must be different", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Basic phone number validation
        if (!systemNumber.matches("\\+?[0-9]{10,14}") ||
                !ownerNumber.matches("\\+?[0-9]{10,14}")) {
            Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateAlertButtonState(boolean isOn) {
        if (isOn) {
            alertToggleButton.setBackgroundResource(R.drawable.rounded_button_background);
            alertToggleButton.setText("Alerts ON");
        } else {
            alertToggleButton.setBackgroundColor(Color.RED);
            alertToggleButton.setText("Alerts OFF");
        }
    }

    private void updateSleepModeButton(boolean isSleepMode) {
        if (isSleepMode) {
            sleepModeButton.setText("Wake");
            sleepModeButton.setBackgroundColor(Color.GREEN);
        } else {
            sleepModeButton.setText("Sleep");
            sleepModeButton.setBackgroundResource(R.drawable.rounded_button_background);
        }
    }

    private void sendCommand(String phoneNumber, String command) {
        if (validatePhoneNumber(phoneNumber)) {
            try {
                SmsManager smsManager = SmsManager.getDefault();

                // Send command SMS
                smsManager.sendTextMessage(phoneNumber, null, command, null, null);

                updateCommandResponse("Command sent: " + command + " to " + phoneNumber);
            } catch (Exception e) {
                updateCommandResponse("Failed to send " + command + " command");
            }
        }
    }

    private void updateCommandResponse(String message) {
        settingsResponseText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}