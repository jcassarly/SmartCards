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
    private ConnectedThread manage_conn_thread = null;

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
            manage_conn_thread = new ConnectedThread(mmSocket);
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

    private class ConnectedThread extends Thread {
        // Heartbeat states
        public static final int WAIT_RESPONSE = 0;
        public static final int MERGING = 1;
        public static final int RECEIVING_FILE = 2;
        public static final int RECEIVING_IMAGE = 3;
        private int state = WAIT_RESPONSE;

        // Message Structure
        private final int CMD_INDEX = 0;
        private final int REV_NUM_INDEX = 4;
        private final int DECK_DATA_INDEX = 8;

        // Message Commands
        private final int CMD_HEARTBEAT = 0;
        private final int CMD_LOCKED = 1;
        private final int CMD_UNLOCKED = 2;

        // if there's an error in the command
        private final int ERROR_IN_CMD = -1;

        private final int BUFFER_SIZE = 100000;

        // Deck Revision numbers
        private int cur_rev_num = 0;
        private int cur_pi_rev_num = 0;
        private int prev_rev_num = 0;

        private Queue<String> image_queue;

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private ByteBuffer mmBuffer; // mmBuffer store for the stream
        private byte staged_deck[];

        public ConnectedThread(BluetoothSocket socket) {
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
        }

        public void run() {
//            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer.array());
                    byte msgCopy[] = Arrays.copyOfRange(mmBuffer.array(), 0, numBytes);
                    Arrays.fill(mmBuffer.array(), 0, numBytes, (byte)0);
                    runStateMachine(msgCopy, numBytes);

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
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        public Queue<String> getImageList() {
//            File file = new File(DECKLIST_FILE_NAME);
            Queue<String> imageList = new LinkedList<String>();
            BufferedReader br = null;
            FileInputStream fis = null;

            try {
//                br = new BufferedReader(new FileReader(file));
//                String imageName;
//                while((imageName = br.readLine()) != null) {
//                    if (!imageName.contains("[") && !imageName.contains("]")) {
//                        imageList.add(imageName);
//                    }
//                }
//                fis = new FileInputStream(file);
                fis = parent_activity.openFileInput(DECKLIST_FILE_NAME);
                byte buffer[] = new byte[fis.available()];
                fis.read(buffer);
                String jstr = new String(buffer, "UTF-8");
                Pattern pattern = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL);
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
                        imageList.add(current.getString(arr_index));
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
            return imageList;
        }

        public void writeToFile(byte data[], String fileName) {
            File file = null;
            FileOutputStream fos = null;
            try {

                file = new File(parent_activity.getFilesDir(), fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
//                fos = parent_activity.openFileOutput(fileName, Context.MODE_PRIVATE);

                fos.write(data);
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

        }

        public void receiveDeck(byte data[]) {
            switch (state) {
                case RECEIVING_FILE:
                    writeToFile(data, DECKLIST_FILE_NAME);
                    image_queue = getImageList();
                    state = RECEIVING_IMAGE;
                    break;
                case RECEIVING_IMAGE:
                    if (image_queue.size() > 0) {
                        // TODO: Put the images in the image directory?
                        writeToFile(data, image_queue.remove());
                        // TODO: After this function runs, it closes the connection?
                    }
                    break;
                default:
                    // TODO: Some kind of error handling.
            }
        }

        public int runStateMachine(byte msg[], int numBytes) {
            boolean command_error = false;
            boolean send_response = true;
            int command = getMsgInt(msg, CMD_INDEX);
            switch (command) {
                case CMD_HEARTBEAT:
                    // Update the pi's revision number and send our current revision number
                    cur_pi_rev_num = getMsgInt(msg, REV_NUM_INDEX);
                    break;
                case CMD_LOCKED:
                    // TODO: Revert deck back to revision from pi, save change log
                    cur_rev_num = prev_rev_num;
                    break;
                case CMD_UNLOCKED:
//                    Message readMsg = handler.obtainMessage(
//                            MessageConstants.MESSAGE_READ, numBytes, -1,
//                            msg);
//                    readMsg.sendToTarget();
                    if (numBytes == 8) { // 8 bytes is when received two integers: command and rev_num
                        if (state == WAIT_RESPONSE) {
                            state = RECEIVING_FILE;
                        } else if (state == RECEIVING_IMAGE) {
                            state = MERGING;
                            send_response = false;
                        } else if (state == MERGING) {
                            sendImage();
                            send_response = false;
                        } else {
                            command_error = true;
                        }
                    } else {
                        byte data[] = Arrays.copyOfRange(msg, DECK_DATA_INDEX, numBytes);
                        receiveDeck(data);
                    }
                    break;
                default:
                    command_error = true;
                    break;
            }

            if (send_response && !command_error) {
                ByteBuffer send_bytes = ByteBuffer.allocate(Integer.BYTES * 2);
                send_bytes.putInt(command).putInt(cur_rev_num);
                write(send_bytes.array());
            }

            return command_error == false ? state : ERROR_IN_CMD;
        }



        private int getMsgInt(byte[] msg, int index) {
            // Because of little endian
            return ByteBuffer.wrap(msg).getInt(index);
        }

        public void stageMerge() {
            prev_rev_num = cur_rev_num;
            // TODO: Read new revision number from deck object, get deck lists
            image_queue = getImageList(); // temporary solution
//            File file = new File(DECKLIST_FILE_NAME);
            FileInputStream fis = null;

            try {
                fis = parent_activity.openFileInput(DECKLIST_FILE_NAME);
                staged_deck = new byte[(int) fis.available()];
                fis.read(staged_deck);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException ioe) {
                    System.out.println("Error in closing filestream reading from deck file.");
                }
            }
        }

        private void sendImage() {
            byte fileBuffer[];
            if (!image_queue.isEmpty()) {
                String image_name = image_queue.remove();
//                File file = new File(IMAGE_DIR + image_name);
                FileInputStream fis = null;

                try {
//                    fis = new FileInputStream(file);
                    fis = parent_activity.openFileInput(image_name);
                    fileBuffer = new byte[(int) fis.available()];
                    ByteBuffer sendBuffer = ByteBuffer.allocate(Integer.BYTES * 2 + fis.available());
                    sendBuffer.putInt(CMD_UNLOCKED).putInt(cur_rev_num);
                    fis.read(fileBuffer);
                    sendBuffer.put(fileBuffer);
                    write(sendBuffer.array());
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                } finally {
                    try {
                        if (fis != null) {
                            fis.close();
                        }
                    } catch (IOException ioe) {
                        System.out.print("Error in closing image file input stream.");
                    }
                }
            }
        }

        public void merge() {
            if (state == MERGING) {
                ByteBuffer send_bytes = ByteBuffer.allocate(Integer.BYTES * 2 + staged_deck.length);
                send_bytes.putInt(CMD_UNLOCKED).putInt(cur_rev_num);
                send_bytes.put(staged_deck);
                write(send_bytes.array());
            } else {
                System.out.println("Not ready to merge yet!");
            }
        }

        public int getServerState() {
            return state;
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

