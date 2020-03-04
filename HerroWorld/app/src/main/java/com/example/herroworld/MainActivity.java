package com.example.herroworld;

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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;



import org.w3c.dom.Text;

import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final String MESSAGE = "com.example.herroworld.MESSAGE";
    private Python py;
    private final static int REQUEST_ENABLE_BT = 1;

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
                TextView txtVw = (TextView) findViewById(R.id.bluetextview);
                txtVw.setText(txtVw.getText() + "\n" + deviceHardwareAddress);
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

        // For bluetooth
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

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

    public void bluetooth(View view) {
        TextView txtVw = (TextView) findViewById(R.id.bluetextview);

//        PyObject bluetoothModule = py.getModule("TestBluetooth");
//        String addresses = bluetoothModule.callAttr("discover").toString();
//        txtVw.setText(addresses);

        String message = "Bluetooth adapter error!";

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            // Ask to enable Bluetooth if not already
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            // Query already paired devices
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            message = "Paired Devices: ";
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
//                    String deviceName = device.getName();
//                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    message = message + device.getName() + ", " + device.getAddress();

                }
            }

            txtVw.setText(message);
        }
    }
}
