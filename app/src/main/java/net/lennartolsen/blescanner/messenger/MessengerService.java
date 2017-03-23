package net.lennartolsen.blescanner.messenger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import net.lennartolsen.blescanner.R;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class MessengerService extends Service {

    NotificationManager nManager;
    /** Keeps track of registered clients **/
    ArrayList<Messenger> mClients = new ArrayList<>();

    int mValue = 0;

    /**
     * CONSTANTS FOR HANDLING MESSAGES
     */

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

    public MessengerService() {
    }

    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_VALUE:
                    mValue = msg.arg1;
                    for (int i=mClients.size()-1; i>=0; i--) {
                        try {
                            mClients.get(i).send(Message.obtain(null,
                                    MSG_SET_VALUE, mValue, 0));
                        } catch (RemoteException e) {
                            // The client is dead.  Remove it from the list;
                            // we are going through the list from back to front
                            // so this is safe to do inside the loop.
                            mClients.remove(i);
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate(){
        nManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        //showNotification();
    }

    @Override
    public void onDestroy(){
        nManager.cancel(R.string.remote_service_started);

        Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.e(TAG, "Bound to the messenger service");
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.e(TAG, "Called on start command");
        return START_STICKY;
    }

/**    private void showNotification(){
        CharSequence text = getText(R.string.remote_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MessengerService.class), 0);

        Notification notification = new Notification.Builder(this).
                setSmallIcon(R.drawable.stat_sample).
                setTicker(text).
                setWhen(System.currentTimeMillis()).
                setContentTitle(getText(R.string.local_service_label)).
                setContentText(text).
                setContentIntent(contentIntent).build();

        nManager.notify(R.string.remote_service_started, notification);
    }**/

}
