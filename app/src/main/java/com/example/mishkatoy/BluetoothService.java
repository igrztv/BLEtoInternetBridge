package com.example.mishkatoy;

import android.app.ListActivity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static android.support.constraint.Constraints.TAG;

public class BluetoothService extends Service {

    private int counter = 0;
    private boolean counterRunning = false;

    private final IBinder binder = new LocalBinder();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket mmSocket;
    private Handler handler;

    public static final String MESSAGE_READ = "BluetoothMessageRead";
    public static final String MESSAGE_WRITE = "BluetoothMessageWrite";
    public static final String MESSAGE_TOAST = "BluetoothMessageToast";
    private OutputStream mmOutStream;
    private byte[] mmBuffer = new byte[1024];

    private Set<BluetoothDevice> pairedDevices;

    public int startBLE() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            return -1;
        }

        if (!bluetoothAdapter.isEnabled()) {
            if (bluetoothAdapter.enable()) {
                return 0;
            } else {
                return -1;
            }
        }

        return 0;
    }

    public void stopBLE() {

    }

    public void searchBLE() {
        /*
        BluetoothLeScanner leScanner = bluetoothAdapter.getBluetoothLeScanner();
        leScanner.startScan();*/
        if (bluetoothAdapter.startDiscovery()) {
            // activate spinner for best UI expierence
        }
    }

    public Set<BluetoothDevice> getDevices() {
        pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.e("BLE", "paired device count: " + pairedDevices.size());
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.i("BLE", deviceName + " = " + deviceHardwareAddress);
            }
        }
        return pairedDevices;
    }

    public boolean connectBLE(BluetoothDevice device) {

        if (mmSocket != null && mmSocket.isConnected()) {
            disconnectBLE();
        }

        final BluetoothDevice mDevice = device;

        if (!counterRunning) {
            counterRunning = true;
            new Thread(new Runnable() {

                InputStream mmInStream;

                public void run() {
                    BluetoothSocket tmp = null;
                    try {
                        // Get a BluetoothSocket to connect with the given BluetoothDevice.
                        // MY_UUID is the app's UUID string, also used in the server code.
                        UUID MY_UUID = new UUID(0x11112222,0x33334444);
                        tmp = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    } catch (IOException e) {
                        counterRunning = false;
                        Log.e("BleutoothSocket", "Socket's create() method failed", e);
                    }
                    mmSocket = tmp;

                    // Cancel discovery to help the connection
                    bluetoothAdapter.cancelDiscovery();

                    try {
                        // Connect to the remote device through the socket. This call blocks
                        // until it succeeds or throws an exception.
                        mmSocket.connect();
                    } catch (IOException connectException) {
                        // Unable to connect; close the socket and return.
                        Log.e("BleutoothSocket", "Could not connect. Close the client socket", connectException);
                        counterRunning = false;
                        try {
                            mmSocket.close();
                        } catch (IOException closeException) {
                            Log.e("BleutoothSocket", "Could not close the client socket", closeException);
                        }
                    }

                    InputStream tmpIn = null;
                    OutputStream tmpOut = null;

                    // Get the input and output streams; using temp objects because
                    // member streams are final.
                    try {
                        tmpIn = mmSocket.getInputStream();
                    } catch (IOException e) {
                        Log.e(TAG, "Error occurred when creating input stream", e);
                    }
                    try {
                        tmpOut = mmSocket.getOutputStream();
                    } catch (IOException e) {
                        Log.e(TAG, "Error occurred when creating output stream", e);
                    }

                    mmInStream = tmpIn;
                    mmOutStream = tmpOut;

                    // Keep listening to the InputStream until an exception occurs.
                    while (true) {
                        try {
                            // Read from the InputStream.
                            int numBytes = mmInStream.read(mmBuffer);
                            // Send the obtained bytes to the UI activity.
                            Intent broadCastIntent = new Intent();
                            broadCastIntent.setAction(MESSAGE_READ);
                            broadCastIntent.putExtra("BLE_data", mmBuffer.toString());
                            sendBroadcast(broadCastIntent);
                        } catch (IOException e) {
                            Log.d(TAG, "Input stream was disconnected", e);
                            break;
                        }
                    }
                }
            }).start();
        }

        return true; // == connecting...
    }


    public boolean sendCommand(String cmd) {
        try {
            mmOutStream.write(cmd.getBytes());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
            return false;
        }
    }

    public void disconnectBLE() {
        try {
            Log.e("BleutoothSocket", "Close the client socket by request");
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BLE", "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("BLE", "ondestroy!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
