package com.example.mishkatoy;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static android.support.constraint.Constraints.TAG;

public class BLEService extends Service {

    private final static String TAG = BLEService.class.getSimpleName();

    private BluetoothLeScanner LeScanner;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private String bluetoothDeviceAddress;
    private final IBinder binder = new BLEService.LocalBinder();
    private BluetoothGatt bluetoothGatt;
    private boolean mScanning;
    private Handler handler = new Handler();
    private int connectionState = STATE_DISCONNECTED;
    private List<BluetoothGattService> gattServices;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_BLE_DEVICE_FOUND =
            "com.example.mishkatoy.ble.ACTION_BLE_DEVICE_FOUND";
    public final static String ACTION_GATT_CONNECTED =
            "com.example.mishkatoy.ble.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.mishkatoy.ble.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.mishkatoy.ble.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.mishkatoy.ble.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.mishkatoy.ble.EXTRA_DATA";

    public final static UUID UUID_YOUR_DEVICE_UUID =
            UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public final static UUID REMOTE_TX_UUID_CHARACTERISTIC =
            UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    public final static UUID NOTIFICATION_DESCRIPTOR =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

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

    public void stopScanDevices() {
        if (LeScanner != null) {
            mScanning = false;
            LeScanner.stopScan(leScanCallback);
        }
    }

    public void startScanDevices() {
        stopScanDevices();
        long SCAN_PERIOD = 10000;
        LeScanner = bluetoothAdapter.getBluetoothLeScanner();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanDevices();
            }
        }, SCAN_PERIOD);

        mScanning = true;
        LeScanner.startScan(leScanCallback);
    }

    // private LeDeviceListAdapter leDeviceListAdapter;
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            System.out.println("BLE// onScanResult");
            Log.e("callbackType", String.valueOf(callbackType));
            Log.e("result", result.toString());
            BluetoothDevice device = result.getDevice();
            if (device.getName() != null) {
                Log.e("device", device.getName());
                Intent broadCastIntent = new Intent();
                broadCastIntent.setAction(ACTION_BLE_DEVICE_FOUND);
                broadCastIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
                sendBroadcast(broadCastIntent);
            }

            /*if (result.getDevice().getName().equals("UART Service")) {
                connectBLE(result.getDevice());
            }*/
            // allDevices.add();

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            System.out.println("BLE// onBatchScanResults");
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            System.out.println("BLE// onScanFailed");
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    public boolean connectBLE(BluetoothDevice device) {
        stopScanDevices();
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        return true;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                connectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        bluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                connectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattServices = gatt.getServices();
                Log.w(TAG, "onServicesDiscovered received: " + gatt.getServices().toString());
                for (BluetoothGattService gattService : gattServices) {
                    Log.w(TAG, "\t\tService: " + gattService.getUuid().toString());
                    List<BluetoothGattCharacteristic> chara = gattService.getCharacteristics();
                    for (BluetoothGattCharacteristic gattCharacteristic : chara) {
                        Log.w(TAG, "\t\t\tcharacteristic: " + gattCharacteristic.getUuid().toString());
                        if (REMOTE_TX_UUID_CHARACTERISTIC.equals(gattCharacteristic.getUuid())) {
                            gatt.setCharacteristicNotification(gattCharacteristic, true);
                            Log.w(TAG, "\t\t\t\tset notification!");
                            BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(NOTIFICATION_DESCRIPTOR);
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.w(TAG, "onCharacteristicRead received: " + characteristic.getValue().toString());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.w(TAG, "onCharacteristicChanged: " + characteristic.getValue().toString());
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public List<BluetoothGattService> getServices() {
        return gattServices;
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                    stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    public void disconnectBLE() {

    }

    public class LocalBinder extends Binder {
        BLEService getService() {
            return BLEService.this;
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
