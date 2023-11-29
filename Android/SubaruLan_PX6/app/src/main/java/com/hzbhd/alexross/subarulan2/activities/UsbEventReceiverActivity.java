/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

public class UsbEventReceiverActivity extends Activity {
    private static final String TAG = UsbEventReceiverActivity.class.getSimpleName();

    static UsbManager _usbManager;
    static Activity thisActivity;
    public static final String ACTION_USB_DEVICE_ATTACHED = "com.example.ACTION_USB_DEVICE_ATTACHED";

    private static final String ACTION_USB_PERMISSION = "com.hzbhd.alexross.subarulan2.USB_PERMISSION";
    private static final int requestusbcode = 156;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        thisActivity =this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        Log.i(TAG, "UsbEventReceiverActivity:" + intent.getAction());
        if (intent != null) {
            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                Parcelable usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                // Create a new intent and put the usb device in as an extra
                Intent broadcastIntent = new Intent(ACTION_USB_DEVICE_ATTACHED);
                broadcastIntent.putExtra(UsbManager.EXTRA_DEVICE, usbDevice);

                // Broadcast this event so we can receive it
                sendBroadcast(broadcastIntent);
            }
            else   if (intent.getAction().equals("com.hzbhd.alexross.subarulan.check")) {
                Parcelable usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                Check((UsbDevice) usbDevice);
            }
        }
        // Close the activity
        finish();
    }

    private void setUsbIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
    }
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent incomeIntent) {
            Log.i(TAG, "usbReceiver:" + incomeIntent.getAction());
            if (incomeIntent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = incomeIntent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) // User accepted our USB connection. Try to open the device as a serial port
                    Log.i(TAG, "usbReceiver:" + incomeIntent.getAction());
                }
            }

    };


    public  void Check(UsbDevice device) {
        RequestUserPermission(device);
    }

    private  void RequestUserPermission(UsbDevice device) {
            RequestUSBPermissionAccess(); // Displaying popup but when taps on allow sametime not able to UsbManager.HasPermission       orUsbManager.OpenDevice

    }

   static void RequestUSBPermissionAccess() {
        if (ContextCompat.checkSelfPermission( (Activity)thisActivity, "android.permission.USB_PERMISSION"  )!= PackageManager.PERMISSION_GRANTED)
        {
            Log.i(TAG, "RequestUSBPermissionAccess:requestPermissions");
            ActivityCompat.requestPermissions( (Activity)thisActivity, new String[]{"android.permission.USB_PERMISSION"},requestusbcode);
        }
        else
        {
            Log.i(TAG, "RequestUSBPermissionAccess:garanted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult" + grantResults.length+"--"+permissions.length);
    }
}