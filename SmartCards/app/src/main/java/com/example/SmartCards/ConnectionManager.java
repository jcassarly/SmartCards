package com.example.SmartCards;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionManager extends Thread {
    // States
    public static final int STATE_WAIT_RESP = 0;
    public static final int STATE_SEND_FILE = 1;
    public static final int STATE_RECV_FILE = 2;
    private int state = STATE_WAIT_RESP;

    // Message Commands
    private final int CMD_HEARTBEAT = 0;
    private final int CMD_LOCKED = 1;
    private final int CMD_UNLOCKED = 2;
    private final int CMD_DECK_START = 3;
    private final int CMD_DECK_END = 4;
    private final int CMD_ACK = 5;
    private final int CMD_SEND_START = 6;
    private final int CMD_SEND_DATA = 7;
    private final int CMD_SEND_END = 8;

    // Message Structure
    private final int INDEX_CMD = 0;
    private final int INDEX_REV_NUM = 4;
    private final int INDEX_FILE_SIZE = 4;
    private final int INDEX_FILE_NAME = 8;
    private final int INDEX_FILE_DATA = 4;

    // if there's an error in the command
    private final int ERROR_IN_CMD = -1;

    // Bluetooth packet size
    private final int BUFFER_SIZE = 251;

    public static final String TAG = "BLUETOOTH_MANAGER";

    // Deck Revision numbers
    private int cur_rev_num = 0;
    private int cur_pi_rev_num = 0;
    private int prev_rev_num = 0;

    // File Transfer members
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
//            Message writeErrorMsg =
//                    handler.obtainMessage(BluetoothService.MessageConstants.MESSAGE_TOAST);
//            Bundle bundle = new Bundle();
//            bundle.putString("toast",
//                    "Couldn't send data to the other device");
//            writeErrorMsg.setData(bundle);
//            handler.sendMessage(writeErrorMsg);
        }
    }

    private void runStateMachine(byte msg[], int numBytes) {
        int command = getMsgInt(msg, INDEX_CMD);
        switch(state) {
            case STATE_WAIT_RESP:
                parseCmdWaitResp(command, msg, numBytes);
                break;
            case STATE_RECV_FILE:
                parseCmdRecvFile(command, msg, numBytes);
                break;
            case STATE_SEND_FILE:
                parseCmdSendFile(command, msg, numBytes);
                break;
            default:
                System.err.print("State Error in Connection Management Thread!");
        }
    }

    public void parseCmdWaitResp(int command, byte msg[], int numBytes) {
        boolean command_error = false;
        switch (command) {
            case CMD_HEARTBEAT:
                // Update the pi's revision number and send our current revision number
                cur_pi_rev_num = getMsgInt(msg, INDEX_REV_NUM);
                break;
            case CMD_LOCKED:
                // TODO: Revert deck back to revision from pi, save change log
                cur_rev_num = prev_rev_num;
                break;
            case CMD_UNLOCKED:
                // Prepare to receive files from the Pi.
                state = STATE_RECV_FILE;
                break;
            default:
                command_error = true;
                break;
        }

        if (!command_error) {
            sendCmdAndRev(command);
        } else {
            // TODO: error handling response to pi. Maybe reset to heartbeat?
        }
    }

    public void parseCmdRecvFile(int command, byte msg[], int numBytes) {
        int response_cmd = CMD_ACK;
        switch (command) {
            case CMD_DECK_START:
                recv_file_buffer = null;
                recv_file_name = null;
                break;
            case CMD_SEND_START:
                int file_length = getMsgInt(msg, INDEX_FILE_SIZE);
                recv_file_buffer = ByteBuffer.allocate(file_length);
                String file_name = getMsgStr(msg, INDEX_FILE_NAME, msg.length);
                recv_file_name = file_name;
                break;
            case CMD_SEND_DATA:
                // In these packets, there will be no revision number. Only command + payload
                byte payload[] = getPayload(msg, INDEX_FILE_DATA);
                recv_file_buffer.put(payload);
                break;
            case CMD_SEND_END:
                // Write to file
                writeReceivedFile();
                recv_file_name = null;
                recv_file_buffer = null;
                break;
            case CMD_DECK_END:
                // Load the send_file_queue with new filenames
                fillFileQueue();
                state = STATE_SEND_FILE;
                response_cmd = CMD_DECK_START;
                break;
        }
        sendCmdAndRev(response_cmd);
    }

    public void parseCmdSendFile(int command, byte msg[], int numBytes) {
        if (command == CMD_ACK) {
            // There is no file send in progress
            if (send_file_buffer == null) {

                // There are are still files to send
                if (send_file_queue.size() > 0) {
                    // Load new file into buffer
                    readSendFile();

                    // send start packet
                    sendCmdAndRev(CMD_DECK_START);
                }

                // There are no more files to send
                else {
                    // send end deck packet
                    state = STATE_WAIT_RESP;
                    sendCmdAndRev(CMD_DECK_END);
                }
            }

            // A file is currently being sent
            else {
                int file_buffer_length = send_file_buffer.array().length;
                int file_buffer_position = send_file_buffer.position();
                // The file is done sending
                if (file_buffer_position == file_buffer_length) {
                    send_file_buffer = null;
                    send_file_queue.remove();
                    // send end file packet
                    sendCmdAndRev(CMD_SEND_END);
                }

                // The file is not done, send the next packet
                else if (file_buffer_position < file_buffer_length && file_buffer_position > 0) {
                    // send data packet
                    // These packets only have command + payload
                    int end_index = Math.min(send_file_buffer.position() + BUFFER_SIZE, file_buffer_length);
                    int length = end_index - file_buffer_position;
                    ByteBuffer send_data = ByteBuffer.allocate(Integer.BYTES + length);
                    send_data.putInt(CMD_SEND_DATA);
                    send_file_buffer.get(send_data.array(), Integer.BYTES, length);
                    write(send_data.array());
                }
            }
        } else {
            // TODO: error handling response to pi.
        }
    }

    public void fillFileQueue() {
        send_file_queue.add(DECKLIST_FILE_NAME);
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
                        send_file_queue.add(current_string);
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
    }

    public void writeReceivedFile() {
        File file = null;
        FileOutputStream fos = null;
        try {

            file = new File(parent_activity.getFilesDir(), recv_file_name);
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
//                fos = parent_activity.openFileOutput(fileName, Context.MODE_PRIVATE);

            fos.write(recv_file_buffer.array());
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

    public void readSendFile() {
        if (send_file_queue.size() > 0) {
            String file_name = send_file_queue.peek();
            FileInputStream fis = null;

            try {
                fis = parent_activity.openFileInput(file_name);
                send_file_buffer = ByteBuffer.allocate(fis.available());
                fis.read(send_file_buffer.array());
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

    private void sendCmdAndRev(int command) {
        ByteBuffer send_bytes = ByteBuffer.allocate(Integer.BYTES * 2);
        send_bytes.putInt(command).putInt(cur_rev_num);
        write(send_bytes.array());
    }

    /**
     * temporary method for updating deck.
     * @param new_rev_num
     */
    public void updateDeck(int new_rev_num) {
        prev_rev_num = cur_rev_num;
        cur_rev_num = new_rev_num;
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
