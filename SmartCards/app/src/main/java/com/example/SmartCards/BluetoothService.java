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
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
            manage_conn_thread = new ConnectionManager(mmSocket, parent_activity, DECKLIST_FILE_NAME);
            manage_conn_thread.start();
//            Thread mng_thread = new Thread(manage_conn_thread);
//            mng_thread.start();

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

    private class ConnectionManager extends Thread {
        // Message Types
        private final static int MSG_QUERY = 1;
        private final static int MSG_RECV_FILE = 2;
        private final static int MSG_ERROR = 3;

        // Query Codes
        private final static int QUERY_JSON = 1;
        private final static int QUERY_IMAGE = 2;
        private final static int QUERY_OVERRIDE = 3;
        private final static int QUERY_LOCK = 4;
        private final static int QUERY_UNLOCK = 5;

        // Receive File Codes
        private final static int RECV_FILE_OK = 1;
        private final static int RECV_FILE_ERR = 2;
        private final static int RECV_FILE_BEGIN = 3;
        private final static int RECV_FILE_END = 4;

        // Error Codes
        private final static int ERROR_BUSY = 2;
        private final static int ERROR_RECEIVE = 3;
        private final static int ERROR_MISMATCH = 4;
        private final static int ERROR_UNKNOWN = 5;

        // Indexes of data locations in correctly formatted messages
//        private final static int INDEX_PACKET_SIZE = 0;
        private final static int INDEX_TYPE = 0;
        private final static int INDEX_CODE = INDEX_TYPE + 4;
        private final static int INDEX_FILE_SIZE = INDEX_CODE + 4;
        private final static int INDEX_NAME_SIZE = INDEX_FILE_SIZE + 4;
        private final static int INDEX_NAME = INDEX_NAME_SIZE + 4;

        private final static int STATE_QUERY = 1;
        private final static int STATE_RECEIVE = 2;
        private int state = STATE_QUERY;

        // Bluetooth packet size
        private final int BUFFER_SIZE = 50000;
        private final int INT_SIZE = 4;

        public static final String TAG = "BLUETOOTH_MANAGER";

        // Deck Revision numbers
        private int cur_rev_num = 0;

        // File Transfer members
        private ByteBuffer cur_packet = null;
        private LinkedBlockingQueue<byte[]> file_queue = null;
        private ByteBuffer send_file_buffer = null;
        private ByteBuffer recv_file_buffer = null;
        private String recv_file_name;
        private Queue<String> send_file_queue;

        private final String DECKLIST_FILE_NAME;

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private ByteBuffer mmBuffer; // mmBuffer store for the stream
        private Activity parent_activity;

        public ConnectionManager(BluetoothSocket socket, Activity parent_activity, String deck_file_name) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mmBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            this.parent_activity = parent_activity;
            send_file_queue = new LinkedList<String>();
            file_queue = new LinkedBlockingQueue<>();
            DECKLIST_FILE_NAME = deck_file_name;
        }

        public void run() {
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer.array());
                    byte msgCopy[] = Arrays.copyOfRange(mmBuffer.array(), 0, numBytes);
                    Arrays.fill(mmBuffer.array(), 0, numBytes, (byte)0);
                    ByteBuffer data = ByteBuffer.wrap(msgCopy);

                    while (data.hasRemaining()) {
                        processPackets(data);
                    }
                    // Send the obtained bytes to the UI activity.
//                    Message readMsg = handler.obtainMessage(
//                            MessageConstants.MESSAGE_READ, numBytes, -1,
//                            msgCopy);
//                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

