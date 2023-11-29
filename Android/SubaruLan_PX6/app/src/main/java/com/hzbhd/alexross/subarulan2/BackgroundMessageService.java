/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.IBinder;

import android.util.Log;

import androidx.annotation.Nullable;

public class BackgroundMessageService extends Service {
    private static final String TAG = BackgroundMessageService.class.getSimpleName();
    BroadcastReceiver mReceiver ;

    @Override
    public void onCreate() {
        super.onCreate();
        // REGISTER RECEIVER THAT HANDLES SCREEN ON AND SCREEN OFF LOGIC
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
          mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Log.i("EXIT", "ondestroy!");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class ScreenReceiver extends BroadcastReceiver {

        private boolean screenOff;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,  intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                screenOff = true;
                Intent broadcastIntent = new Intent("com.nwd.ACTION_TURNOFF_DEVICE_POWER");
                sendBroadcast(broadcastIntent);

            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                screenOff = false;
                Intent broadcastIntent = new Intent("com.nwd.ACTION_TURNON_DEVICE_POWER");
                sendBroadcast(broadcastIntent);
            }
        }
    }
}