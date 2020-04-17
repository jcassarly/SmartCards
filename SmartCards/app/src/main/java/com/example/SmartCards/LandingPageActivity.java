package com.example.SmartCards;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import java.io.FileInputStream;

public class LandingPageActivity extends AppCompatActivity {

    // Bluetooth Connection Members
    public static UUID CONNECTION_UUID = null;
    public static final String DECKLIST_FILE_NAME = "decklist.json";
    public static final String IMAGE_DIR = "images";
    public static String BLUETOOTH_DEVICE_NAME = "LAPTOP-COVRD6IN";
    public static ReceiveMsgHandler receive_msg_handler = null;
    public static BluetoothService bluetooth_service = null;

    private class ReceiveMsgHandler extends Handler {

        public ReceiveMsgHandler() {
            super(Looper.getMainLooper());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);

        // Set UUID for bluetooth connection.
        CONNECTION_UUID =  UUID.fromString(getString(R.string.UUID));

        // Setup bluetooth, do something if it fails?
        receive_msg_handler = new ReceiveMsgHandler();
        bluetooth_service = new BluetoothService(
                "",
                BLUETOOTH_DEVICE_NAME,
                receive_msg_handler,
                this,
                DECKLIST_FILE_NAME,
                IMAGE_DIR);
    }

    public void openInfo(View view) {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
    }

    public void createDeck(View view) {
        Intent intent = new Intent(this, EditDeck.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void connectBluetooth(View view) {
        bluetooth_service.connect(CONNECTION_UUID);
    }
}