//                // Share the sent message with the UI activity.
//                Message writtenMsg = handler.obtainMessage(
//                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
//                String msgTxt = new String(mmBuffer.array());
//                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
//            Message writeErrorMsg =
//                    handler.obtainMessage(BluetoothService.MessageConstants.MESSAGE_TOAST);
//            Bundle bundle = new Bundle();
//            bundle.putString("toast",
//                    "Couldn't send data to the other device");
//            writeErrorMsg.setData(bundle);
//            handler.sendMessage(writeErrorMsg);
            }
        }

        private void send(byte data[]) {
            int packet_size = data.length;
            ByteBuffer send_bytes = ByteBuffer.allocate(packet_size + INT_SIZE);
            send_bytes.putInt(packet_size);
            send_bytes.put(data);
            write(send_bytes.array());
        }

        private void processPackets(ByteBuffer data) {
            // If there is no packet currently being assembled, prepare the next one
            if (cur_packet == null) {
                int packet_size = data.getInt();
                cur_packet = ByteBuffer.allocate(packet_size);
            }

            // Find out how many bytes of the data buffer to put in it
            if (cur_packet.hasRemaining()) {
                int cur_remaining = cur_packet.remaining();
                if (data.remaining() < cur_remaining) {
                    cur_packet.put(data);
                } else {
//                    byte temp[] = new byte[cur_remaining];
                    data.get(cur_packet.array(),cur_packet.arrayOffset() + cur_packet.position(),cur_remaining);
//                    data.get(temp);
//                    cur_packet.put(temp);
                    // Parse individual packet
                    parsePacket(cur_packet.array());
                    cur_packet = null;
                }
            }
        }

        private void sendInt(int msgInt) {
            ByteBuffer send_bytes = ByteBuffer.allocate(INT_SIZE);
            send_bytes.putInt(msgInt);
            write(send_bytes.array());
        }

        private void parsePacket(byte msg[]) {
            if (state == STATE_QUERY) {
                parseResponse(msg);
            } else if (state == STATE_RECEIVE) {
                file_queue.add(msg);
//                send(msg);
                state = STATE_QUERY;
            }
        }

        private void parseResponse(byte msg[]) {
            int msgType = getMsgInt(msg, INDEX_TYPE);
            int msgCode = getMsgInt(msg, INDEX_CODE);
            switch (msgType) {
                case MSG_RECV_FILE:
                    if (msgCode == RECV_FILE_BEGIN) {
                        state = STATE_RECEIVE;
                    } else if (msgCode == RECV_FILE_END) {
                        state = STATE_QUERY;
                    }
                    break;
                case MSG_QUERY:
                    // TODO: Respond to ui with query result
                    break;
                case MSG_ERROR:
                    // TODO: Respond to ui with error
                    break;
            }
        }

        public void receiveFile(String file_name) {
            // TODO: remove this line, just for testing
            byte file_data[] = file_queue.poll();
            FileOutputStream fos = null;
            try {
                File file = new File(parent_activity.getFilesDir(), file_name);
                if (!file.exists()) {
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
                fos.write(file_data);
                fos.flush();
            } catch (IOException ioe) {
                // TODO: Some proper error handling
                ioe.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException ioe) {
                    // TODO: some more proper error handling
                    System.out.println("Error in closing the stream");
                }
            }
            sendFile(file_name);
        }

        public void sendFile(String file_name) {
            FileInputStream fis = null;
            try {
                fis = parent_activity.openFileInput(file_name);
                byte file_buffer[] = new byte[fis.available()];
                fis.read(file_buffer);
                send(file_buffer);
            } catch (IOException ioe) {
                // TODO: Some proper error handling
                ioe.printStackTrace();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException ioe) {
                    // TODO: some more proper error handling
                    System.out.println("Error in closing the stream");
                }
            }
        }

        private int getMsgInt(byte[] msg, int index) {
            // Because of little endian
            return ByteBuffer.wrap(msg).getInt(index);
        }

        private String getMsgStr(byte[] msg, int start, int end) {
            byte msgCopy[] = Arrays.copyOfRange(msg, start, end);
            String parsed_string;
            try {
                parsed_string = new String(msgCopy, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                // TODO: Some proper error handling
                uee.printStackTrace();
                parsed_string = "";
            }
            return parsed_string;
        }

        private byte[] getPayload(byte[] msg, int start) {
            // Because of little endian
            byte msgCopy[] = Arrays.copyOfRange(msg, start, msg.length);
            return msgCopy;
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }


    public void connect(UUID uuid) {
        make_conn_thread = new ConnectThread(uuid);
        make_conn_thread.start();
    }

    public void sendFile(String file_name) {
        manage_conn_thread.sendFile(file_name);
    }

    public void receiveFile(String file_name) {
        manage_conn_thread.receiveFile(file_name);
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

