package com.example.mishkatoy;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.UUID;

public class DeviceCharacteristics {

    public final static UUID UUID_YOUR_DEVICE_UUID =
            UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");

    public final static UUID REMOTE_RX_UUID_CHARACTERISTIC =
            UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public final static UUID REMOTE_TX_UUID_CHARACTERISTIC =
            UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    public final static UUID REMOTE_POWER_UUID_CHARACTERISTIC =
            UUID.fromString("6E400004-B5A3-F393-E0A9-E50E24DCCA9E");
    public final static UUID REMOTE_DURATION_UUID_CHARACTERISTIC =
            UUID.fromString("6E400005-B5A3-F393-E0A9-E50E24DCCA9E");
    public final static UUID REMOTE_PAUSE_UUID_CHARACTERISTIC =
            UUID.fromString("6E400006-B5A3-F393-E0A9-E50E24DCCA9E");
    public final static UUID REMOTE_DO_UUID_CHARACTERISTIC =
            UUID.fromString("6E400007-B5A3-F393-E0A9-E50E24DCCA9E");
    public final static UUID REMOTE_REPEAT_UUID_CHARACTERISTIC =
            UUID.fromString("6E400008-B5A3-F393-E0A9-E50E24DCCA9E");

    public BluetoothGattCharacteristic rxChar;
    public BluetoothGattCharacteristic txChar;
    public BluetoothGattCharacteristic powerChar;
    public BluetoothGattCharacteristic durationChar;
    public BluetoothGattCharacteristic pauseChar;
    public BluetoothGattCharacteristic doChar;
    public BluetoothGattCharacteristic repeatChar;

    public final static int CHARACTERISTIC_NOT_FOUND = 0;
    public final static int CHARACTERISTIC_FOUND = 1;
    public final static int NEED_NOTIFICATION = 2;


    public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        if (uuid.equals(REMOTE_RX_UUID_CHARACTERISTIC)) {
            return rxChar;
        }
        if (uuid.equals(REMOTE_TX_UUID_CHARACTERISTIC)) {
            return txChar;
        }
        if (uuid.equals(REMOTE_POWER_UUID_CHARACTERISTIC)) {
            return powerChar;
        }
        if (uuid.equals(REMOTE_DURATION_UUID_CHARACTERISTIC)) {
            return durationChar;
        }
        if (uuid.equals(REMOTE_PAUSE_UUID_CHARACTERISTIC)) {
            return pauseChar;
        }
        if (uuid.equals(REMOTE_DO_UUID_CHARACTERISTIC)) {
            return doChar;
        }
        if (uuid.equals(REMOTE_REPEAT_UUID_CHARACTERISTIC)) {
            return repeatChar;
        }
        return null;
    }


    public int findUUID(UUID uuid, BluetoothGattCharacteristic charPointer) {
        if (uuid.equals(REMOTE_RX_UUID_CHARACTERISTIC)) {
            rxChar = charPointer;
            return CHARACTERISTIC_FOUND;
        }
        if (uuid.equals(REMOTE_TX_UUID_CHARACTERISTIC)) {
            txChar = charPointer;
            return NEED_NOTIFICATION;
        }
        if (uuid.equals(REMOTE_POWER_UUID_CHARACTERISTIC)) {
            powerChar = charPointer;
            return CHARACTERISTIC_FOUND;
        }
        if (uuid.equals(REMOTE_DURATION_UUID_CHARACTERISTIC)) {
            durationChar = charPointer;
            return CHARACTERISTIC_FOUND;
        }
        if (uuid.equals(REMOTE_PAUSE_UUID_CHARACTERISTIC)) {
            pauseChar = charPointer;
            return CHARACTERISTIC_FOUND;
        }
        if (uuid.equals(REMOTE_DO_UUID_CHARACTERISTIC)) {
            doChar = charPointer;
            return CHARACTERISTIC_FOUND;
        }
        if (uuid.equals(REMOTE_REPEAT_UUID_CHARACTERISTIC)) {
            Log.e("REPEAT", "repeatChar");
            repeatChar = charPointer;
            return CHARACTERISTIC_FOUND;
        }
        return CHARACTERISTIC_NOT_FOUND;
    }

}
