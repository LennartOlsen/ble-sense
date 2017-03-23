package net.lennartolsen.blescanner.messenger;

import android.os.Message;

/**
 * Created by lennartolsen on 21/03/2017.
 */

public interface IMessageReceiver {
    void ReceiveMessage(Message message);
}
