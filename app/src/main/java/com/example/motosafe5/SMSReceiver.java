package com.example.motosafe5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String messageBody = smsMessage.getMessageBody();
                    String sender = smsMessage.getOriginatingAddress();

                    // Check if the message contains GPS coordinates
                    String[] coordinates = extractCoordinates(messageBody);

                    if (coordinates != null) {
                        // Create intent with coordinates
                        Intent mapIntent = new Intent(context, MainActivity.class);

                        // Important flags to handle different app states
                        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mapIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        // Pass the coordinates
                        mapIntent.putExtra("latitude", coordinates[0]);
                        mapIntent.putExtra("longitude", coordinates[1]);
                        mapIntent.putExtra("fromSMS", true); // Add indicator that this is from SMS

                        context.startActivity(mapIntent);

                        Toast.makeText(context,
                                "GPS Coordinates Received from " + sender,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    private String[] extractCoordinates(String message) {
        // Remove any non-coordinate characters
        message = message.replaceAll("[^0-9.,\\s-]", "");

        // Split by common separators
        String[] parts = message.split("[,\\s]+");

        // Look for two number-like strings that could be coordinates
        for (int i = 0; i < parts.length - 1; i++) {
            try {
                double lat = Double.parseDouble(parts[i]);
                double lon = Double.parseDouble(parts[i + 1]);

                // Basic validation of coordinates
                if (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
                    return new String[]{
                            String.format("%.6f", lat),
                            String.format("%.6f", lon)
                    };
                }
            } catch (NumberFormatException ignored) {}
        }

        return null;
    }
}

