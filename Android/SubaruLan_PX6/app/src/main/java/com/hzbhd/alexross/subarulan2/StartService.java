/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class StartService extends Service {
    private static final String TAG = BackgroundUSBService.class.getSimpleName();

    public StartService() {
        Log.i(TAG,"StartService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"IBind");
        // TODO: Return the communication channel to the service.
       // throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }
}
