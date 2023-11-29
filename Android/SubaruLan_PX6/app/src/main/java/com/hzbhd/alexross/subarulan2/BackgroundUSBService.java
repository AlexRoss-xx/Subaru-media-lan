/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;


import android.hardware.display.DisplayManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;


import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.utils.log.JLog;
import com.hzbhd.midware.constant.channelManagerDef;
import com.hzbhd.midware.misc.MiscService;
import com.hzbhd.midware.misc.PowerOffService;
import com.hzbhd.midware.service.RunService;
import com.hzbhd.midwareproxy.bluetooth.BluetoothHfpCallbackListener;
import com.hzbhd.midwareproxy.bluetooth.BluetoothServiceCallbackListener;
import com.hzbhd.midwareproxy.bluetooth.BluetoothServiceManager;
import com.hzbhd.midwareproxy.misc.MiscOperation;
import com.hzbhd.midwareproxy.sourceswitch.SourceSwitchManager;

import com.hzbhd.alexross.subarulan2.models.State;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.nwd.kernel.source.DeviceState;
import com.nwd.kernel.source.SettingTableKey;
import com.nwd.kernel.utils.KernelUtils;
import com.physicaloid.lib.Boards;
import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.UsbVidList;
import com.physicaloid.lib.programmer.avr.UploadErrors;
import com.physicaloid.lib.usb.UsbAccessor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import dalvik.system.PathClassLoader;
import nfore.android.bt.servicemanager.HfpErrorInfo;
import nfore.android.bt.servicemanager.HfpPhoneNumberInfo;
import nfore.android.bt.servicemanager.HfpStatusInfo;

import com.hzbhd.midwareproxy.bluetooth.BluetoothHfpCallbackListener;

public class BackgroundUSBService extends IntentService {

    private static final JLog LOG = new JLog("MusicBtBroadcastReceiver", false, JLog.TYPE_DEBUG);

    private static final Object LOCK = new Object();

    private static final String TAG = BackgroundUSBService.class.getSimpleName();

    public static final String ACTION_START = "com.hzbhd.alexross.subarulan2.action.START";
    public static final String ACTION_STOP = "com.hzbhd.alexross.subarulan2.action.STOP";
    public static final String ACTION_RECIVE = "com.hzbhd.alexross.subarulan2.action.RECIVE";
    public static final String ACTION = "com.hzbhd.alexross.subarulan2.action.ACTION";


    public static final String MCU_CONNECTED = "com.hzbhd.alexross.subarulan2.MCU.connected";
    public static final String MCU_DISCONNECTED = "com.hzbhd.alexross.subarulan2.mcu.disconnected";
    public static final String MCU_UPDATING = "com.hzbhd.alexross.subarulan2.mcu.updating";
    public static final String MCU_UPDATED = "com.hzbhd.alexross.subarulan2.mcu.updated";

    private static final String ACTION_USB_PERMISSION = "com.hzbhd.alexross.subarulan2.USB_PERMISSION";
    private static final int requestusbcode = 156;
    public static final String ACTION_USB_READY = "com.hzbhd.alexross.subarulan2.USB_DEVICE_READY";
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_NOT_SUPPORTED = "com.hzbhd.alexross.subarulan2.NOT_SUPPORTED_DEVICE";



    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_SEND_MCU_COMMAND = "com.hzbhd.alexross.subarulan2.action.SEND_MCU_COMMAND";
    public static final String EXTRA_MCU_COMMAND_PARAM = "com.hzbhd.alexross.subarulan2.extra.MCU_COMMAND_PARAM";

    public static final String ACTION_SEND_CAN_COMMAND = "com.hzbhd.alexross.subarulan2.action.SEND_CAN_COMMAND";
    public static final String ACTION_RECONNECT_COMMAND = "com.hzbhd.alexross.subarulan2.action.SEND_RECONNECT_COMMAND";

    public static final String ACTION_STATEMODE_COMMAND = "com.hzbhd.alexross.subarulan2.action.STATEMODE";

    UsbManager _usbManager;
    UsbDevice _device;

    static Context DeviceUARTContext;

    public static boolean isRunning = false;


    Physicaloid mPhysicaloid;
    UsbAccessor usbAccess;

    D2xxManager ftdid2xx;
    FT_Device ftDev = null;
    boolean sending = false;
    boolean uart_configured = true;
    int openIndex = 0;
    int currentIndex = -1;

    private static final int MAX_READBUF_SIZE = 256;
    public static final int READ_LENGTH = 8192;

    public int readcount = 0;
    public int iavailable = 0;
    byte[] readData;
    char[] readDataToText;
    public boolean readThreadGoing = false;
    public readThread read_thread;

    int baudRate = 2000000; /*baud rate*/
    byte stopBit = 1; /*1:1stop bits, 2:2 stop bits*/
    byte dataBit = 8; /*8:8bit, 7: 7bit*/
    byte parity = 0;  /* 0: none, 1: odd, 2: even, 3: mark, 4: space*/
    short flowControl = D2xxManager.FT_FLOW_XON_XOFF;// D2xxManager.FT_FLOW_DTR_DSR;    /*0:none, 1: flow control(CTS,RTS)*/

