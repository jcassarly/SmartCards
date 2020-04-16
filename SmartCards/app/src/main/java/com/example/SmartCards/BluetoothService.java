package com.example.SmartCards;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.core.content.res.TypedArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class BluetoothService {
    public static final String TAG = "BLUETOOTH_SERVICE";
    public final static int REQUEST_ENABLE_BT = 1;
    public final static int NO_ERROR = 0;
    public final static int BLUETOOTH_ADAPTER_NULL = -1;
    public final static int BLUETOOTH_NOT_ENABLED = -2;
    private final String DECKLIST_FILE_NAME;
    private final String IMAGE_DIR;

    private Activity parent_activity = null;
    private Handler handler; // handler that gets info from Bluetooth service
    private BluetoothAdapter mmAdapter;
    private BluetoothDevice mmDevice = null;
    private ConnectThread make_conn_thread = null;
    private ConnectionManager manage_conn_thread = null;

    public BluetoothService(String device_address, String device_name, Handler msg_handler, Activity activity, String file_name, String image_dir) {
        parent_activity = activity;
        mmAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mmAdapter != null && checkBluetoothEnabled() == NO_ERROR) {

            // Query already paired devices
            Set<BluetoothDevice> pairedDevices = mmAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getAddress().equals(device_address) || device.getName().equals(device_name)) {
                        mmDevice = device;
                        break;
                    }
                }
            }
        }
        handler = msg_handler;

        // Creates a new file directory for images
        File directory = parent_activity.getFilesDir();
//        DECKLIST_FILE_NAME = directory.getAbsolutePath() + "/" + file_name;
        DECKLIST_FILE_NAME = file_name;
        File new_img_dir = new File(directory, image_dir);
        IMAGE_DIR = image_dir; //new_img_dir.getAbsolutePath();

    }

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    private class ConnectThread extends Thread {

        private BluetoothSocket mmSocket = null;

        public ConnectThread(UUID connection_uuid) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = mmDevice.createRfcommSocketToServiceRecord(connection_uuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;

        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mmAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
//            manageMyConnectedSocket(mmSocket);
            manage_conn_thread = new ConnectionManager(mmSocket, parent_activity);
            manage_conn_thread.start();

        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }


    public void connect(UUID uuid) {
        make_conn_thread = new ConnectThread(uuid);
        make_conn_thread.start();
    }

    /**
     * This function stages a new deck for sending to the pi
     */
    public void merge() {
////        manage_conn_thread.write(msg.getBytes());
//        int serverState = manage_conn_thread.getServerState();
//        if (serverState == ConnectedThread.WAIT_RESPONSE) {
////            Random randy = new Random();
////            byte new_deck[] = ByteBuffer.allocate(Integer.BYTES * 5).putInt(randy.nextInt()).putInt(randy.nextInt()).putInt(randy.nextInt()).putInt(randy.nextInt()).putInt(randy.nextInt()).array();
//            manage_conn_thread.stageMerge();
//        } else if(serverState == ConnectedThread.MERGING) {
//            manage_conn_thread.merge();
//        }
        manage_conn_thread.merge();
    }

    public void stageMerge() {
        manage_conn_thread.stageMerge();
    }


    public int getRevisionNumber(byte[] buffer) {
        return ByteBuffer.wrap(buffer).getInt(0);
    }

    public int checkBluetoothEnabled() {
        // TODO: Add a way to detect if bluetooth access denied
        int return_code = NO_ERROR;
        // Ask to enable Bluetooth if not already
        if (!mmAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            parent_activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return BLUETOOTH_NOT_ENABLED;
        }
        return return_code;
    }
}

