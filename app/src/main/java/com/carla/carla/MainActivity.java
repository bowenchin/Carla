package com.carla.carla;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CARLA";
    private static final int numSamples = 30;

    private TextView date;
    private TextView trip_update;
    private TextView duration_update;
    private TextView trip_counter;
    private TextView timer_count;
    private TextView inputData;

    private String input;

    private int counter;
    private int flag;
    private long sumArray = 0;
    private int countTimes = 0;
    private long startTime = 0;
    private long endTime = 0;

    private long tStart;
    private long tEnd;
    private long tDelta;
    private double elapsedSeconds;
    private int timeFlag;

    Handler bluetoothIn;
    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    int[] samples = new int[numSamples];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setElevation(0);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = sdf.format(d);

        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());
        date = (TextView) findViewById(R.id.date);
        date.setText(dayOfTheWeek + ", " + currentDateTimeString);

        trip_update = (TextView) findViewById(R.id.trip_update_time);
        trip_update.setText(new SimpleDateFormat("HH:mm").format(new Date()));

        duration_update = (TextView) findViewById(R.id.duration_update_time);
        duration_update.setText(new SimpleDateFormat("HH:mm").format(new Date()));

        trip_counter = (TextView)findViewById(R.id.trip_counter);
        timer_count = (TextView)findViewById(R.id.timer);
        inputData = (TextView)findViewById(R.id.inputData);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);      								//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
//                        timer_count.setText(dataInPrint);
                        inputData.setText("Data Received = " + dataInPrint);

                        input = dataInPrint.substring(1,endOfLineIndex);

                        timeFlag = 0;


                        //BEACON STATUS PROCESSING
                        if(input.equals("IN")){
                            flag = 1;
                            counter+=1;
                            trip_counter.setText(String.valueOf(counter));
                            tStart = System.currentTimeMillis();
                            timeFlag = 1;

                        }
                        else if (input.equals("OUT")){
                            flag = 0;
                            if(timeFlag==1) {
                                tEnd = System.currentTimeMillis();
                                tDelta = tEnd - tStart;
                                elapsedSeconds = tDelta / 1000.0;
                                timer_count.setText(elapsedSeconds + " seconds");
                                timeFlag = 0;
                            }
                        }
                        /*
                        //initialize array
                        for (int i = 0; i < numSamples; i++) {
                            samples[i] = 0;
                        }
                        lastRead = 0;

                        for(int i = 0; i > numSamples; i--){
                            samples[i] = samples[i-1];
                        }
                        samples[0] = flag;
                        sumArray = 0;
                        for(int i = 0; i < numSamples; i++){
                            sumArray = sumArray + samples[i];
                        }

                        if(sumArray > 0){
                            if(flag == 0){
                                startTime = System.currentTimeMillis();
                                countTimes++;

                                flag = 1;
                            }
                            else if (flag == 1){
                                endTime = System.currentTimeMillis() - startTime;
                                timer_count.setText(Long.toString(endTime/1000) + "seconds");

                                flag = 0;
                            }
                        }

                        Log.d(TAG, "FLAG: " + flag);
                        Log.d(TAG, "LAST READ: " + lastRead);

                        */

                        if (recDataString.charAt(0) == '#')								//if it starts with # we know it is what we are looking for
                        {

                            trip_counter.setText(String.valueOf(countTimes));
                            Log.d(TAG, "Data received: " + input);

                        }
                        recDataString.delete(0, recDataString.length()); 					//clear all string data
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

    }



    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        //address = "9C:1D:58:A3:BD:7F";

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");

    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                //finish();

            }
        }
    }
}
