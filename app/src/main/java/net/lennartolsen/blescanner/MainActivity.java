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
import android.widget.Button;
import android.widget.TextView;

import com.kontakt.sdk.android.common.KontaktSDK;

import net.lennartolsen.blescanner.services.BTService;
import net.lennartolsen.blescanner.services.GPSService;
import net.lennartolsen.blescanner.services.KontaktService;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    TextView mCallbackText;
    Button debugButton;

    GPSService gpsService;
    KontaktService kontaktService;

    boolean isGPSBound = false;
    boolean isKontaktBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KontaktSDK.initialize(this);

        mCallbackText = (TextView) findViewById(R.id.debugView);
        debugButton = (Button) findViewById(R.id.button);

        debugButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(View v) {
                mCallbackText.setText("I do nuttin!");
            }
        });
        if( handlePermissions() ) {
            if(this.kontaktService == null){startKontaktService();}
            if(this.gpsService == null){startGPSService();}
        }
    }

    void handleNewLocation(Location l){
        mCallbackText.setText("Received from service: " + l.toString());
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
            Log.e("World", "UNGRANTED");
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
            Log.e("World", "GRANTED");
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
        Log.e("WORLD", "try to bind");
        bindService(new Intent(this, KontaktService.class), kontaktServiceConnection, Context.BIND_AUTO_CREATE);
    }



    private Messenger gpsMessageService;
    final Messenger mMessenger = new Messenger(new handler());

    class handler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GPSService.MSG_NEW_LOCATION :
                    Log.e("WORLD", "NEW LOCATION " + msg.obj.toString());
                    handleNewLocation((Location) msg.obj);
                    break;
                case GPSService.MSG_SET_VALUE :
                    Log.e("WORLD", "SET VALUE" + msg.getData().toString());
                    break;
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
            } catch (RemoteException e) { Log.e("WORLD", e.toString()); }
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
            Log.e("WORLD", "KONTAKTSERVICE SERVICE CONNECTED");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            KontaktService.LocalBinder binder = (KontaktService.LocalBinder) service;
            kontaktService = binder.getService();
            isKontaktBound = true;

            // We want to monitor the service for as long as we are
            // connected to it.
            /*try {
                //REGISTER MEE!
                Message msg = Message.obtain(null,
                        GPSService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                gpsMessageService.send(msg);
            } catch (RemoteException e) { Log.e("WORLD", e.toString()); }*/
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isKontaktBound = false;
        }
    };
}
