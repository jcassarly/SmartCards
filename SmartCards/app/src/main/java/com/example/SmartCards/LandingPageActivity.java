package com.example.SmartCards;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import java.io.FileInputStream;

public class LandingPageActivity extends AppCompatActivity {

    // Bluetooth Connection Members
    public static UUID CONNECTION_UUID = null;
    public static final String DECKLIST_FILE_NAME = "decklist.json";
    public static String IMAGE_DIR = "images";
    public static String DECK_LIST_DIR;
    public static String DECK_LIST;
    public static String FILE_TRANSFER_LIST;
    public static String BLUETOOTH_DEVICE_NAME = "raspberrypi";
    public static ReceiveMsgHandler receive_msg_handler = null;
    public static BluetoothService bluetooth_service = null;

    private EditDeckManager deckManager;
    public static String DECK_MANAGER = "DeckManager";

    private class ReceiveMsgHandler extends Handler {

        public ReceiveMsgHandler() {
            super(Looper.getMainLooper());
        }
    };

    // TODO: instantiate the deck manager
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);

        // Set UUID for bluetooth connection.
        CONNECTION_UUID = UUID.fromString(getString(R.string.UUID));

        //File testFile = new File(dir, "ThisisaTest");
        File dir = this.getDir("deck", this.MODE_PRIVATE);
        File decklist = this.getDir("decklist", this.MODE_PRIVATE);
        IMAGE_DIR = dir.toString();
        DECK_LIST_DIR = decklist.toString();
        DECK_LIST = DECK_LIST_DIR + "/decklist.json";
        FILE_TRANSFER_LIST = DECK_LIST_DIR + "/file_transfer.json";


        // Setup bluetooth, do something if it fails?
        receive_msg_handler = new ReceiveMsgHandler();
        bluetooth_service = new BluetoothService(
                "",
                BLUETOOTH_DEVICE_NAME,
                receive_msg_handler,
                this,
                DECKLIST_FILE_NAME,
                IMAGE_DIR);

        deckManager = EditDeckManager.getInstance(this);

    }


    public void openInfo(View view) {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
    }

    public void createDeck(View view) {
        if (bluetooth_service.isConnected()) {
            Intent intent = new Intent(this, EditDeck.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            Toast.makeText(this, "Connect Bluetooth First", Toast.LENGTH_SHORT).show();
        }
    }

    public void playGame(View view) {
        if (bluetooth_service.isConnected()) {
            if (bluetooth_service.override() == BluetoothService.SEND_STATUS.SUCCESS) {
                Intent intent = new Intent(this, PlayActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                Toast.makeText(this, "Unable to override pi", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Connect Bluetooth First", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void debug(View view) {
        Intent intent = new Intent(this, Test.class);
        startActivity(intent);
    }

    public void connectBluetooth(View view) {
        bluetooth_service.connect(CONNECTION_UUID);
        //Toast.makeText(this, "Connection Starting", Toast.LENGTH_SHORT).show();
        while (!bluetooth_service.isConnected());
        Toast.makeText(this, "Connection Successful", Toast.LENGTH_SHORT).show();
    }
}
