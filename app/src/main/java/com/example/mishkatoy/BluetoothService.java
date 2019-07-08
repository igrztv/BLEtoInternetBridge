package com.example.mishkatoy;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class BluetoothService extends Service {

    BluetoothAdapter bluetoothAdapter;
    public static final String BLE_RESPONSE = "ble_response";
    public static final String BLE_REQUEST = "ble_request";

    private final IBinder mBinder = new BluetoothBinder();

    private int counter = 1;
    private boolean runBluetoothThread = false;

    public int getCounter() {
        return counter;
    }

    public class BluetoothBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public void vibrate() {
        Log.e("BLE Service", "vibrating");
    }

    private void Tick() {
        if (!runBluetoothThread) {
            runBluetoothThread = true;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        while(true) {
                            counter += 1;
                            Log.e("BLE Service", "running" + Integer.toString(counter));
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        // Restore interrupt status.
                        Thread.currentThread().interrupt();
                    }
                }
            }).start();
        }
    }

    @Override
    public void onCreate() {
        Log.e("BLE Service", "onCreate");
        Tick();
        /*HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceHandler = new ServiceHandler(serviceLooper);
        serviceLooper = thread.getLooper();*/

        // Intent intent = new Intent(this, BluetoothService.class);
        // startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    public BluetoothService() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("BLE Service", "onBind");

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Tick();

        return mBinder;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
}
