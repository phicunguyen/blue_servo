package com.bluetooth.blue_servo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private UUID MY_UUID = UUID.fromString("1e0ca4ea-299d-4335-93eb-27fcfe7fa848");
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;
    final byte delimiter = 33;
    Handler handle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //phi_check
        if(!mBluetoothAdapter.isEnabled())
        {
            //Toast.makeText(this, "Phi enable blue tooth", Toast.LENGTH_SHORT).show();
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            //Toast.makeText(this, "size: " + pairedDevices.size(), Toast.LENGTH_SHORT).show();
            for(BluetoothDevice device : pairedDevices)
            {
                Toast.makeText(this, device.getName(), Toast.LENGTH_SHORT).show();
                if(device.getName().equals("raspberrypi")) //Note, you will need to change this to match the name of your device
                {
                    //Toast.makeText(this, "ok found it: " + device.getName(), Toast.LENGTH_SHORT).show();
                    mmDevice = device;
                    break;
                }
            }
        }
    }

    class servoThread implements Runnable {
        String msg;
        public servoThread(String msg) {
            this.msg = msg;
        }
        @Override
        public void run() {
            //sendBtMsg(btMsg);
            sendBtMsg(msg);
            while (!Thread.currentThread().isInterrupted()) {
                int bytesAvailable = 0;
                boolean workDone = false;
                final InputStream mmInputStream;
                try {
                    mmInputStream = mmSocket.getInputStream();
                    bytesAvailable = mmInputStream.available();
                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        //byte[] readBuffer = new byte[1024];
                        mmInputStream.read(packetBytes);
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == delimiter) {
                                workDone = true;
                                break;
                            }
                        }
                    }
                    if (workDone == true){
                        mmSocket.close();
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void severo_zero_degree(View view) {
        Toast.makeText(getApplicationContext(), "severo_zero_degree", Toast.LENGTH_SHORT).show();
        (new Thread(new servoThread("0"))).start();
    }

    public void severo_ninety_degree(View view) {
        Toast.makeText(getApplicationContext(), "severo_ninety_degree", Toast.LENGTH_SHORT).show();
        (new Thread(new servoThread("90"))).start();
    }

    public void severo_one_eighty_degree(View view) {
        Toast.makeText(getApplicationContext(), "severo_one_eighty_degree", Toast.LENGTH_SHORT).show();
        (new Thread(new servoThread("180"))).start();
    }

    @SuppressLint("MissingPermission")
    public void sendBtMsg(String msg2send) {
        //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //Standard SerialPortService ID
        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            if (!mmSocket.isConnected()) {
                mmSocket.connect();
            }
            String msg = msg2send;
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
