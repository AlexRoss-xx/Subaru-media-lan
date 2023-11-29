/*
 *  2023.
 * Alexey Rasskazov
 */

package com.physicaloid.lib.framework;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import com.physicaloid.lib.UsbVidList;
import com.physicaloid.lib.usb.UsbAccessor;
import com.physicaloid.lib.usb.driver.uart.UartCdcAcm;
import com.physicaloid.lib.usb.driver.uart.UartCp210x;
import com.physicaloid.lib.usb.driver.uart.UartFtdi;

public class AutoCommunicator {
    @SuppressWarnings("unused")
    private static final String TAG = AutoCommunicator.class.getSimpleName();

    public AutoCommunicator() {
    }

    public SerialCommunicator getSerialCommunicator(Context context) {
        UsbAccessor usbAccess = UsbAccessor.INSTANCE;
        usbAccess.init(context);

        for(UsbDevice device : usbAccess.manager().getDeviceList().values()) {
            int vid = device.getVendorId();
            for(UsbVidList usbVid : UsbVidList.values()) {
                if(vid == usbVid.getVid()) {
                    if(vid == UsbVidList.FTDI.getVid()) {
                        return new UartFtdi(context);
                    } else if(vid == UsbVidList.CP210X.getVid()) {
                        return new UartCp210x(context);
                    }
                }
            }
        }

        return new UartCdcAcm(context);
    }
}