    private static boolean stopService;
    private static boolean serviceRuning;
    private static boolean isBtMute = false;
    private static boolean registered = false;
    private Handler mHandler;

    private State stateMode = State.UNKNOWN;

    private int muteIndex = 0;
    AtomicInteger atomicMute = new AtomicInteger(0);
    private MediaPlayer mp;
    private AudioManager am;


    private String currentPlayingApp = "";
    private String startingApp = "";
    private boolean granted = false;

    AudioManager audioManager;

    BluetoothServiceManager bt;
    private MiscOperation msc;

    /**
     * BUS message
     */
    public static class MessageEvent {
        public MessageEvent(String message) {
            this.message = message;
        }

        String message;
    }


    public BackgroundUSBService() {
        super("BackgroundUSBService");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        DeviceUARTContext = this;
        isRunning = true;
        MyApplication.repository.clearFuelConsumption();
//android.os.Debug.waitForDebugger();
        am = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        // am.setStreamVolume(AudioManager.STREAM_RING,am.getStreamMaxVolume(AudioManager.STREAM_RING),AudioManager.FLAG_SHOW_UI);
        mp = new MediaPlayer();
        AssetFileDescriptor afd = this.getResources().openRawResourceFd(R.raw.castor);
        if (afd != null) {
            try {
                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mp.setAudioStreamType(AudioManager.STREAM_RING);
                mp.setLooping(false);
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Toast.makeText(DeviceUARTContext, message.obj.toString(), Toast.LENGTH_LONG).show();
            }
        };

        //EventBus.getDefault().register(this);

        readData = new byte[MAX_READBUF_SIZE];
        readDataToText = new char[READ_LENGTH];

        try {
            ftdid2xx = D2xxManager.getInstance(this);
        } catch (D2xxManager.D2xxException ex) {
            ex.printStackTrace();
        }

        setUsbIntentFilter();
        _usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        usbAccess = UsbAccessor.INSTANCE;
        usbAccess.init(this);
        registerReceivers();
        mPhysicaloid = new Physicaloid(this);

        MyApplication.application.startReceive();
        findSerialPortDevice();
        registerSourceReceiver();

        bt = BluetoothServiceManager.getBluetoothManager();
        bt.registerHfpCallBackListenr(this.mHfpCallbackListener);

        System.out.println("The BluetoothServiceManager   " + bt.getCurrentSource());
    }


