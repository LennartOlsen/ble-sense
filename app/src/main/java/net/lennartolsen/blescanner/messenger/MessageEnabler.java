package net.lennartolsen.blescanner.messenger;

import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import net.lennartolsen.blescanner.R;

import java.util.Date;
import java.util.concurrent.Callable;

import static net.lennartolsen.blescanner.messenger.MessengerService.MSG_SET_VALUE;

/**
 * Created by lennartolsen on 21/03/2017.
 */

public class MessageEnabler {

    private final String TAG = "MesseageEnabler";

    private boolean isMessengerBound = false;

    /**
     * I belive this would mock an actual service or activity invoking this method
     **/
    private Context ctx;

    protected IMessageReceiver onReceive;

    Handler.Callback cb;

    public MessageEnabler(Context ctx, IMessageReceiver receiver) {
        this.ctx = ctx;
        doBindService();
    }

    /**
     * THIS HAPPENS WHEN WE RECEIVE A MESSAGE
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_VALUE:
                    Log.d(TAG, "Received message from messenger: " + msg.toString());
                    try {
                        MessageEnabler.this.onReceive.ReceiveMessage(msg);
                    } catch (Exception e){
                        Log.e(TAG, "Could not call onReceive" + e.toString());
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     *
     * Messsenger Service specific
     *
     **/
    Messenger mService = null;
    private ServiceConnection messengerServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            mService = new Messenger(service);

            try {
                // Register us!
                Message msg = Message.obtain(null,
                        MessengerService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {}

            // As part of the sample, tell the user what happened.
            Toast.makeText(ctx, R.string.remote_service_connected,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {

            mService = null;

            Toast.makeText(ctx, R.string.remote_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    public boolean sendMessage(int message){
        try {
            Message msg = Message.obtain(null,
                    MessengerService.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mService.send(msg);

            // Give it some value as an example.
            int i = (int) (new Date().getTime()/1000);
            msg = Message.obtain(null,
                    MSG_SET_VALUE, message, 0);

            mService.send(msg);
        } catch (Exception e){
            Log.e(TAG, e.toString());
            return false;
        }
        return true;
    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        // Intent intent = new Intent(this, MessengerService.class)
        Log.e(TAG, "Is Bound = " + isMessengerBound);
        this.ctx.bindService(new Intent(this.ctx, MessageEnabler.class), messengerServiceConnection, Context.BIND_AUTO_CREATE);
        isMessengerBound = true;
        Log.d(TAG, "Bound");
    }

    void doUnbindService() {
        if (isMessengerBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            MessengerService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            this.ctx.unbindService(messengerServiceConnection);
            isMessengerBound = false;
            Log.d(TAG, "Unbound");
        }
    }

}
