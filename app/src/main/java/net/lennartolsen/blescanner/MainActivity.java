package net.lennartolsen.blescanner;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.*;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.kontakt.sdk.android.common.KontaktSDK;

import net.lennartolsen.blescanner.services.GPSService;
import net.lennartolsen.blescanner.services.KontaktService;

import java.util.*;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    TextView mCallbackText;

    GPSService gpsService;
    KontaktService kontaktService;

    MyDBHandler dbHandler;

    boolean isGPSBound = false;
    boolean isKontaktBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KontaktSDK.initialize(this);

        mCallbackText = (TextView) findViewById(R.id.debugView);

        if( handlePermissions() ) {
            if(this.kontaktService == null){startKontaktService();}
            if(this.gpsService == null){startGPSService();}
        }
        dbHandler = new MyDBHandler(this, null, 1);
        if(dbHandler.checkIfEmpty()){
            dbHandler.addDevice(new Device("sx8Z","Atrie"));
            dbHandler.addDevice(new Device("5f4E","Studiezone"));
            dbHandler.addDevice(new Device("HwqA","U168"));
            dbHandler.addDevice(new Device("9N4C","U167"));
            dbHandler.addDevice(new Device("iqvD","Gang"));
            dbHandler.addDevice(new Device("QP9Y","U166"));
            dbHandler.addDevice(new Device("S5XN","U165"));
            dbHandler.addDevice(new Device("3rMl","Studiezone"));
            dbHandler.addDevice(new Device("gkKV","Studiezone"));
            dbHandler.addDevice(new Device("KdtM","U164"));
            dbHandler.addDevice(new Device("krQd","U163"));
            dbHandler.addDevice(new Device("1XIJ","Gang"));
            dbHandler.addDevice(new Device("LxPa","U162"));
            dbHandler.addDevice(new Device("Y2bm","U161"));
            dbHandler.addDevice(new Device("h6YJ","U160"));
            dbHandler.addDevice(new Device("F29K","Studiezone"));
            dbHandler.addDevice(new Device("z5gv","Trappe"));
            dbHandler.addDevice(new Device("Ras3","Atrie"));
            dbHandler.addDevice(new Device("Vkrs","Trappe"));
            dbHandler.addDevice(new Device("RPpW","Gang"));
            dbHandler.addDevice(new Device("3UUi","Gang"));
            dbHandler.addDevice(new Device("rSfe","Studiezone"));
            dbHandler.addDevice(new Device("koHK","U171"));
            dbHandler.addDevice(new Device("CffO","U170"));
            dbHandler.addDevice(new Device("NEcd","U177"));
            dbHandler.addDevice(new Device("dHSx","U176"));
            dbHandler.addDevice(new Device("n1hZ","Studiezone"));
            dbHandler.addDevice(new Device("0bNm","Studiezone"));
            dbHandler.addDevice(new Device("GZGz","U172"));
            dbHandler.addDevice(new Device("76bB","U173"));
            dbHandler.addDevice(new Device("1AGP","U174"));
            dbHandler.addDevice(new Device("RN94","U175"));
            dbHandler.addDevice(new Device("6qXL","Studiezone"));
            dbHandler.addDevice(new Device("m6ZW","Trappe"));
            dbHandler.addDevice(new Device("Eh6f","Atrie"));
            dbHandler.addDevice(new Device("23ip","Gang"));
            dbHandler.addDevice(new Device("oPSB","U182"));
            dbHandler.addDevice(new Device("bR6t","U183"));
            dbHandler.addDevice(new Device("FNWl","Atrie"));
            dbHandler.addDevice(new Device("zhSV","U181"));
            dbHandler.addDevice(new Device("64AZ","U180"));
        }
    }


    void handleNewLocation(Location l){
        mCallbackText.setText("Using GPS, your location is : LAT(" + l.getLatitude() + ") LNG(" + l.getLongitude() +") ");
    }

    void handleNewDeviceList(ArrayList<Device> devices){
        ListView lv = (ListView) findViewById(R.id.bluetoothList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1
        );
        lv.setAdapter(adapter);
        adapter.clear();
        for(Device d : devices) {
            adapter.add(d.getRoomNumber() + " ("+ d.getDeviceName() +")" + " - RSSI: " + d.getSignalStrength() +" AGE : " + ((int) (System.currentTimeMillis() / 1000L) - d.getBirth()));
        }
        if(devices.get(0) != null) {
            mCallbackText.setText("Using Bluetooth, you are close to room : " + devices.get(0).getRoomNumber());
        }
    }

    /**
     * Makes sure that all of our permission requests have been handled
     * LOCATION HANDLERS ARE FOR API-24 AND UP
     */
    protected boolean handlePermissions() {
        boolean consent = false;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE
                    },
                    1);
            consent = false;
        } else {
            consent = true;
        }
        return consent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        ArrayList<String> allowed = new ArrayList<String>();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] <= 0) {
                allowed.add(permissions[i]);
            }
        }
        //Call out location service starter if all is good thank fuck for api-24 n' up
        if (allowed.contains(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                allowed.contains(Manifest.permission.ACCESS_FINE_LOCATION) &&
                allowed.contains(Manifest.permission.BLUETOOTH) &&
                allowed.contains(Manifest.permission.BLUETOOTH_ADMIN)&&
                allowed.contains(Manifest.permission.INTERNET) &&
                allowed.contains(Manifest.permission.ACCESS_NETWORK_STATE)) {
            startKontaktService();
            startGPSService();
        }
    }

    private void startGPSService() {
        bindService(new Intent(this, GPSService.class), gpsServiceConnection, Context.BIND_AUTO_CREATE);
    }
    private void startKontaktService() {
        bindService(new Intent(this, KontaktService.class), kontaktServiceConnection, Context.BIND_AUTO_CREATE);
    }



    private Messenger gpsMessageService;
    private Messenger kontaktMessageService;
    final Messenger mMessenger = new Messenger(new handler());

    class handler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GPSService.MSG_NEW_LOCATION :
                    handleNewLocation((Location) msg.obj);
                    break;
                case GPSService.MSG_SET_VALUE :
                    break;
                case KontaktService.MSG_NEW_BLUETOOTH_LIST :
                    handleNewDeviceList((ArrayList<Device>) msg.obj);
            }
            super.handleMessage(msg);
        }
    };

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection gpsServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GPSService.LocalBinder binder = (GPSService.LocalBinder) service;
            gpsService = binder.getService();
            gpsMessageService = new Messenger(binder.getMessageBinder());
            isGPSBound = true;

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                //REGISTER MEE!
                Message msg = Message.obtain(null,
                        GPSService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                gpsMessageService.send(msg);
            } catch (RemoteException e) { Log.e(TAG, e.toString()); }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isGPSBound = false;
        }
    };

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection kontaktServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            KontaktService.LocalBinder binder = (KontaktService.LocalBinder) service;
            kontaktService = binder.getService();
            kontaktMessageService = new Messenger(binder.getMessageBinder());
            isKontaktBound = true;

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                //REGISTER MEE!
                Message msg = Message.obtain(null,
                        GPSService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                kontaktMessageService.send(msg);
            } catch (RemoteException e) { Log.e(TAG, e.toString()); }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isKontaktBound = false;
        }
    };
}
