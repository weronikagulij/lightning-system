package com.example.sw_app;
//
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
//    Button b1,b2,b3,b4;
//    private BluetoothAdapter BA;
//    private Set<BluetoothDevice> pairedDevices;
//    ListView lv;
//    private OutputStream outputStream;
//    private BluetoothSocket socket = null;
    private String TAG = "mymsg";
    private UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
    private final static int REQUEST_ENABLE_BT = 1;
    private ButtonManager buttonManager;
    private SeekBar seekBarR;
    private SeekBar seekBarG;
    private SeekBar seekBarB;
    private ConnectThread c;
    BluetoothHeadset bluetoothHeadset;

    // Get the default adapter
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = (BluetoothHeadset) proxy;
            }
        }
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = null;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekBarR = findViewById(R.id.seekBarR);
        seekBarG = findViewById(R.id.seekBarG);
        seekBarB = findViewById(R.id.seekBarB);
//        bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);
//        //
//        bluetoothAdapter.closeProfileProxy(bluetoothHeadset);
        buttonManager = new ButtonManager();
//        ButtonState first = new ButtonState();

        buttonManager.addButton(new ButtonState(1));
        buttonManager.addButton(new ButtonState(2));
        buttonManager.addButton(new ButtonState(3));
        buttonManager.addButton(new ButtonState(4));

        buttonManager.setActiveButton(buttonManager.getButtonByNumber(0));

        seekBarR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                buttonManager.getActiveButton().setR(progress);
                ButtonState tmp = buttonManager.getActiveButton();
                c.writeMsg(tmp.getButtonNumber() +
                        ";" + tmp.getR() + ";" + tmp.getG() + ";" + tmp.getB());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                buttonManager.getActiveButton().setG(progress);
                ButtonState tmp = buttonManager.getActiveButton();
                c.writeMsg(tmp.getButtonNumber() +
                        ";" + tmp.getR() + ";" + tmp.getG() + ";" + tmp.getB());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                buttonManager.getActiveButton().setB(progress);
                ButtonState tmp = buttonManager.getActiveButton();
                c.writeMsg(tmp.getButtonNumber() +
                        ";" + tmp.getR() + ";" + tmp.getG() + ";" + tmp.getB());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d("mymsg", "not supporting bluetooth");
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Log.d("mymsg", "was not enabled");
        }

//        Intent discoverableIntent =
//                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//        startActivity(discoverableIntent);

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("mymsg", deviceName);
                if(deviceName.equals("raspberrypi")) {
                    c = new ConnectThread(device);
                    c.run();
                    break;
                }
//                Log.d(TAG, c.toString());
//                c.writeMsg("message");


            }
        }

    }

    public void setBulb(View view) {
        System.out.println(((Button)view).getText());
        buttonManager.setActiveButtonByNumber(Integer.valueOf(((Button)view).getText().toString()));
        seekBarR.setProgress(buttonManager.getActiveButton().getR());
        seekBarG.setProgress(buttonManager.getActiveButton().getG());
        seekBarB.setProgress(buttonManager.getActiveButton().getB());
    }

    public void exit(View view) {
        c.writeMsg("exit");
        c.cancel();
        System.exit(0);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private OutputStream outputStream;
        public boolean isAvailable = false;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;

            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
//                tmpIn = socket.getInputStream();
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "able to run");
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                outputStream = mmSocket.getOutputStream();
                isAvailable = true;
//                writeMsg("message");
                Log.d(TAG, "able to connect!");

            } catch (IOException connectException) {
                Log.d(TAG, "unable to connect, " + connectException.getMessage());
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

        }

        public void writeMsg(String msg) {
            try {

                if(c.isAvailable) {
                    Log.d(TAG, msg);
                    outputStream.write(msg.getBytes());
                }
            } catch (IOException e) {
                Log.e(TAG, "Error during writing message", e);
            }
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
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        b1 = (Button) findViewById(R.id.button);
//        b2=(Button)findViewById(R.id.button2);
//        b3=(Button)findViewById(R.id.button3);
//        b4=(Button)findViewById(R.id.button4);
//
//        BA = BluetoothAdapter.getDefaultAdapter();
//        lv = (ListView)findViewById(R.id.listView);
//
//    }
//
//    public void on(View v){
//        if (!BA.isEnabled()) {
//            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(turnOn, 0);
//            Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    public void off(View v){
//        BA.disable();
//        Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_LONG).show();
//    }
//
//
//    public  void visible(View v){
//        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        startActivityForResult(getVisible, 0);
//    }
//
//
//    public void list(View v) throws IOException {
//        pairedDevices = BA.getBondedDevices();
//
//        ArrayList list = new ArrayList();
//
//        for(BluetoothDevice bt : pairedDevices) {
////            list.add(bt.getName());
////            ParcelUuid[] uuids = bt.getUuids();
////            BluetoothSocket socket = bt.createRfcommSocketToServiceRecord(uuids[0].getUuid());
////            socket.connect();
////            outputStream = socket.getOutputStream();
////            Log.d("mymsg", outputStream.toString());
////            Log.d("mymsg", bt.getName());
//            Log.d("mymsg", bt.getAddress());
//                try {
//                    ParcelUuid[] uuids = bt.getUuids();
//                    socket = bt.createInsecureRfcommSocketToServiceRecord(uuids[0].getUuid());
//
//                    Log.d("mymsg", "udalo sie pobrac uuid " + uuids[0].getUuid());
//
//                } catch (IOException e0) {
//                    Log.d("BT_TEST", "Cannot create socket");
//                    e0.printStackTrace();
//                }
//
//                try {
//                    socket.connect();
//                    Log.d("mymsg", "udalo sie podlaczyc");
//                } catch (IOException e1) {
//                    Log.d("mymsg", " nie udalo sie podlaczyc " + e1.getMessage());
//                    try {
//                        socket.close();
//                        Log.d("BT_TEST", "Cannot connect");
//                        e1.printStackTrace();
//                    } catch (IOException e2) {
//                        Log.d("BT_TEST", "Socket not closed");
//                        e2.printStackTrace();
//                    }
//                }
//        }
//        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();
//
//        final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
//
//        lv.setAdapter(adapter);
//    }
}