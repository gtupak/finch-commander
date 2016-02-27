package com.finch_commander;

import android.content.Intent;
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
    private TextView logger;

    private Audio startEarcon;
    private Audio stopEarcon;
    private Audio errorEarcon;

    private Session session;
    private Transaction recTransaction;

    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private final UUID uuid = UUID.fromString("322b3d7c-dd9c-11e5-b86d-9a79f06e9478");

    private final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleRec = (Button)findViewById(R.id.toggleRec);
        toggleRec.setOnClickListener(this);
        logger = (TextView)findViewById(R.id.logger);

        // Set up Nuance
        session = Session.Factory.session(this, Configuration.SERVER_URI, Configuration.APP_KEY);
        loadEarcons();
        setState(State.IDLE);

        // Set up Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        // Make sure Bluetooth is enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            // Get paired devices
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            logger.append("\nPaired devices acquired. Number of devices: " + pairedDevices.size());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                logger.append("\nBluetooth successfully turned on.\nAcquiring paired devices...");
                pairedDevices = mBluetoothAdapter.getBondedDevices();
                logger.append("\nPaired devices acquired. Number of devices: " + pairedDevices.size());
            }
            else {
                logger.append("\nERROR: Bluetooth has to be turned on.");
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

    @Override
    public void onClick(View v) {
        if (v == toggleRec){
            toggleRec();
        }
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

    private enum State {
        IDLE,
        LISTENING,
        PROCESSING
    }
}
