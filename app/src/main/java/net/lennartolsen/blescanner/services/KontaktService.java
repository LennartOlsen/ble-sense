package net.lennartolsen.blescanner.services;

import android.app.Service;
import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
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

import net.lennartolsen.blescanner.Device;
import net.lennartolsen.blescanner.MyDBHandler;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class KontaktService extends Service {
    private final static String TAG = "KontaktService";

    private final static int DEVICE_LIFE_TIME = 90; /* Seconds */

    private ProximityManager proximityManager;

    private boolean firstRun = true;

    private ArrayList<Device> deviceList = new ArrayList<>();

    MyDBHandler dbHandler;

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
                    return;
                }
                proximityManager.startScanning();
            }
        });
    }
    private void stopScanning() {
        //Stop scanning if scanning is in progress
        if (proximityManager.isScanning()) {
            proximityManager.stopScanning();
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
                String uuid = ibeacon.getUniqueId();
                if(uuid != null){
                    String roomNo = dbHandler.getRoomNumberOfDevice(uuid);
                    if(roomNo != null){
                        Device d = new Device(uuid, roomNo);
                        d.setSignalStrength(ibeacon.getRssi());
                        d.setBirth((int) (System.currentTimeMillis() / 1000L));
                        addDeviceToList(d);
                        cleanUpList();

                        new Shipper().execute();
                    }
                }

            }
        };
    }

    protected void addDeviceToList(Device d){
        /* Overwrite the position if the id is already there **/
        for (int i = deviceList.size() - 1; i >= 0; i--) {
            Device currentDevice = deviceList.get(i);
            if(currentDevice.getDeviceName().equals(d.getDeviceName())){
                deviceList.remove(i);
            }
        }
        for (int i = deviceList.size() - 1; i >= 0; i--) {
            Device currentDevice = deviceList.get(i);
            /** List is empty **/
            if(currentDevice == null) {
                deviceList.add(i, d);
                return;
            }
            /* Check if the signal strength is between this and the next one*/
            if(d.getSignalStrength() <= currentDevice.getSignalStrength()){
                /** If next is not there we just add it at that position */
                if(i+1 > deviceList.size() - 1){
                    Log.e(TAG, "Next is Null - adding at " + (i+1));
                    deviceList.add(i+1, d);
                    return;
                }
                /** If in between we add it **/
                if(d.getSignalStrength() >= deviceList.get(i+1).getSignalStrength()){
                    Log.e(TAG, "Is inbetween adding at " + (i+1));
                    deviceList.add(i+1, d);
                    return;
                }
                /* Else do the next one */
            }
        }
        /** If never added we assume that the adding device has the greatest signal strength **/
        deviceList.add(0, d);
    }
    /** Removes every device that is older than DEVICE_LIFE_TIME **/
    private void cleanUpList(){
        for (int i = deviceList.size() - 1; i >= 0; i--) {
            Device currentDevice = deviceList.get(i);
            if((int) (System.currentTimeMillis() / 1000L) - currentDevice.getBirth() > DEVICE_LIFE_TIME){
                deviceList.remove(i);
            }
        }
    }

    private class Shipper extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... aVoid) {
            Log.e(TAG, "Doing in the background");
            try {
                if(!firstRun){
                    Thread.sleep(500);
                }
                firstRun = false;
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Message msg = Message.obtain(null, MSG_NEW_BLUETOOTH_LIST);
            msg.obj = KontaktService.this.deviceList;
            Log.e(TAG, "Sending shiiiit");
            try {
                KontaktService.this.mMessenger.send(msg);
            } catch (RemoteException e){
                Log.e(TAG, e.toString());
            }
        }
    }

    @Override
    public void onCreate() {
        /** Do setup **/

        //Initialize and configure proximity manager
        setupProximityManager();

        //Setup iBeacon and Eddystone regions/namespaces (spaces)
        setupSpaces();

        startScanning();

        dbHandler = new MyDBHandler(this, null, 1);
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
        public IBinder getMessageBinder() {
            return mMessenger.getBinder();
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
     * Command to send a new location
     */
    public static final int MSG_NEW_BLUETOOTH_LIST = 21;

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
                case MSG_NEW_BLUETOOTH_LIST :
                    shipMessageToClients(Message.obtain(null,
                            MSG_NEW_BLUETOOTH_LIST, msg.obj));
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