    BluetoothHfpCallbackListener mHfpCallbackListener = new BluetoothHfpCallbackListener() {

        @Override
        public void onHfpActiveNumberChanged(String s, String s1, String s2) {

        }

        @Override
        public void onHfpAtDownloadPhonebookStateChanged(String s, int i, int i1) {
        }

        @Override
        public void onHfpAtDownloadSMSStateChanged(String s, int i) {
        }

        @Override
        public void onHfpCallStateChanged(String s, int i, int i1) {
            System.out.println("The  onHfpCallStateChanged : " + s + " i:" + i + " i1:" + i1);
            if ((i == 180 || i == 165) && (i1 == 140 || i1 == 110 || i1 == 120 || i1 == 100)) {
                sendMuteOff(true);
                MyApplication.Companion.setIncomingCall(false);
            }
        }

        @Override
        public void onHfpErrorResponse(String s, HfpErrorInfo hfpErrorInfo) {
        }

        @Override
        public void onHfpIncomingCallNumber(String s, String s1) {
            Log.d(TAG, "onHfpIncomingCallNumber");
            isBtMute = true;
            sendMuteOn();
            MyApplication.Companion.setIncomingCall(true);
        }

        @Override
        public void onHfpMicSelectionChanged(String s, int i, int i1) {
        }

        @Override
        public void onHfpMicVolumeChanged(String s, String s1, String s2) {
        }

        @Override
        public void onHfpMuteStateChanged(String s, int i, int i1) {
        }

        @Override
        public void onHfpOutgoingCallNumber(String s, String s1) {
            Log.d(TAG, "onHfpOutgoingCallNumber");
            isBtMute = true;
            sendMuteOn();
            MyApplication.Companion.setIncomingCall(true);
        }

        @Override
        public void onHfpRemoteDeviceBatteryStatusChanged(String s, int i, int i1, int i2, int i3) {
        }

        @Override
        public void onHfpRemoteDevicePhoneNumberChanged(String s, List<HfpPhoneNumberInfo> list) {
        }

        @Override
        public void onHfpRemoteDeviceRoamStatusChanged(String s, boolean b) {
        }

        @Override
        public void onHfpRemoteDeviceServiceStatusChanged(String s, boolean b) {
        }

        @Override
        public void onHfpRemoteDeviceSignalStatusChanged(String s, int i, int i1, int i2, int i3) {
        }

        @Override
        public void onHfpScoStateChanged(String s, int i, int i1) {
        }

        @Override
        public void onHfpSpeakerVolumeChanged(String s, String s1, String s2) {

        }

        @Override
        public void onHfpStateChanged(String s, int i, int i1) {

        }

        @Override
        public void onHfpVoiceControlStateChanged(String s, boolean b) {

        }

        @Override
        public void retHfpRemoteDeviceInfo(String s, HfpStatusInfo hfpStatusInfo) {

        }

        @Override
        public void retHfpRemoteDeviceNetworkOperator(String s, String s1, int i, String s2) {

        }

        @Override
        public void retHfpRemoteDeviceSubscriberNumber(String s, String s1, int i, int i1) {

        }

        @Override
        public void retPbapQueryName(String s, String s1, String s2, boolean b) {

        }
    };


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate();
        Intent intent = new Intent(this, BackgroundUSBService.class);
        intent.setAction(ACTION_START);
        if (!stopService)
            this.startService(intent);
    }

    private void registerSourceReceiver() {
        IntentFilter intentFilter = new IntentFilter(ServiceConstants.SOURCE_UI_FINISH_ACTION);
        intentFilter.addAction(ServiceConstants.SOURCE_REAL_CHANGE_ACTION);
        registerReceiver(this.mSourceReceiver, intentFilter);
    }

    private BroadcastReceiver mSourceReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Removed duplicated region for block: B:23:0x00ad A[Catch:{ Exception -> 0x00bf }] */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x00b6 A[Catch:{ Exception -> 0x00bf }] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(android.content.Context r4, android.content.Intent r5) {
            Log.d(TAG, r5.getAction());
        }
    };


    public boolean IsDeviceSleep() {
        try {
            msc = MiscOperation.getMcuOperationInstant();
            msc.connectService();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Log.d(TAG, "isPowerOffStatus:" + String.valueOf(MiscOperation.isPowerOffStatus()));
        return MiscOperation.isPowerOffStatus();
    }


    private void registerReceivers() {
        final IntentFilter myFilter = new IntentFilter(ACTION_SEND_CAN_COMMAND);
        registerReceiver(mReceiver, myFilter);


        final IntentFilter actionStateFilter = new IntentFilter(ACTION_SEND_MCU_COMMAND);
        registerReceiver(mcuActionReceiver, actionStateFilter);

        final IntentFilter actionStopFilter = new IntentFilter(ACTION_STOP);
        registerReceiver(stopServiceActionReceiver, actionStopFilter);

        final IntentFilter actionStartFilter = new IntentFilter(ACTION_START);
        registerReceiver(startServiceActionReceiver, actionStartFilter);

        final IntentFilter stateModeFilter = new IntentFilter(ACTION_STATEMODE_COMMAND);
        registerReceiver(stateModeReceiver, stateModeFilter);


        final IntentFilter changeSourceModeFilter2 = new IntentFilter("com.nwd.action.ACTION_KEY_VALUE");
        registerReceiver(scandReceiver, changeSourceModeFilter2);
        final IntentFilter changeSourceModeFilter3 = new IntentFilter("android.intent.action.MAIN");
        registerReceiver(scandReceiver, changeSourceModeFilter3);

        final IntentFilter changeSourceModeFilter4 = new IntentFilter("com.nwd.action.ACTION_APP_IN_OUT");
        registerReceiver(scandReceiver, changeSourceModeFilter4);


        final IntentFilter changeSourceModeFilter7 = new IntentFilter("com.nwd.action.ACTION_NO_SOURCE_DEVICE_CHANGE");
        registerReceiver(scandReceiver, changeSourceModeFilter7);
        final IntentFilter changeSourceModeFilter8 = new IntentFilter("com.nwd.action.ACTION_STOP_APP");
        changeSourceModeFilter8.addAction("com.hzbhd.android.MCU_KEY_IND_ACTION");
        registerReceiver(scandReceiver, changeSourceModeFilter8);

        final IntentFilter changeSourceModeFilter9 = new IntentFilter("android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION");
        changeSourceModeFilter9.addAction("android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION");
        registerReceiver(scandReceiver, changeSourceModeFilter9);

        final IntentFilter changeSourceModeFilter91 = new IntentFilter("com.hzbhd.action.sourcerealchange");
        changeSourceModeFilter91.addAction("playstate.change.action");

        changeSourceModeFilter91.addAction("bt.call.number");
        changeSourceModeFilter91.addAction("com.hzbhd.bluetooth.call.action");
        changeSourceModeFilter91.addAction("APP_REQUEST_START_ACTION");
        changeSourceModeFilter91.addAction("com.hzbhd.action.reqsourcechange");
        changeSourceModeFilter91.addAction("android.intent.action.CALL");
        changeSourceModeFilter91.addAction("com.hzbhd.intent.action.music");
        changeSourceModeFilter91.addAction("com.broadcast.switchOriginal");

        changeSourceModeFilter91.addAction("");

        registerReceiver(scandReceiver, changeSourceModeFilter91);

        //PX6  STM32
        final IntentFilter naviModeFilter4 = new IntentFilter("android.intent.action.AUDIO_TRACK_PLAY_CHANGED");
        registerReceiver(naviSoundReceiver, naviModeFilter4);

        final IntentFilter mcuUpdateFilter = new IntentFilter("com.hzbhd.alexross.subarulan2.UPDATE_MCU");
        registerReceiver(mcuUpdateReceiver, mcuUpdateFilter);
    }

    public BroadcastReceiver mcuUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "mcuUpdateReceiver");

            if (ftDev != null && ftDev.isOpen())
                DisConnect();

            Intent mNextIntent = new Intent(MCU_UPDATING);
            DeviceUARTContext.sendBroadcast(mNextIntent);
            if (mPhysicaloid.open()) { // default 9600bps
                try {
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "mcuUpdateReceiver: Start Uploading");

                    mPhysicaloid.upload(Boards.ARDUINO_NANO_328, getResources().openRawResource(R.raw.tribeca3), mUploadCallback);
                    //****************************************************************

                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "mcuUpdateReceiver:Uploaded");

                } catch (RuntimeException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
    };

    private Physicaloid.UploadCallBack mUploadCallback = new Physicaloid.UploadCallBack() {
        @Override
        public void onPreUpload() {
            mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(DeviceUARTContext, "MCU start  updating", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onUploading(final int value) {

        }

        @Override
        public void onPostUpload(boolean success) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "MCU updated successfully");


            mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(DeviceUARTContext, "MCU updated successfully", Toast.LENGTH_LONG).show();
                }
            });

            Intent mNextIntent = new Intent(MCU_UPDATED);
            DeviceUARTContext.sendBroadcast(mNextIntent);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            Reset();
            Intent intent = new Intent(DeviceUARTContext, BackgroundUSBService.class);
            intent.setAction(ACTION_RECONNECT_COMMAND);
            DeviceUARTContext.startService(intent);
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onError(final UploadErrors err) {
            Intent mNextIntent = new Intent(MCU_UPDATED);
            DeviceUARTContext.sendBroadcast(mNextIntent);

            if (BuildConfig.DEBUG)
                Log.d(TAG, "MCU update failed: " + err.toString());

            mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(DeviceUARTContext, "MCU update failed:" + err.toString(), Toast.LENGTH_LONG).show();
                }
            });
        }
    };
    //****************************************************************


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Service received: ");
        }
    };


    private BroadcastReceiver scandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle extras = intent.getExtras();
            Log.d(TAG, intent.toString());
            if (extras != null) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    Object obj = intent.getExtras().get(key);
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, " Intent:" + intent.toString() + " key:" + key + ":" + obj.toString());
                }
            }
        }
    };

    //todo naviSoundReceiver
    private BroadcastReceiver naviSoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "naviSoundReceiver - " + intent);

            Boolean isPlay = intent.getBooleanExtra("isPlay", false);
            System.out.println("isPlay:" + isPlay);
            String processName = intent.getStringExtra("processName");
            System.out.println("processName:" + processName);
            if (isPlay) {
                if (processName.equalsIgnoreCase("ru.yandex.yandexnavi") ||
                        processName.equalsIgnoreCase("com.navitel")) {
                    sendMuteOn();
                }
            } else {
                if (processName.equalsIgnoreCase("ru.yandex.yandexnavi") ||
                        processName.equalsIgnoreCase("com.navitel")) {
                    sendMuteOff(false);
                }
            }
        }
    };


    private void setDtrRts(boolean on) {
        if (ftDev != null)
            if (on) {
                ftDev.setRts();
                ftDev.setDtr();
            } else {
                ftDev.clrDtr();
                ftDev.clrDtr();
            }
    }

    private void Reset() {

        if (ftDev == null)
            findSerialPortDevice();

        if (ftDev != null && ftDev.isOpen()) {
            synchronized (ftDev) {
                setDtrRts(false);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
                setDtrRts(true);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    //SEND MCU COMMAND
    private BroadcastReceiver mcuActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String extra_state = intent.getStringExtra(EXTRA_MCU_COMMAND_PARAM);
            if (BuildConfig.DEBUG)
                Log.d(TAG, "SEND MCU COMMAND:" + extra_state);

            if (extra_state.equalsIgnoreCase("RESET")) {

                String writeData = "X\n";
                byte[] OutData = writeData.getBytes();
                ftDev.write(OutData, writeData.length());

                readThreadGoing = false;
                registered = false;
                EventBus.getDefault().removeAllStickyEvents();

                Reset();
                //ftDev.close();

                SendStartMessage();

                if (!readThreadGoing) {
                    read_thread = new readThread(handler);
                    readThreadGoing = true;
                    read_thread.start();
                }

                SendMCUMessage();

                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(DeviceUARTContext, "Restarting MCU!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (ftDev != null)
                read_thread.toSend = extra_state + "\n";
        }
    };

    //STOP SERVICE
    private BroadcastReceiver stopServiceActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "stopServiceActionReceiver:ACTION_STOP");

            DisConnect();
            stopService = true;
        }
    };

    //START SERVICE
    private BroadcastReceiver startServiceActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "ACTION_START");
            //   ConnectUsb();
        }
    };

    private BroadcastReceiver stateModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Mode");
            stateMode = (State) intent.getSerializableExtra("mode");
            if (stateMode != ApplicationConfig.Companion.getUnmuteMode() && stateMode == State.UNKNOWN) {
                sendMuteOff(false);
            }
        }
    };


    private void sendMuteOn() {
        if (ApplicationConfig.Companion.getUnmuteMode() != stateMode && stateMode != State.UNKNOWN) {
            if (atomicMute.compareAndSet(0, 1)) {
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "mute on ");

                if (readThreadGoing)
                    read_thread.toSend = "M1\n";
            }
        }
    }

    private void sendMuteOff(boolean isBtMute) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "mute off :" + this.isBtMute);
        // if(ApplicationConfig.Companion.getConfig()!= stateMode) {
        if (isBtMute == this.isBtMute)
            if (atomicMute.compareAndSet(1, 0)) {
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "mute off ");

                if (readThreadGoing)
                    read_thread.toSend = "M0\n";

                if (this.isBtMute)
                    this.isBtMute = false;
            }
    }


    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "onDestroy");

        bt.unRegisterHfpCallBackListenr(this.mHfpCallbackListener);

        unregisterReceiver(startServiceActionReceiver);
        unregisterReceiver(mcuUpdateReceiver);
        unregisterReceiver(naviSoundReceiver);
        unregisterReceiver(mReceiver);
        unregisterReceiver(scandReceiver);

        unregisterReceiver(usbReceiver);
        unregisterReceiver(stateModeReceiver);
        unregisterReceiver(stopServiceActionReceiver);
        unregisterReceiver(mcuActionReceiver);

        unregisterReceiver(mSourceReceiver);


        try {
            Thread.sleep(15000);
            if (BuildConfig.DEBUG)
                Log.i(TAG, "onDestroy: sleep" + IsDeviceSleep());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (ftDev != null && ftDev.isOpen() && IsDeviceSleep()) {
            if (BuildConfig.DEBUG)
                Log.i(TAG, "onDestroy: Reset");
            Reset();
            MyApplication.repository.clearFuelConsumption();
        }
        if (ftDev != null && ftDev.isOpen())
            ftDev.close();

        super.onDestroy();

    }


