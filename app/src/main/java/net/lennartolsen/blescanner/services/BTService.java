package net.lennartolsen.blescanner.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by lennartolsen on 22/03/2017.
 */

public class BTService extends Service {
    private final String TAG = "BTService";
    ArrayList<String> deviceList;

    BluetoothAdapter mBluetoothAdapter = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        Log.e(TAG, "Looking for BT Devices");
        // Setup broadcast receiver for device detection (scan for devices)
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mBluetoothReceiver, filter);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Scanning started
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.e(TAG, "Search starting");
                //deviceList.clear();
            }

            // New device found
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                /*if(deviceName != null){
                    deviceList.add(deviceName + " : " + rssi + "dbm");
                } else{
                    deviceList.add(deviceHardwareAddress + " : " + rssi + "dbm");
                }*/

                Log.e(TAG, "BT device Signal: " + rssi + "dbm");
                Log.e(TAG, "BT device: " + deviceName + deviceHardwareAddress);

                /** TODO : Handle found devices **/
            }

            // scanning ended
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e(TAG, "Search ended");
                //mBluetoothAdapter.startDiscovery();
            }
        }
    };

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "Bound Yo");
        return binder;
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        LocalBinder(){
            Log.e(TAG, "BT LOCAL BINDER CONSTRUCT");
        }
        public BTService getService() {
            Log.e(TAG, "Starting BT service");
            return BTService.this;
        }
    }
}