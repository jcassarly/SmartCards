package com.example.herroworld;

import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;



import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final String CONNECTION_ERROR_TAG = "CONNECTION_ERROR";
    public static UUID CONNECTION_UUID;
    public static final String MESSAGE = "com.example.herroworld.MESSAGE";
    private Python py;
    private final static int REQUEST_ENABLE_BT = 1;
    private final BluetoothService bluetooth_service = null;
    private String BLUETOOTH_DEVICE_NAME = "LAPTOP-COVRD6IN";
    private BluetoothDevice bluetooth_device = null;
    private BluetoothAdapter bluetooth_adapter = null;
    private ConnectThread connection_thread = null;

    // For bluetooth device discovery
    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
//                TextView txtVw = (TextView) findViewById(R.id.bluetextview);
//                txtVw.setText(txtVw.getText() + "\n" + deviceHardwareAddress);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        py = Python.getInstance();

        CONNECTION_UUID = UUID.randomUUID();

        // For bluetooth device discovery
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        // Setup bluetooth, do something if it fails?
        boolean bluetoothSetupStatus = bluetoothSetup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    public void touchMe(View view) {
        Random randy = new Random();
        String message = "You Pen is: " + String.valueOf(randy.nextInt(100));
        TextView txtVw = (TextView) findViewById(R.id.penis);
        txtVw.setText(message);

        PyObject testModule = py.getModule("TestPython");
//        PyObject testObj = testModule.get("TestClass");
        String love = testModule.callAttr("getLove").toString();

        PyObject np = py.getModule("numpy.random");
        // PyObject npRand = np.callAttr("random");
//        String love = "Numpy result: " + np.callAttr("randint", 0, 10, 5).toString();
        TextView pyTV = (TextView) findViewById(R.id.pytextview);
        pyTV.setText(love);

    }

    public boolean bluetoothSetup() {
        boolean success = false;

        bluetooth_adapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth_adapter != null) {
            // Ask to enable Bluetooth if not already
            if (!bluetooth_adapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            // Query already paired devices
            Set<BluetoothDevice> pairedDevices = bluetooth_adapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals(BLUETOOTH_DEVICE_NAME)) {
                        bluetooth_device = device;
                        success = true;
                        break;
                    }
                }
            }
        }

        return success && bluetooth_device != null;
    }

    public void connectBluetooth(View view) {
        connection_thread = new ConnectThread(bluetooth_device);
        connection_thread.start();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(CONNECTION_UUID);
            } catch (IOException e) {
                Log.e(CONNECTION_ERROR_TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetooth_adapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(CONNECTION_ERROR_TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
//            manageMyConnectedSocket(mmSocket);
            bluetooth_service = new BluetoothService(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(CONNECTION_ERROR_TAG, "Could not close the client socket", e);
            }
        }
    }
}
