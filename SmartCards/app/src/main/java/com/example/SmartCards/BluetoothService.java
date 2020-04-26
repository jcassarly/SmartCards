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
import android.util.Pair;

import androidx.core.content.res.TypedArrayUtils;

import java.io.BufferedInputStream;
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
import java.sql.Connection;
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

    public interface MSG_TYPES {
        int QUERY = 1;
        int RECV_FILE = 2;
        int ERROR = 3;
        int ACK = 0xBEEFCAFE;
    }

    public interface QUERY_CODES {
        // Query Codes
        int JSON = 1;
        int IMAGE = 2;
        int OVERRIDE = 3;
        int LOCK = 4;
        int UNLOCK = 5;
        int IMAGE_TRANSFER = 6;
    }

    // Receiving file codes
    public interface RECV_FILE_CODES {
        int OK = 1;
        int ERR = 2;
        int BEGIN = 3;
        int END = 4;
    }

    public interface ERROR_CODES {
        int BUSY = 2;
        int RECEIVE = 3;
        int MISMATCH = 4;
        int UNKNOWN = 5;
    }

    public interface SEND_STATUS {
        int SUCCESS = 0;
        int ERROR = -1;
    }


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
        private final int RECEIVE_BUFFER_SIZE = 8192;
        private final int TRANSMIT_BUFFER_SIZE = 8192;
        private final int INT_SIZE = 4;

        public static final String TAG = "BLUETOOTH_MANAGER";

        // File Transfer members
        private ByteBuffer cur_packet = null;
        private Queue<String> send_file_queue;

        private LinkedBlockingQueue<Pair<Integer, Integer>> query_responses;
        private LinkedBlockingQueue<byte[]> file_queue = null;

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
//            RECEIVE_BUFFER_SIZE = mmSocket.getMaxReceivePacketSize();
//            TRANSMIT_BUFFER_SIZE = mmSocket.getMaxTransmitPacketSize();
            mmBuffer = ByteBuffer.allocate(RECEIVE_BUFFER_SIZE);
            this.parent_activity = parent_activity;
            send_file_queue = new LinkedList<String>();
            query_responses = new LinkedBlockingQueue<>();
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
            write(bytes, bytes.length);
        }

        public void write(byte[] bytes, int len) {
            try {
                mmOutStream.write(bytes, 0, len);

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
            int msgCode = -1;
            if (msgType != MSG_TYPES.ACK) {
                msgCode = getMsgInt(msg, INDEX_CODE);

                if (msgType == MSG_TYPES.RECV_FILE) {
                    if (msgCode == RECV_FILE_CODES.BEGIN) {
                        state = STATE_RECEIVE;
                    } else if (msgCode == RECV_FILE_CODES.END) {
                        state = STATE_QUERY;
                    }
                }
            }

            if (msgType != MSG_TYPES.RECV_FILE)
            {
                try {
                    query_responses.put(new Pair<Integer, Integer>(msgType, msgCode));
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }

        }

        public int receiveFile(String file_path) {
            FileOutputStream fos = null;
            try {
                byte file_data[] = file_queue.take();

                String debug = new String(file_data);
                // Check if we actually received an error message instead
                if (file_data.length <= 8) {
                    // Return error code
                    return RECV_FILE_CODES.ERR;
                }
                File file = new File(file_path);
                if (!file.exists()) {
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
                fos.write(file_data);
                fos.flush();
            } catch (IOException ioe) {
                // TODO: Some proper error handling
                ioe.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }  finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException ioe) {
                    // TODO: some more proper error handling
                    System.out.println("Error in closing the stream");
                }
            }
            return RECV_FILE_CODES.OK;
        }

        public void sendFile(String file_name) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(new File(file_name));
                BufferedInputStream bis = new BufferedInputStream(fis, TRANSMIT_BUFFER_SIZE);
                byte file_buffer[] = new byte[TRANSMIT_BUFFER_SIZE];
                int len;
                ByteBuffer size_buffer = ByteBuffer.allocate(Integer.BYTES);
                size_buffer.putInt(fis.available());
                write(size_buffer.array());
                while ((len = bis.read(file_buffer)) != -1) {
                    write(file_buffer, len);
                }
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

        public Pair<Integer, Integer> getQueryResponse() {
            Pair<Integer, Integer> response = new Pair<Integer, Integer>(0,0);
            try {
                response = query_responses.take();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            return response;
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

        public boolean socketConnected() {
            return mmSocket.isConnected();
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

    public int sendFile(String file_name) {
        if (manage_conn_thread != null) {
            manage_conn_thread.sendFile(file_name);
            return SEND_STATUS.SUCCESS;
        } else {
            return SEND_STATUS.ERROR;
        }
    }

    public int receiveFile(String file_path) {
        int return_code = SEND_STATUS.ERROR;
        if (manage_conn_thread != null) {
            if (manage_conn_thread.receiveFile(file_path) == RECV_FILE_CODES.OK) {
                return_code = SEND_STATUS.SUCCESS;
            }
        }
        return return_code;
    }

    public Pair<Integer, Integer> receiveResponse() {
        if (manage_conn_thread != null) {
            return manage_conn_thread.getQueryResponse();
        } else {
            return null;
        }
    }

    public int sendQuery(int code) {
        int return_code = SEND_STATUS.ERROR;
        if (manage_conn_thread != null) {
            ByteBuffer send_bytes = ByteBuffer.allocate(8);
            send_bytes.putInt(MSG_TYPES.QUERY);
            send_bytes.putInt(code);
            manage_conn_thread.send(send_bytes.array());
            return_code = SEND_STATUS.SUCCESS;
        }
        return return_code;
    }

    public int block() {
        int return_code = sendQuery(QUERY_CODES.LOCK);
        if (return_code == SEND_STATUS.SUCCESS) {
            if (receiveResponse().first == MSG_TYPES.ACK) {
                return_code = SEND_STATUS.SUCCESS;
            }
            else
            {
                return_code = SEND_STATUS.ERROR;
            }
        }
        return return_code;
    }

    public int unblock() {
        int return_code = sendQuery(QUERY_CODES.UNLOCK);
        if (return_code == SEND_STATUS.SUCCESS) {
            if (receiveResponse().first == MSG_TYPES.ACK) {
                return_code = SEND_STATUS.SUCCESS;
            }
            else
            {
                return_code = SEND_STATUS.ERROR;
            }
        }
        return return_code;
    }

    public int getDeckList() {
        int return_code = SEND_STATUS.ERROR;
        if (sendQuery(QUERY_CODES.JSON) == SEND_STATUS.SUCCESS) {
            return_code = receiveFile(LandingPageActivity.DECK_LIST);
        }
        return return_code;
    }

    public int transferImages() {
        int return_code = SEND_STATUS.ERROR;
        if (sendQuery(QUERY_CODES.IMAGE_TRANSFER) == SEND_STATUS.SUCCESS) {
            Pair<Integer, Integer> resp = receiveResponse();
            if (resp.first == MSG_TYPES.ACK) {
                sendFile(LandingPageActivity.FILE_TRANSFER_LIST);
                resp = receiveResponse();
                if (resp.first == MSG_TYPES.ACK) {
                    return_code = SEND_STATUS.SUCCESS;
                }
                else
                {
                    return_code = SEND_STATUS.ERROR;
                }
            }
            else
            {
                return_code = SEND_STATUS.ERROR;
            }
        }
        return return_code;
    }

    private Queue<String> readDecklist() {
        Queue<String> file_names = new LinkedList<String>();
        FileInputStream fis = null;
        try {
            fis = parent_activity.openFileInput(DECKLIST_FILE_NAME);
            byte buffer[] = new byte[fis.available()];
            fis.read(buffer);
            String jstr = new String(buffer, "UTF-8");
            Pattern pattern = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL); // in case there's some unwanted characters outside the brackets.
            Matcher matcher = pattern.matcher(jstr);
            matcher.find();
            jstr = matcher.group();
            JSONObject jobj = (JSONObject) new JSONTokener(jstr).nextValue();
            JSONArray deck = jobj.getJSONArray("deckList");
            JSONArray inPlay = jobj.getJSONArray("inPlayList");
            JSONArray discard = jobj.getJSONArray("discardList");

            JSONArray lists[] = {deck, inPlay, discard};
            for (int list_index = 0; list_index < lists.length; list_index++) {
                JSONArray current = lists[list_index];
                for (int arr_index = 0; arr_index < current.length(); arr_index++) {
                    String current_string = current.getString(arr_index);
                    if (!current_string.equals("null")) {
                        file_names.add(current_string);
                    }
                }
            }

        } catch (IOException ioe) {
            // TODO: Some proper error handling
            ioe.printStackTrace();
        } catch(JSONException je) {
            je.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error in closing the image name stream");
            }
        }
        return file_names;
    }


    public int override() {
        int return_code = sendQuery(QUERY_CODES.OVERRIDE);
        if (return_code == SEND_STATUS.SUCCESS){
            Pair<Integer, Integer> resp = receiveResponse();

            if (resp.first == MSG_TYPES.ACK) {
                sendFile(LandingPageActivity.DECK_LIST);
                resp = receiveResponse();

                if (resp.first == MSG_TYPES.ACK) {
                    return_code = SEND_STATUS.SUCCESS;
                }
                else
                {
                    return_code = SEND_STATUS.ERROR;
                }
            }
            else
            {
                return_code = SEND_STATUS.ERROR;
            }
        }
        return return_code;
    }

    public boolean isConnected() {
        return manage_conn_thread != null && manage_conn_thread.isAlive() && manage_conn_thread.socketConnected();
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

