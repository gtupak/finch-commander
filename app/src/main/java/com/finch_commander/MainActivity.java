package com.finch_commander;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.nuance.speechkit.Audio;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Recognition;
import com.nuance.speechkit.RecognitionType;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * NOTE: Configuration class can be made from the Nuance sample app
 * and updated with your own credentials.
 *
 * Sample app at: https://developer.nuance.com/public/index.php?task=prodDev
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private State state = State.IDLE;

    private Button toggleRec;
    private Button showDevicesButton;
    private Button sendMsgButton;
    private TextView logger;

    private Audio startEarcon;
    private Audio stopEarcon;
    private Audio errorEarcon;

    private Session session;
    private Transaction recTransaction;

    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private final UUID uuid = UUID.fromString("40850b4a-de88-11e5-b86d-9a79f06e9478"); //UUID.fromString("40850b4a-de88-11e5-b86d-9a79f06e9478");

    private ConnectedThread mConnectedThread;

    private final int REQUEST_ENABLE_BT = 1;
    private final int MESSAGE_READ = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleRec = (Button)findViewById(R.id.toggleRec);
        toggleRec.setOnClickListener(this);
        showDevicesButton = (Button) findViewById(R.id.showDevicesButton);
        showDevicesButton.setOnClickListener(this);
        sendMsgButton = (Button) findViewById(R.id.sendMsgButton);
        sendMsgButton.setOnClickListener(this);

        logger = (TextView)findViewById(R.id.logger);

        // Set up Nuance
        session = Session.Factory.session(this, Configuration.SERVER_URI, Configuration.APP_KEY);
        loadEarcons();
        setState(State.IDLE);

        // Set up Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setUpBluetooth();

        // Find Bluetooth device that is the Finch server
        BluetoothDevice finchServerDevice = null;
        if (pairedDevices == null) {
            logger.append("\nERROR: No paired devices available.");
            return;
        }

        finchServerDevice = findFinchServer(pairedDevices);
        if (finchServerDevice == null) {
            logger.append("\nERROR: Could not find device with required UUID. Our UUID variant : " + uuid.variant());
            return;
        }

        // Connect to device
        ConnectThread connectThread = new ConnectThread(finchServerDevice);
        // Connect and send a test command (see the run() code)
        connectThread.start();
        logger.append("\nConnection successfully established with " + connectThread.mmSocket.getRemoteDevice().getName());
    }

    /**
     * Used for managing connection - transferring data
     */
    public void manageConnectedSocket(BluetoothSocket btSocket) {
        ConnectedThread connectedThread = new ConnectedThread(btSocket);
        connectedThread.start();

        // Send test command to the server
        mConnectedThread = connectedThread;
//        String testCommand = "COMMAND: ---SUCCESS---";
//        connectedThread.write(testCommand.getBytes());
    }

    @Override
    public void onClick(View v) {
        if (v == toggleRec){
            toggleRec();
        }
        else if (v == showDevicesButton) {
            getPairedDevicesAndPrint();
        }
        else if (v == sendMsgButton) {
            if (mConnectedThread != null) {
                String testCommand = "COMMAND: ---SUCCESS---";
                mConnectedThread.write(testCommand.getBytes());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                logger.append("\nBluetooth successfully turned on.\nAcquiring paired devices...");
                getPairedDevicesAndPrint();
            }
            else {
                logger.append("\nERROR: Bluetooth has to be turned on.");
            }
        }
    }

    /**
     * Helper device to find the device that holds the Finch server
     */
    private BluetoothDevice findFinchServer(Set<BluetoothDevice> devices) {
        for (BluetoothDevice device : devices) {
            for (ParcelUuid deviceUuid : device.getUuids()) {
                // TODO Tried to compare by actual UUID but device's UUID was printed backwards. Getting UUID that has variant 7 for now
                if (deviceUuid.getUuid().variant() == 7) {
                    return device;
                }
            }
        }
        return null;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == MESSAGE_READ) {
                    byte[] readbuf = (byte[]) msg.obj;
                    // Construct a string from the buffer
                    String messageReceived = new String(readbuf, 0, msg.arg1);
                    logger.append("\nRECEIVED: " + messageReceived);
            }
        }
    };

    /**
     * Sets up Bluetooth and connectivity
     */
    private void setUpBluetooth() {
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        // Make sure Bluetooth is enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            getPairedDevicesAndPrint();
        }
    }

    /**
     * Gets paired devices and displays them in the logger
     */
    private void getPairedDevicesAndPrint() {
        // Clear logger
        logger.setText("");

        // Get paired devices
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        logger.append("\nPaired devices acquired. Number of devices: " + pairedDevices.size());
    }

    private void displayDevices(Set<BluetoothDevice> devices) {
        for (BluetoothDevice bluetoothDevice : devices) {
            logger.append("\n" + bluetoothDevice.getName());
            ParcelUuid deviceUuids[] =  bluetoothDevice.getUuids();
            for (ParcelUuid parcelUuid : deviceUuids) {
                logger.append("\n    Variant: " + parcelUuid.getUuid().variant());
            }
        }
    }

    /**
     * Set the state and update the button text
     */
    private void setState(State newState) {
        state = newState;
        switch (newState) {
            case IDLE:
                toggleRec.setText("Recognize");
                break;
            case LISTENING:
                toggleRec.setText("Listening...");
                break;
            case PROCESSING:
                toggleRec.setText("Processing...");
                break;
        }
    }

    /**
     * Earcons
     */
    private void loadEarcons() {
        //Load all the earcons from disk
        startEarcon = new Audio(this, R.raw.sk_start, Configuration.PCM_FORMAT);
        stopEarcon = new Audio(this, R.raw.sk_stop, Configuration.PCM_FORMAT);
        errorEarcon = new Audio(this, R.raw.sk_error, Configuration.PCM_FORMAT);
    }

    /**
     * Recording transactions
     */
    private void toggleRec() {
        switch (state){
            case IDLE:
                recognize();
                break;
            case LISTENING:
                stopRecording();
                break;
            case PROCESSING:
                cancel();
                break;
        }
    }

    /**
     * Start listening and send voice to server
     */
    private void recognize(){
        // Setup rec transaction options
        Transaction.Options options = new Transaction.Options();
        options.setRecognitionType(RecognitionType.DICTATION);
        options.setDetection(DetectionType.Short);
        options.setLanguage(new Language("eng-USA"));
        options.setEarcons(startEarcon, stopEarcon, errorEarcon, null);

        // Start listening
        recTransaction = session.recognize(options, recListener);
    }

    /**
     * Used for Nuance
     */
    private Transaction.Listener recListener = new Transaction.Listener(){
        @Override
        public void onStartedRecording (Transaction transaction){
            logger.append("\nStart recording..");
            setState(State.LISTENING);
        }

        @Override
        public void onFinishedRecording(Transaction transaction){
            logger.append("\nRecording finished.");
            setState(State.PROCESSING);
        }

        @Override
        public void onRecognition(Transaction transaction, Recognition recognition){
            logger.append("\nYou said: " + recognition.getText());
            setState(State.IDLE);
        }

        @Override
        public void onSuccess(Transaction transaction, String s){
            // Notification of a successful transaction.. Nothing to do
        }

        @Override
        public void onError(Transaction transaction, String s, TransactionException ex){
            logger.append("\nError: " + ex.getMessage() + ". " + s);

            //Something went wrong. Check Configuration.java to ensure that your settings are correct.
            //The user could also be offline, so be sure to handle this case appropriately.
            //We will simply reset to the idle state.
            setState(State.IDLE);
        }
    };

    /**
     * Stop recording the user
     */
    private void stopRecording(){
        recTransaction.stopRecording();
    }

    /**
     * Cancel the rec transaction
     */
    private void cancel() {
        recTransaction.cancel();
        setState(State.IDLE);
    }

    /**
     * Different states for Nuance's API
     */
    private enum State {
        IDLE,
        LISTENING,
        PROCESSING
    }

    /**
     * Commands for socket managing
     */
    private enum Command {
        SEND_MESSAGE
    }

    /**
     * Thread that listens for input and output streams
     * when connected to a device
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException ex) {  }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte [] buffer = new byte[1024]; // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while(true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException ex) {
                    break;
                }
            }
        }

        /**
         * Call this from the main activity to send data to the remote device
         */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException ex) {  }
        }

        /**
         * Call this from the main activity to shutdown the connection
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ex) {  }
        }
    }

    /**
     * Thread used to start a connection with a BluetoothDevice
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException ex) {
                logger.append("\nERROR: Caught IOException in ConnectThread(BluetoothDevice device)");
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                logger.append("\nERROR: Unable to connect. Closing the socket.");
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    logger.append("\nERROR: Unable to close socket.");
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /**
         * Will cancel an in-progress connection and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                logger.append("\nERROR: Unable to cancel connection.");
            }
        }
    }
}
