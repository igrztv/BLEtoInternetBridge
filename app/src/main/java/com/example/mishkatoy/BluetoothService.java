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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;
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
        leScanner.startScan();

        scanCallback */

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
                }
            }).start();
        }

        return true; // == connecting...
    }

    public void disconnectBLE() {
        try {
            Log.e("BleutoothSocket", "Close the client socket by request");
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    public boolean sendCommand(String cmd) {
        if (mmSocket.isConnected()) {

        }
        return false;
    }

    public String receiveResponse() {
        String response = "";
        return response;
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
