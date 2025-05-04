package com.example.motosafe5;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CommandActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_REQUEST_CODE = 123;
    private static final String PREFS_NAME = "MotoSafePrefs";
    private static final String PHONE_NUMBER_KEY = "devicePhoneNumber";
    private static final String TRACKING_STATE_KEY = "trackingState";

    private EditText phoneNumberInput;
    private TextView commandResponseText;
    private SharedPreferences sharedPreferences;
    private ToggleButton trackToggleButton;
    private ToggleButton onOffToggleButton;
    private Button acknowledgeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        commandResponseText = findViewById(R.id.commandResponseText);
        Button savePhoneNumberButton = findViewById(R.id.savePhoneNumberButton);

        // Restore saved phone number
        String savedPhoneNumber = sharedPreferences.getString(PHONE_NUMBER_KEY, "");
        phoneNumberInput.setText(savedPhoneNumber);

        // Command buttons
        trackToggleButton = findViewById(R.id.trackToggleButton);
        Button statusButton = findViewById(R.id.statusButton);
        onOffToggleButton = findViewById(R.id.onOffToggleButton);
        acknowledgeButton = findViewById(R.id.acknowledgeButton);

        // Set the toggle state based on saved preference
        boolean isTracking = sharedPreferences.getBoolean(TRACKING_STATE_KEY, false);
        trackToggleButton.setChecked(isTracking);

        // Set the initial button color and text based on tracking state
        if (isTracking) {
            trackToggleButton.setBackgroundColor(Color.RED);
            trackToggleButton.setText("TRACK ON");
        } else {
            trackToggleButton.setBackgroundResource(R.drawable.rounded_button_background);
            trackToggleButton.setText("Track");
        }

        // Set the initial state for ON/OFF toggle - default to ON
        onOffToggleButton.setChecked(true);
        onOffToggleButton.setBackgroundResource(R.drawable.rounded_button_background);
        onOffToggleButton.setText("ON");

        // Save phone number button listener
        savePhoneNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberInput.getText().toString().trim();
                if (validatePhoneNumber(phoneNumber)) {
                    // Save phone number to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(PHONE_NUMBER_KEY, phoneNumber);
                    editor.apply();

                    Toast.makeText(CommandActivity.this, "Phone number saved", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set click listeners for command buttons
        trackToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isTracking = trackToggleButton.isChecked();

                // Update tracking state in preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(TRACKING_STATE_KEY, isTracking);
                editor.apply();

                // Set button color and text based on tracking state
                if (isTracking) {
                    trackToggleButton.setBackgroundColor(Color.RED);
                    trackToggleButton.setText("TRACK ON");
                    sendCommand("TRACK ON");
                    updateCommandResponse("Tracking started - Updates every 10 seconds");
                } else {
                    // Restore the original button background
                    trackToggleButton.setBackgroundResource(R.drawable.rounded_button_background);
                    trackToggleButton.setText("Track");
                    sendCommand("TRACK OFF");
                    updateCommandResponse("Tracking stopped");
                }
            }
        });

        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("STATUS");
            }
        });

        onOffToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isOn = onOffToggleButton.isChecked();

                // Update button color and text based on state
                if (isOn) {
                    onOffToggleButton.setBackgroundResource(R.drawable.rounded_button_background);
                    onOffToggleButton.setText("ON");
                    sendCommand("ON");
                    updateCommandResponse("Device turned ON");
                } else {
                    onOffToggleButton.setBackgroundColor(Color.RED);
                    onOffToggleButton.setText("OFF");
                    sendCommand("OFF");
                    updateCommandResponse("Device turned OFF");
                }
            }
        });

        acknowledgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("ACKNOWLEDGE");
                updateCommandResponse("Acknowledgment sent");
            }
        });

        // Request SMS permissions
        requestSMSPermission();
    }

    private void requestSMSPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        }
    }

    private void sendCommand(String command) {
        String phoneNumber = phoneNumberInput.getText().toString().trim();

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

    private boolean validatePhoneNumber(String phoneNumber) {
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter system phone number", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Basic phone number validation (adjust regex as needed)
        if (!phoneNumber.matches("\\+?[0-9]{10,14}")) {
            Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateCommandResponse(String message) {
        commandResponseText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}