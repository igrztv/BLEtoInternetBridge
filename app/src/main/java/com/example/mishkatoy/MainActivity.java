package com.example.mishkatoy;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.mishkatoy.BLEService.EXTRA_DATA;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    BLEService mService;
    boolean mBound = false;
    boolean bluetoothScanAccess = false;
    ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
    Set<BluetoothDevice> deviceSet = new HashSet<>();
    static final int MY_PERMISSION_REQUEST_CONSTANT = 1;

    void bindBLE() {
        Log.e("MAIN", "onBind");
        Intent intent = new Intent(this, BLEService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    Context context;
    ListView bleDeviceList;
    final Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        Button searchButton = findViewById(R.id.button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBound) {
                    list.clear();
                    // list.addAll(mService.getDevices());
                    bleDeviceList.setAdapter(new ArrayAdapter<>(ctx,
                            android.R.layout.simple_list_item_1, list));
                    mService.startScanDevices();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        bleDeviceList = (ListView) findViewById(R.id.deviceList);
        bleDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?>adapter, View v, int position, long id){
                Object item = adapter.getItemAtPosition(position);
                Log.e("onItemClick", item.toString() + " at " + position + ", id: " + id);
                mService.connectBLE(list.get(position));
            }
        });

        Button singleButton = findViewById(R.id.button4);
        Button sequenceButton = findViewById(R.id.button7);
        singleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { sendData("single"); }
        });
        sequenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { sendData("sequence"); }
        });

        context = getApplicationContext();

        // Request permissions to scan bluetooth devices
        String perm[] = {Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(this, perm, MY_PERMISSION_REQUEST_CONSTANT);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CONSTANT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bluetoothScanAccess = true;

                    IntentFilter filter = new IntentFilter();
                    filter.addAction(BluetoothDevice.ACTION_FOUND);
                    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    filter.addAction(BLEService.ACTION_BLE_DEVICE_FOUND);
                    filter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
                    filter.addAction(BLEService.ACTION_DATA_AVAILABLE);
                    registerReceiver(receiver, filter);

                    Intent intent = new Intent(this, BluetoothService.class);
                    startService(intent);
                    bindBLE();

                } else {

                    Toast toast = Toast.makeText(context, "Cannot start bluetooth );", Toast.LENGTH_SHORT);
                    toast.show();

                    bluetoothScanAccess = false;
                }
            }
        }
    }

    public void sendData(String button) {
        if (button.equals("single")) {
            // mService.sendCommand("single:");
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("MAIN", "onDestroy!");
        super.onDestroy();
        unbindService(connection);
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        Log.e("MAIN", "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.e("MAIN", "onPause");
        super.onPause();
    }

    @Override
    protected void onStart() {
        Log.e("MAIN", "onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.e("MAIN", "onStop");
        super.onStop();
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("BROADCAST", "onReceive " + action);
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.e("BROADCAST", "ACTION_DISCOVERY_STARTED");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e("BROADCAST", "ACTION_DISCOVERY_FINISHED");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                list.add(device);
                bleDeviceList.setAdapter(new ArrayAdapter<BluetoothDevice>(ctx,
                        android.R.layout.simple_list_item_1, list));
                Log.e("BROADCAST", device.getName());
            } else if (BLEService.ACTION_BLE_DEVICE_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceSet.add(device);
                if (!list.contains(device)){
                    list.add(device);
                }
                bleDeviceList.setAdapter(new ArrayAdapter<BluetoothDevice>(ctx,
                        android.R.layout.simple_list_item_1, list));
                Log.e("BROADCAST", device.getName());
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                TextView responce = findViewById(R.id.textView3);
                String data = intent.getStringExtra(BLEService.EXTRA_DATA);
                if (data != null) {
                    responce.setText(data + "\n");
                    Log.e("DATA", data);
                }
            } else if (BluetoothService.MESSAGE_READ.equals(action)) {
                TextView responce = findViewById(R.id.textView3);
                String data = intent.getStringExtra("BLE_data");
                responce.setText(data);
            }
        }
    };

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e("MAIN", "onServiceConnected!!!");
            BLEService.LocalBinder binder = (BLEService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            if (mService.startBLE() != 0) {
                Toast toast = Toast.makeText(context, "Cannot start bluetooth ;(", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e("MAIN", "onServiceDisconnected!!!");
            mBound = false;
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_device) {
            // Handle the camera action
        } else if (id == R.id.nav_content) {

        } else if (id == R.id.nav_connect) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
