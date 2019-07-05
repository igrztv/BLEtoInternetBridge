package com.example.mishkatoy;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class BluetoothService extends Service {

    BluetoothAdapter bluetoothAdapter;
    public static final String EGG_RESPONSE = "ble_response";
    public static final String BLE_REQUEST = "ble_request";

    private int counter = 1;

    public int getCounter() {
        return counter;
    }

    public BluetoothService() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        Log.e("BLE Service", "onStartCommand");

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        while(true) {
            SystemClock.sleep(1000);
            counter += 1;
            Log.e("BLE Service", "running");
        }

        // If we get killed, after returning from here, restart
        // return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
}
