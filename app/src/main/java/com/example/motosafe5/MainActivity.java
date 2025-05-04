package com.example.motosafe5;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_CODE = 101;
    private static final String PREFS_NAME = "MotoSafePrefs";
    private static final String TRACKING_STATE_KEY = "trackingState";

    private MapView map;
    private EditText coordInput;
    private TextView locationDetails;
    private TextView errorMessage;
    private TextView trackingStatus;
    private Marker currentMarker;
    private Polyline trackingPath;
    private List<GeoPoint> trackPoints;
    private boolean isTrackingActive = false;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize osmdroid configuration
        Configuration.getInstance().load(getApplicationContext(),
                getPreferences(Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_main);

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isTrackingActive = sharedPreferences.getBoolean(TRACKING_STATE_KEY, false);

        // Request SMS permissions
        requestSMSPermission();

        // Initialize views
        map = findViewById(R.id.map);
        coordInput = findViewById(R.id.coordInput);
        locationDetails = findViewById(R.id.locationDetails);
        errorMessage = findViewById(R.id.errorMessage);
        trackingStatus = findViewById(R.id.trackingStatus);
        Button showButton = findViewById(R.id.showButton);
        Button commandButton = findViewById(R.id.commandButton);
        Button clearTrackButton = findViewById(R.id.clearTrackButton);

        // Add settings button
        ImageButton settingsButton = findViewById(R.id.settingsButton);

        // Initialize tracking path and points list
        trackingPath = new Polyline();
        trackingPath.setColor(Color.BLUE);
        trackingPath.setWidth(5f);
        trackPoints = new ArrayList<>();

        // Add path overlay to map
        map.getOverlays().add(trackingPath);

        // Update tracking status display
        updateTrackingStatusDisplay();

        // Configure map
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        final IMapController mapController = map.getController();
        mapController.setZoom(2.0);
        mapController.setCenter(new GeoPoint(0, 0));

        // Button click listeners
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocation();
            }
        });

        commandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commandIntent = new Intent(MainActivity.this, CommandActivity.class);
                startActivity(commandIntent);
            }
        });

        clearTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearTrackingPath();
                Toast.makeText(MainActivity.this, "Tracking path cleared", Toast.LENGTH_SHORT).show();
            }
        });

        // Settings button click listener
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        // Enter key listener
        coordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    showLocation();
                    return true;
                }
                return false;
            }
        });

        // Check for coordinates from SMS
        handleIncomingSMSCoordinates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();

        // Check if tracking status changed while in CommandActivity
        boolean currentTrackingState = sharedPreferences.getBoolean(TRACKING_STATE_KEY, false);
        if (isTrackingActive != currentTrackingState) {
            isTrackingActive = currentTrackingState;
            updateTrackingStatusDisplay();

            // If tracking was turned off, save the path points but keep displaying them
            if (!isTrackingActive) {
                Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tracking active - waiting for coordinates", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    private void updateTrackingStatusDisplay() {
        if (isTrackingActive) {
            trackingStatus.setText("Tracking: Active");
            trackingStatus.setTextColor(Color.GREEN);
        } else {
            trackingStatus.setText("Tracking: Inactive");
            trackingStatus.setTextColor(Color.RED);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Set the new intent to process it later
        setIntent(intent);

        // Handle coordinates from the new intent
        String latitude = intent.getStringExtra("latitude");
        String longitude = intent.getStringExtra("longitude");

        if (latitude != null && longitude != null) {
            try {
                double lat = Double.parseDouble(latitude);
                double lon = Double.parseDouble(longitude);

                // Update the input field
                coordInput.setText(String.format("%s, %s", latitude, longitude));

                // Show the location
                showLocation();

                // If tracking is active, add point to tracking path
                if (isTrackingActive) {
                    addPointToTrackingPath(lat, lon);
                }

                Toast.makeText(this, "New location received via SMS",
                        Toast.LENGTH_LONG).show();
            } catch (NumberFormatException e) {
                showError("Invalid coordinates from SMS");
            }
        }
    }

    private void clearTrackingPath() {
        trackPoints.clear();
        trackingPath.setPoints(trackPoints);
        map.invalidate();
    }

    private void requestSMSPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECEIVE_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS},
                    SMS_PERMISSION_CODE);
        }
    }

    private void handleIncomingSMSCoordinates() {
        // Check if coordinates were passed via intent
        String latitude = getIntent().getStringExtra("latitude");
        String longitude = getIntent().getStringExtra("longitude");

        if (latitude != null && longitude != null) {
            try {
                // Parse coordinates
                double lat = Double.parseDouble(latitude);
                double lon = Double.parseDouble(longitude);

                // Set coordinates in input field
                coordInput.setText(String.format("%s, %s", latitude, longitude));

                // Show location on map
                showLocation();

                // If tracking is active, add point to tracking path
                // This part was not working correctly before
                if (isTrackingActive) {
                    // Make sure to add the point to tracking path
                    addPointToTrackingPath(lat, lon);
                    // Update tracking display
                    updateLocationDetails();
                }

                // Show toast to inform user
                Toast.makeText(this, "Location received via SMS",
                        Toast.LENGTH_LONG).show();
            } catch (NumberFormatException e) {
                showError("Invalid coordinates from SMS");
            }
        } else {
            // Set default coordinates
            coordInput.setText("10.331446252993214, 123.6669827644954");
            showLocation();
        }
    }

    private void addPointToTrackingPath(double latitude, double longitude) {
        GeoPoint point = new GeoPoint(latitude, longitude);

        // Add point to tracking list
        trackPoints.add(point);

        // Update polyline
        trackingPath.setPoints(trackPoints);

        // Redraw map
        map.invalidate();

        // Update status
        updateLocationDetails();
    }

    private void updateLocationDetails() {
        if (trackPoints.size() > 0) {
            GeoPoint lastPoint = trackPoints.get(trackPoints.size() - 1);
            String details = String.format("Location Details:\nLatitude: %.4f\nLongitude: %.4f\nPoints tracked: %d",
                    lastPoint.getLatitude(), lastPoint.getLongitude(), trackPoints.size());
            locationDetails.setText(details);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
    String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission Denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String[] extractCoordinates(String message) {
        // Remove any non-coordinate characters except numbers, periods, commas,
        // spaces, and negative signs
        message = message.replaceAll("[^0-9.,\\s-]", "");

        // Try multiple parsing strategies
        String[] strategies = {
                // Strategy 1: Split by comma or space
                "[,\\s]+",
                // Strategy 2: Space-separated coordinates
                "\\s+",
                // Strategy 3: Comma-separated coordinates
                ","
        };

        for (String strategy : strategies) {
            String[] parts = message.split(strategy);

            // Try different coordinate parsing approaches
            for (int i = 0; i < parts.length - 1; i++) {
                try {
                    // Try parsing current and next part as lat/lon
                    double lat = Double.parseDouble(parts[i].trim());
                    double lon = Double.parseDouble(parts[i + 1].trim());

                    // Validate coordinate ranges
                    if (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
                        return new String[]{
                                String.format("%.6f", lat),
                                String.format("%.6f", lon)
                        };
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return null;
    }

    private void showLocation() {
        // Reset previous error
        errorMessage.setText("");

        // Remove previous marker if exists
        if (currentMarker != null) {
            map.getOverlays().remove(currentMarker);
        }

        String coordInputText = coordInput.getText().toString().trim();

        // Try to extract coordinates from the input
        String[] extractedCoords = extractCoordinates(coordInputText);

        if (extractedCoords == null) {
            showError("No valid coordinates found. Use \"Latitude, Longitude\"");
            return;
        }

        double latitude, longitude;
        try {
            latitude = Double.parseDouble(extractedCoords[0]);
            longitude = Double.parseDouble(extractedCoords[1]);
        } catch (NumberFormatException e) {
            showError("Invalid coordinate values");
            return;
        }

        // Create and add marker
        GeoPoint point = new GeoPoint(latitude, longitude);
        currentMarker = new Marker(map);
        currentMarker.setPosition(point);
        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(currentMarker);

        // Center and zoom map
        final IMapController mapController = map.getController();
        mapController.setCenter(point);
        mapController.setZoom(18.0);

        // Update location details
        locationDetails.setText(String.format("Location Details:\nLatitude: %.4f\nLongitude: %.4f",
                latitude, longitude));

        // If tracking is active, add point to tracking path
        if (isTrackingActive) {
            addPointToTrackingPath(latitude, longitude);
        }
    }

    private void showError(String message) {
        errorMessage.setText(message);
    }
}

