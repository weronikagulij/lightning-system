package com.example.sw_app;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private String TAG = "mymsg";
    private UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
    private final static int REQUEST_ENABLE_BT = 1;
    private ButtonManager buttonManager;
    private SeekBar seekBarR;
    private SeekBar seekBarG;
    private SeekBar seekBarB;
    private List<View> ledButtons;
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

        ledButtons = new ArrayList<View>();
        ledButtons.add(findViewById(R.id.button1));
        ledButtons.add(findViewById(R.id.button2));
        ledButtons.add(findViewById(R.id.button3));
        ledButtons.add(findViewById(R.id.button4));

        seekBarR = findViewById(R.id.seekBarR);
        seekBarG = findViewById(R.id.seekBarG);
        seekBarB = findViewById(R.id.seekBarB);

        seekBarR.setProgress(1);
        seekBarB.setProgress(1);
        seekBarG.setProgress(1);
        buttonManager = new ButtonManager();

        buttonManager.addButton(new ButtonState(1));
        buttonManager.addButton(new ButtonState(2));
        buttonManager.addButton(new ButtonState(3));
        buttonManager.addButton(new ButtonState(4));

        buttonManager.setActiveButton(buttonManager.getButtonByNumber(0));

        ledButtons.get(0).setActivated(true);

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

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                Log.d("mymsg", deviceName);
                if(deviceName.equals("raspberrypi")) {
                    c = new ConnectThread(device);
                    c.run();
                    break;
                }
            }
        }

    }

    public void setBulb(View view) {
        ledButtons.get(buttonManager.getActiveButton().getButtonNumber() - 1).setActivated(false);

        buttonManager.setActiveButtonByNumber(Integer.valueOf(((Button)view).getText().toString()) - 1);
        seekBarR.setProgress(buttonManager.getActiveButton().getR());
        seekBarG.setProgress(buttonManager.getActiveButton().getG());
        seekBarB.setProgress(buttonManager.getActiveButton().getB());
        ledButtons.get(buttonManager.getActiveButton().getButtonNumber() - 1).setActivated(true);
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
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "able to run");
            try {
                mmSocket.connect();
                outputStream = mmSocket.getOutputStream();
                isAvailable = true;
                Log.d(TAG, "able to connect!");

            } catch (IOException connectException) {
                Log.d(TAG, "unable to connect, " + connectException.getMessage());
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
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
}