//    @Subscribe(threadMode = ThreadMode.BACKGROUND)
//    @Keep
//    public void onMessageEvent(MessageEvent event) {
//        if (BuildConfig.DEBUG)
//            Log.i(TAG, "onMessageEvent: " + event.message);
//        // ProcessMessage(event.message);
//    }

    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "onNotice BroadcastReceiver  called");
        }
    };


    public static void StartUSBService(Context context) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "StartUSBService ..");

        if (!BackgroundUSBService.serviceRuning) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "StartUSBService 2");
            Intent intent = new Intent(context, BackgroundUSBService.class);
            intent.setAction(ACTION_START);
            context.startService(intent);
        }
    }

    public static void StopService(Context context) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "StopService");
        Intent in = new Intent("ACTION_STOP");
        context.sendBroadcast(in);
        //context.stopService(in);
    }

    public static void UpdateMCU(Context context) {
        Intent in = new Intent("com.hzbhd.alexross.subarulan2.UPDATE_MCU");
        context.sendBroadcast(in);
    }

    public static void SendMCUCommand(Context context, String command) {
        Intent in = new Intent(ACTION_SEND_MCU_COMMAND);
        in.putExtra(EXTRA_MCU_COMMAND_PARAM, command);
        // Fire the broadcast with intent packaged
        //LocalBroadcastManager.getInstance(this).sendBroadcast(in);
        context.sendBroadcast(in);
    }

    public static void SendStateCommand(Context context, State extra) {
        Intent in = new Intent(ACTION_STATEMODE_COMMAND);
        in.putExtra("mode", extra);
        context.sendBroadcast(in);
    }

    public static void SendRESETCommand(Context context) {
        Intent intent = new Intent(context, BackgroundUSBService.class);
        intent.setAction(ACTION_RECONNECT_COMMAND);
        context.startService(intent);
    }


    private void setUsbIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver, filter);
    }


    private void RequestUserPermission() {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "RequestUserPermission");

        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        _usbManager.requestPermission(_device, mPendingIntent);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent incomeIntent) {
            if (BuildConfig.DEBUG)
                Log.i(TAG, "usbReceiver:" + incomeIntent.getAction());

            if (incomeIntent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = incomeIntent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) // User accepted our USB connection. Try to open the device as a serial port
                {
                    if (BuildConfig.DEBUG)
                        Log.i(TAG, "EXTRA_PERMISSION_GRANTED");
                    ConnectUsb();
                } else // User not accepted our USB connection. Send an Intent to the Main Activity
                {
                    mHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(DeviceUARTContext, "Not accepted  USB connection", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else if (incomeIntent.getAction().equals(ACTION_USB_ATTACHED)) {
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "ACTION_USB_ATTACHED");
                UsbDevice device = (UsbDevice) incomeIntent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device.getVendorId() == 1027)
                    findSerialPortDevice();

            } else if (incomeIntent.getAction().equals(ACTION_USB_DETACHED)) {

                if (BuildConfig.DEBUG)
                    Log.i(TAG, "ACTION_USB_DETACHED");
                UsbDevice device = (UsbDevice) incomeIntent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device.getVendorId() == 1027) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(DeviceUARTContext, "USB DETACHED!:", Toast.LENGTH_LONG).show();
                        }
                    });

                    readThreadGoing = false;
                    if (ftDev != null && ftDev.isOpen()) {
                        ftDev.close();
                    }
                }
            }
        }
    };


    private void DisConnect() {
        // Toast.makeText(DeviceUARTContext, "Reconnecting", Toast.LENGTH_LONG).show();
        readThreadGoing = false;
        synchronized (ftDev) {
            ftDev.close();
        }
    }

    private void reConnect() {
        {
            readThreadGoing = false;
            synchronized (ftDev) {
                // ftDev.resetDevice(); // flush any data from the device buffers
                ftDev.close();
            }
        }

        findSerialPortDevice();
    }

    private void findSerialPortDevice() {
        int count = _usbManager.getDeviceList().size();
        if (count == 0)
            mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(DeviceUARTContext, "No USB Device", Toast.LENGTH_LONG).show();
                }
            });
        else {
            count = ftdid2xx.createDeviceInfoList(DeviceUARTContext);
            if (count > 0) {
                // final D2xxManager.FtDeviceInfoListNode deviceInfoListDetail = ftdid2xx.getDeviceInfoListDetail(0);
                // This snippet will try to open the first encountered usb device connected, excluding usb root hubs
                HashMap<String, UsbDevice> usbDevices = _usbManager.getDeviceList();

                if (!usbDevices.isEmpty()) {
                    for (UsbDevice device : usbAccess.manager().getDeviceList().values()) {
                        int vid = device.getVendorId();
                        for (UsbVidList usbVid : UsbVidList.values()) {
                            if (vid == usbVid.getVid()) {
                                if (vid == UsbVidList.FTDI.getVid()) {
                                    _device = device;
                                    if (_usbManager.hasPermission(_device))
                                        ConnectUsb();
                                    else
                                        RequestUserPermission();

                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Send R2 message
    public void SendMCUMessage() {
        registered = false;
        read_thread.datastr = "";

        read_thread.toSend = "R2\n";
        registered = true;
        if (BuildConfig.DEBUG)
            Log.i(TAG, "Sent Message MCU:R2");
    }

    // Send start messages  to MCU
    public void SendStartMessage() {

        if (ftDev.isOpen() == false) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "SendMessage: device not open");
            return;
        }
        //  Latency
        //  ftDev.setLatencyTimer((byte) 16);  // 16
        try {
            if (BuildConfig.DEBUG)
                Log.i(TAG, "Sending Message R1");

            String writeData = "R1\n";
            byte[] OutData = writeData.getBytes();
            synchronized (ftDev) {
                ftDev.write(OutData, writeData.length(), true);
            }

            Thread.sleep(100);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        if (BuildConfig.DEBUG)
            Log.i(TAG, "Sent message R1 ");
    }


    private void ConnectUsb() {
        while (IsDeviceSleep())
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        if (BuildConfig.DEBUG)
            Log.i(TAG, "ConnectUsb");

        mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(DeviceUARTContext, "Connecting", Toast.LENGTH_SHORT).show();
            }
        });

        if (BuildConfig.DEBUG)
            Log.i(TAG, "ftDev :" + ftDev);

        int tmpProtNumber = openIndex;
        //  if (currentIndex != openIndex) {
        if (null == ftDev) {
            // ftDev = ftdid2xx.openByIndex(DeviceUARTContext, openIndex);
            ftDev = ftdid2xx.openByUsbDevice(DeviceUARTContext, _device);
            // ftDev = ftdid2xx.openBySerialNumber(this, ApplicationConfig.Companion.getDeviceSerialNumber());

        } else {
            if (ftDev.isOpen())
                ftDev.close();
            synchronized (ftDev) {
                ftDev = ftdid2xx.openByUsbDevice(DeviceUARTContext, _device);
                // ftDev = ftdid2xx.openBySerialNumber(this, ApplicationConfig.Companion.getDeviceSerialNumber());
            }
        }

        uart_configured = false;

        if (ftDev == null) {
            if (BuildConfig.DEBUG)
                Log.i(TAG, "ftDev:null  can not  openByUsbDevice ");
            return;
        }

        if (true == ftDev.isOpen()) {
            if (BuildConfig.DEBUG)
                Log.i(TAG, "ftDev:isOpen ");
            currentIndex = openIndex;
        }

        final D2xxManager.FtDeviceInfoListNode deviceInfo = ftDev.getDeviceInfo();
        final String number = deviceInfo.serialNumber;

        if (BuildConfig.DEBUG)
            Log.i(TAG, "Device number:" + number);

        // Log.i(TAG, "Device number:"+SC.encryptString(number));
        // REGISTRATION
        //  ApplicationConfig.Companion.setDevice(SC.encryptString(number));

        ConfigurateFTDI();
      /*    (!(number).equals(ApplicationConfig.Companion.getDeviceSerialNumber())) {
            mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(DeviceUARTContext, "Device not registered:" + number, Toast.LENGTH_LONG).show();
                }
            });*/

    }

    // USB  FTDI  setup
    private void ConfigurateFTDI() {
        synchronized (ftDev) {
            ftDev.resetDevice(); // flush any data from the device buffers

            SetConfig(baudRate, dataBit, stopBit, parity, flowControl);
            ftDev.purge((byte) (D2xxManager.FT_PURGE_TX));
            // ftDev.restartInTask();
            ftDev.setLatencyTimer((byte) 2);
        }

        SendStartMessage();

        if (!readThreadGoing) {
            read_thread = new readThread(handler);
            readThreadGoing = true;
            read_thread.start();
        }

        SendMCUMessage();
        // SendStartMessage();
        Intent in = new Intent(MCU_CONNECTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
        // sendBroadcast(in);
        mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(DeviceUARTContext, "Connected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * @param baud
     * @param dataBits
     * @param stopBits
     * @param parity
     * @param flowControl
     */
    private void SetConfig(int baud, byte dataBits, byte stopBits, byte parity, short flowControl) {
        if (!ftDev.isOpen()) {
            Log.i(TAG, "ftDev: device not open");
            return;
        }

        // configure our port
        // reset to UART mode for 232 devices
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
        ftDev.setBaudRate(baud);

        switch (dataBits) {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }

        switch (stopBits) {
            case 1:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }

        switch (parity) {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            case 4:
                parity = D2xxManager.FT_PARITY_SPACE;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }

        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

        short flowCtrlSetting;
        switch (flowControl) {
            case 0:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        // TODO : flow ctrl: XOFF/XOM
        // TODO : flow ctrl: XOFF/XOM
        ftDev.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);
        uart_configured = true;
    }

    // Process incoming MCU Message
    public void ProcessMessage(String str) {
        {
            if (BuildConfig.DEBUG)
                Log.i(TAG, "ProcessMessage:" + str);
            Intent in = new Intent(ACTION);
            in.putExtra("Message", str);

            LocalBroadcastManager.getInstance(this).sendBroadcast(in);
            sendBroadcast(in);
        }
    }


    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (BuildConfig.DEBUG)
                Log.i(TAG, "handleMessage:" + msg.obj);

            ProcessMessage((String) msg.obj);
        }
    };

    private class readThread extends Thread {
        Handler mHandler;
        String datastr = new String();
        String toSend = null;
        Long tsLong;
        String brokenString = "";

        readThread(Handler h) {
            mHandler = h;
            this.setPriority(Thread.MAX_PRIORITY);
        }

        @Override
        public void run() {
            int i;
            boolean isReset = false;
            tsLong = System.currentTimeMillis() / 10;

            while (readThreadGoing && !isReset) {
                long tscurrent = System.currentTimeMillis() / 10;
                //   Log.i(TAG, "Last message sync time:" + (tscurrent - tsLong));

                if (((tscurrent - tsLong) > 3000) && registered && !IsDeviceSleep()) {  //todo add if mcu connected and recive start message
                    if (BuildConfig.DEBUG)
                        Log.i(TAG, "Last message sync time to long:" + (tscurrent - tsLong));
                    mHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(DeviceUARTContext, "No data", Toast.LENGTH_SHORT).show();
                        }
                    });

                    synchronized (ftDev) {
                        String writeData = "X\n";
                        byte[] OutData = writeData.getBytes();
                        ftDev.write(OutData, writeData.length());

                        do {
                            iavailable = ftDev.getQueueStatus();
                            Log.e(TAG, "available: " + iavailable);
                            if (iavailable > 0) {
                                if (iavailable > MAX_READBUF_SIZE) {
                                    iavailable = MAX_READBUF_SIZE;
                                }

                                ftDev.read(readData, iavailable);
                            }
                        }
                        while (iavailable > 0);
                    }

                    SendRESETCommand(DeviceUARTContext);
                    isReset = true;

                } else {
                    synchronized (ftDev) {
                        iavailable = ftDev.getQueueStatus();
                        if (iavailable > 0 && registered) {
                            if (iavailable > MAX_READBUF_SIZE) {
                                iavailable = MAX_READBUF_SIZE;
                            }

                            ftDev.read(readData, iavailable);
                            for (i = 0; i < iavailable; i++) {
                                readDataToText[i] = (char) readData[i];
                            }

                            tsLong = System.currentTimeMillis() / 10;

                            if (registered) {
                                byte[] received = new byte[iavailable];
                                System.arraycopy(readData, 0, received, 0, iavailable);
                                String receivedStr = new String(received).trim();
                                if (!receivedStr.isEmpty())
                                    if ((receivedStr.startsWith("{")) && (receivedStr.endsWith("}"))) {
                                        if (BuildConfig.DEBUG)
                                            Log.i(TAG, "Recived completed  message(s):" + receivedStr);

                                        EventBus.getDefault().post(new MessageEvent(receivedStr));
                                        brokenString = "";
                                    } else if (receivedStr.startsWith("{")) {

                                        if (BuildConfig.DEBUG)
                                            Log.i(TAG, "Reciveed started message:" + receivedStr);

                                        datastr = receivedStr;
                                        brokenString = "";
                                    } else if ((datastr.startsWith("{")) && receivedStr.trim().endsWith("}")) {

                                        if (BuildConfig.DEBUG)
                                            Log.i(TAG, "Recive finished message:" + receivedStr);

                                        datastr += receivedStr;

                                        if (BuildConfig.DEBUG)
                                            Log.i(TAG, "Combined message:" + datastr);

                                        String tosend = datastr;
                                        EventBus.getDefault().post(new MessageEvent(tosend));
                                        datastr = "";
                                        brokenString = "";
                                    } else {
                                        String[] messages = IncomingMessageUtil.Companion.toArray(receivedStr);
                                        int n = messages.length;
                                        int current = 0;
                                        if (n >= 2) {
                                            while (current <= n - 1) {
                                                if (brokenString.length() == 0) {
                                                    if (current == n - 1) {
                                                        brokenString = messages[current];

                                                        if (BuildConfig.DEBUG)
                                                            Log.i(TAG, "Broken  message end:" + brokenString);
                                                    } else {
                                                        if (messages[current].startsWith("{") && messages[current].endsWith("}")) {
                                                            String tosend = messages[current];

                                                            if (BuildConfig.DEBUG)
                                                                Log.i(TAG, "Broken  message  contains full :" + messages[current]);

                                                            EventBus.getDefault().post(new MessageEvent(tosend));
                                                        } else {
                                                            brokenString = messages[current + 1];
                                                            current += 1;

                                                            if (BuildConfig.DEBUG)
                                                                Log.i(TAG, "Broken  message start:" + brokenString);

                                                        }
                                                    }
                                                } else {
                                                    brokenString += messages[current];
                                                    if (brokenString.startsWith("{") && brokenString.endsWith("}")) {
                                                        String tosend = brokenString;
                                                        EventBus.getDefault().post(new MessageEvent(tosend));
                                                        if (BuildConfig.DEBUG)
                                                            Log.i(TAG, "Broken  message  completed :" + brokenString);
                                                        brokenString = "";
                                                    }
                                                }
                                                current += 1;
                                            }
                                        } else {
                                            if (BuildConfig.DEBUG)
                                                Log.e(TAG, "Unknown message:" + receivedStr);
                                            mHandler.post(new Runnable() {
                                                public void run() {
                                                    //Toast.makeText(DeviceUARTContext, "Connecting", Toast.LENGTH_SHORT).show();
                                                    Toast.makeText(DeviceUARTContext, "Unknown message", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            SendRESETCommand(DeviceUARTContext);
                                            isReset = true;
                                        }
                                    }
                            }
                        } else if (toSend != null) {
                            byte[] OutData = toSend.getBytes();
                            ftDev.write(OutData, toSend.length());
                            toSend = null;
                            if (BuildConfig.DEBUG)
                                Log.v(TAG, "Sent message");
                        }
                    }
                }
            }
        }
    }

    private boolean isApplicationRunning() {
        List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
        for (AndroidAppProcess process : processes) {
            if (process.name.equalsIgnoreCase("com.hzbhd.alexross.subarulan2")) {
                //Log.d(TAG, "Process foregraund:" + process.foreground);
                return true;
            }
        }
        return false;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "onStartCommand:" + intent.getAction());

            String action = intent.getAction();
            if (ACTION_RECONNECT_COMMAND.equals(action)) {
                if (ftDev != null) {
                    if (ftDev.isOpen()) {
                        reConnect();
                    }
                }
            } else if (ACTION_STOP.equals(action)) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "ACTION_STOP");
                readThreadGoing = false;
                StopService(this);
                stopSelf();
            }
        }
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // android.os.Debug.waitForDebugger();
        if (intent != null) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "onHandleIntent:" + intent.getAction());

            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                serviceRuning = true;
                if (ftDev == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    findSerialPortDevice();
                    // ConnectUsb();
                }
                while (!stopService) {

                }

                if (stopService) {
                    String writeData = "X\n";
                    byte[] OutData = writeData.getBytes();
                    ftDev.write(OutData, writeData.length());

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }

                    serviceRuning = false;
                    readThreadGoing = false;
                    //  ftDev.purge((byte) (D2xxManager.FT_PURGE_RX));
                    // ftDev.purge((byte) (D2xxManager.FT_PURGE_TX));
                    //ftDev.restartInTask();
                    //ftDev.resetDevice();
                    //  Reset();
                    ftDev.close();
                    ftDev = null;
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "stopService");
                    //  stopSelf();
                    //  unregisterReceiver(receiver);
                    //  Thread.currentThread().interrupt();
                }
            }
        }
    }
}


