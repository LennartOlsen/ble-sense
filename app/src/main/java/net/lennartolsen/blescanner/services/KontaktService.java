package net.lennartolsen.blescanner.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.device.BeaconRegion;
import com.kontakt.sdk.android.ble.device.EddystoneNamespace;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.SpaceListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class KontaktService extends Service {
    private final static String TAG = "KontaktService";

    private ProximityManager proximityManager;

    public KontaktService() {
    }

    private void setupProximityManager() {
        proximityManager = ProximityManagerFactory.create(this);

        //Configure proximity manager basic options
        proximityManager.configuration()
                //Using ranging for continuous scanning or MONITORING for scanning with intervals
                .scanPeriod(ScanPeriod.RANGING)
                //Using BALANCED for best performance/battery ratio
                .scanMode(ScanMode.BALANCED)
                //OnDeviceUpdate callback will be received with 5 seconds interval
                .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(5));

        //Setting up iBeacon and Eddystone spaces listeners
        proximityManager.setSpaceListener(createSpaceListener());

        //Setting up empty no-operation iBeacon and Eddystone listeners as in this example we are not interested in particular devices events.
        //Those listeners can't be null though, because ProximityManager must be aware that it needs to look for both iBeacons and Eddystones
        proximityManager.setIBeaconListener(createIBeaconListener());
        proximityManager.setEddystoneListener(new SimpleEddystoneListener() {
        });
    }
    private void setupSpaces() {
        //Setting up single iBeacon region. Put your own desired values here.
        IBeaconRegion region = new BeaconRegion.Builder().identifier("My Region") //Region identifier is mandatory.
                .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) //Default Kontakt.io proximity.
                //Optional major and minor values
                //.major(1)
                //.minor(1)
                .build();
        proximityManager.spaces().iBeaconRegion(region);

        //Setting up single Eddystone namespace. Put your own desired values here.
        IEddystoneNamespace namespace = new EddystoneNamespace.Builder().identifier("My Namespace") //Namespace identifier is mandatory.
                .namespace("f7826da64fa24e988024") //Default Kontakt.io namespace.
                //Optional instance id value
                //.instanceId("instanceId")
                .build();
        proximityManager.spaces().eddystoneNamespace(namespace);
    }
    private void startScanning() {
        //Connect to scanning service and start scanning when ready
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                //Check if proximity manager is already scanning
                if (proximityManager.isScanning()) {
                    Log.e(TAG, "Already Scanning");
                    return;
                }
                proximityManager.startScanning();
                Log.e(TAG, "Scannning");
            }
        });
    }
    private void stopScanning() {
        //Stop scanning if scanning is in progress
        if (proximityManager.isScanning()) {
            proximityManager.stopScanning();
            Log.e(TAG, "Scanning stopped");
        }
    }
    private SpaceListener createSpaceListener() {
        return new SpaceListener() {
            @Override
            public void onRegionEntered(IBeaconRegion region) {
                Log.i(TAG, "New Region entered: " + region.getIdentifier());
            }

            @Override
            public void onRegionAbandoned(IBeaconRegion region) {
                Log.e(TAG, "Region abandoned " + region.getIdentifier());
            }

            @Override
            public void onNamespaceEntered(IEddystoneNamespace namespace) {
                Log.i(TAG, "New Namespace entered: " + namespace.getIdentifier());
            }

            @Override
            public void onNamespaceAbandoned(IEddystoneNamespace namespace) {
                Log.i(TAG, "Namespace abandoned: " + namespace.getIdentifier());
            }
        };
    }
    protected IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
                Log.i(TAG, "IBeacon discovered: " + ibeacon.toString());
            }
        };
    }

    @Override
    public void onCreate() {
        /** Do setup **/

        //Initialize and configure proximity manager
        setupProximityManager();

        //Setup iBeacon and Eddystone regions/namespaces (spaces)
        setupSpaces();

        startScanning();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "Bound");
        return binder;
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public KontaktService getService() {
            return KontaktService.this;
        }
    }

    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    public static final int MSG_REGISTER_CLIENT = 1;

    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    public static final int MSG_UNREGISTER_CLIENT = 2;

    /**
     * Command to service to set a new value.  This can be sent to the
     * service to supply a new value, and will be sent by the service to
     * any registered clients with the new value.
     */
    public static final int MSG_SET_VALUE = 3;

    /**
     * Command to send a new location
     */
    public static final int MSG_NEW_LOCATION = 4;

    int mValue = 0;
    ArrayList<Messenger> mClients = new ArrayList<>();

    class handler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_VALUE:
                    mValue = msg.arg1;
                    shipMessageToClients(Message.obtain(null,
                            MSG_SET_VALUE, mValue, 0));
                    break;
                case MSG_NEW_LOCATION :
                    shipMessageToClients(Message.obtain(null,
                            MSG_NEW_LOCATION, msg.obj));
                default:
                    super.handleMessage(msg);
            }
        }
    };

    /**
     * Sends message to all registered clients (removes them on errors)
     * @param msg the message to send
     */
    protected void shipMessageToClients(Message msg){
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                mClients.get(i).send(msg);
            } catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new KontaktService.handler());
}
