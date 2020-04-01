package com.example.herroworld;

import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;



import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final String CONNECTION_ERROR_TAG = "CONNECTION_ERROR";
    public static UUID CONNECTION_UUID = UUID.fromString("b5c65192-1d67-471f-8147-0d0e8904efaa");
    public static final String MESSAGE = "com.example.herroworld.MESSAGE";
    public static final String DECKLIST_FILE_NAME = "decklist.json";
    public static final String IMAGE_DIR = "images";

    private Python py;

    private String BLUETOOTH_DEVICE_NAME = "LAPTOP-COVRD6IN";
    //    private String BLUETOOTH_DEVICE_NAME = "raspberrypi";
    private BluetoothService bluetooth_service = null;
    private ReceiveMsgHandler receive_msg_handler = null;


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

    private class ReceiveMsgHandler extends Handler {

        public ReceiveMsgHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO
            TextView msgView = (TextView) findViewById(R.id.android_rcv_msg);
            String msgTxt = new String((byte[])msg.obj);
            msgView.setText(msgTxt);
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

//        CONNECTION_UUID = UUID.randomUUID();

        // For bluetooth device discovery
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

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

//    public boolean bluetoothSetup() {
//        boolean success = false;
//
//        bluetooth_adapter = BluetoothAdapter.getDefaultAdapter();
//        if (bluetooth_adapter != null) {
//            // Ask to enable Bluetooth if not already
//            if (!bluetooth_adapter.isEnabled()) {
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            }
//
//            // Query already paired devices
//            Set<BluetoothDevice> pairedDevices = bluetooth_adapter.getBondedDevices();
//
//            if (pairedDevices.size() > 0) {
//                // There are paired devices. Get the name and address of each paired device.
//                for (BluetoothDevice device : pairedDevices) {
//                    if (device.getName().equals(BLUETOOTH_DEVICE_NAME)) {
//                        bluetooth_device = device;
//                        success = true;
//                        break;
//                    }
//                }
//            }
//            Handler bluetooth_handler = new Handler() {
//                public void handleMessage(Message msg) {
//                    // TODO
//                    TextView msgView = (TextView) findViewById(R.id.android_rcv_msg);
//                    String msgTxt = new String((byte[])msg.obj);
//                    msgView.setText(msgTxt);
//                }
//            };
//            bluetooth_service = new BluetoothService(bluetooth_handler);
//        }
//
//        return success && bluetooth_device != null;
//    }

    public void connectBluetooth(View view) {
        bluetooth_service.connect(CONNECTION_UUID);
    }

    public void debugBluetooth(View view) {
//        Set<BluetoothDevice> pairedDevices = bluetooth_adapter.getBondedDevices();
//        TextView txtVw = (TextView) findViewById(R.id.android_rcv_msg);
//        String message = "Could not find Laptop.";
//        if (pairedDevices.size() > 0) {
//            // There are paired devices. Get the name and address of each paired device.
//            for (BluetoothDevice device : pairedDevices) {
//                if (device.getName().equals(BLUETOOTH_DEVICE_NAME)) {
//                    message = device.getAddress();
//                }
//            }
//        }
//        message = message + "\n";
//        ParcelUuid uuids[] = bluetooth_device.getUuids();
//        for (ParcelUuid uuid : uuids) {
//            message = message + uuid.toString() + "\n";
//            System.out.println("UUID: " + uuid.toString());
//        }
//        txtVw.setText(message);
    }


    public void sendBluetooth(View view) {
        EditText blueMsg = (EditText) findViewById(R.id.bluemsg);
        String message = blueMsg.getText().toString();
        bluetooth_service.send(message);
    }

}
