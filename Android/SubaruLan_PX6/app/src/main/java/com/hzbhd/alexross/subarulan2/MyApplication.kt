/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2


import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.hzbhd.alexross.subarulan2.models.*
import com.hzbhd.midware.constant.channelManagerDef
import com.hzbhd.midwareproxy.dsp.DspManager
import com.hzbhd.midwareproxy.radio.RadioManager
import com.hzbhd.midwareproxy.sourceswitch.SourceSwitchManager
import com.jaredrummler.android.processes.AndroidProcesses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.random.Random


/**
 * Created by Alexey Rasskazov.
 */

class MyApplication : Application() {

    val rm = RadioManager.getRadioManager()
    private var view: View? = null
    private var asistView: View? = null
    //NWD
    private final val ACTION_SET_MUTE = "com.nwd.action.ACTION_SET_MUTE"

    private val TAG = MyApplication::class.java.simpleName


    private var starting = false
    private var file_CAN: BufferedWriter? = null
    private var file_LAN: BufferedWriter? = null

    internal var gson = Gson()

    internal var isServiceRunning: Boolean = false

    internal lateinit var bManager: LocalBroadcastManager

    private var mHandler: Handler? = null


    internal var listener: SharedPreferences.OnSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        // listener implementation

        canLogging = prefs.getBoolean("CAN_LOGGING", false)
        if (canLogging)
            startCANLogging()
        else
            stopCANLogging()

        lanLogging = prefs.getBoolean("LAN_LOGGING", false)
        if (lanLogging)
            startLANLogging()
        else
            stopLANLogging()

        assistEnable = prefs.getBoolean("ASSIST", false)
        stModel.infoWindowEnable = assistEnable

        var infoType: String = prefs.getString("assist_preference_infoType", "1")!!
        stModel.infoWindowType = infoType.toInt()


        var str: String = prefs.getString("FUEL_CORRECTION", "1.53")!!

        stModel.canModel.fuelCorrection = str.toFloat()  // prefs.getFloat("FUEL_CORRECTION", 1f)

        var strTemp: String = prefs.getString("TEMP_CORRECTION", "0")!!
        stModel.canModel.tempCorrection = strTemp.toFloat()

        engineTempWarning = prefs.getBoolean("ENGINE_TEMP_WARNING", false)
        var str2: String = prefs.getString("ENGINE_TEMP_WARNING_VALUE", "100")!!
        engineTempWarningValue = str2.toInt()
        engineTempShow = prefs.getBoolean("ENGINE_TEMP_SHOW", false)
        stModel.canModel.engine_temperatureEnable = engineTempShow
        stModel.canModel.engine_temperature_warning_value = engineTempWarningValue
        stModel.canModel.fuelConsamptionEnable = prefs.getBoolean("FUEL_CONS", false)
        stModel.canModel.fuelConsamptionChartEnable = prefs.getBoolean("FUEL_CONS_CHART", false)
        var strfcChartType: String = prefs.getString("preference_ChartType", "0")!!
        stModel.canModel.fcChartType = strfcChartType.toInt()

        stModel.canModel.sheduler = prefs.getBoolean("SERVICE_ENABLE", false)
        var str3: String = prefs.getString("OIL_REPLACEMENT_INTERVAL", "10000")!!
        stModel.canModel.sheduler_interval = str3.toInt()

        if (stModel.canModel.sheduler == false) {
            repository.clear()
        }

        stModel.canModel.assistColor = prefs.getInt("assist_preference", 0)

        Log.i("APPLICATION", "Settings key changed: " + key)
    }

    init {

    }


    override fun onCreate() {

        super.onCreate()
        application = this

        val wordsDao = VehicleDatabase.getInstance(this).vehicleDao()
        repository = VehicleRepository(wordsDao)

        Log.i(TAG, "ON CREATE")

        mp = MediaPlayer.create(this, R.raw.castor)


        am = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager;
        //  mp.setDataSource(this, R.raw.alarm_beep_01)
        // mp.prepare()
        // Create an explicit intent for an Activity in your app

        val duration = 500
        val checkServiceRunnable = Runnable {
            Log.i(TAG, "BackgroundUSBService.isRunning: " + BackgroundUSBService.isRunning)
            if (!BackgroundUSBService.isRunning)
                BackgroundUSBService.StartUSBService(this)
        }

        val handler = Handler()
        handler.postDelayed(checkServiceRunnable, duration.toLong())

        val incomingCallFilter = IntentFilter("com.bt.ACTION_BT_INCOMING_CALL")
        registerReceiver(btCallReceiver, incomingCallFilter)

        val outgoingCallFilter = IntentFilter("com.bt.ACTION_BT_BEGIN_CALL_ONLINE")
        registerReceiver(btCallReceiver, outgoingCallFilter)

        val incomingCallEndFilter = IntentFilter("com.bt.ACTION_BT_END_CALL")
        incomingCallEndFilter.priority = 999
        registerReceiver(btCallReceiver, incomingCallEndFilter)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "my_channel"
            val descriptionText = "subaru channel"
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel("234", name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        mHandler = Handler(Looper.getMainLooper())
    }

    override fun onTerminate() {
        Log.i("APPLICATION", "onTerminate: " + this)
        mp.release()
        stopLANLogging()
        stopCANLogging()
        unregisterReceiver(btCallReceiver)

        super.onTerminate()
    }


    fun startReceive() {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "start recive: " + starting)

        if (!starting) {
            appContext = applicationContext

            // Register EventBus
            EventBus.getDefault().register(this)
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

//            canLogging = sharedPref.getBoolean("CAN_LOGGING", false)
//            if (canLogging && file_CAN == null)
//                startCANLogging()
//
//            lanLogging = sharedPref.getBoolean("LAN_LOGGING", false)
//            if (lanLogging && file_LAN == null)
//                startLANLogging()

            assistEnable = sharedPref.getBoolean("ASSIST", false)
            stModel.infoWindowEnable = assistEnable

            val infoType: String = sharedPref.getString("assist_preference_infoType", "1")!!
            stModel.infoWindowType = infoType.toInt()

            stModel.canModel.tpmsEnable = sharedPref.getBoolean(R.string.settings_key_tpms.toString(), true)


            val str: String = sharedPref.getString("FUEL_CORRECTION", "1")!!
            stModel.canModel.fuelCorrection = str.toFloat()  // prefs.getFloat("FUEL_CORRECTION", 1f)

            val strTemp: String = sharedPref.getString("TEMP_CORRECTION", "1")!!
            stModel.canModel.tempCorrection = strTemp.toFloat()

            engineTempWarning = sharedPref.getBoolean("ENGINE_TEMP_WARNING", false)
            val str2: String = sharedPref.getString("ENGINE_TEMP_WARNING_VALUE", "100")!!
            engineTempWarningValue = str2.toInt()
            engineTempShow = sharedPref.getBoolean("ENGINE_TEMP_SHOW", false)
            stModel.canModel.engine_temperatureEnable = engineTempShow
            stModel.canModel.engine_temperature_warning_value = engineTempWarningValue

            stModel.canModel.fuelConsamptionEnable = sharedPref.getBoolean("FUEL_CONS", false)
            stModel.canModel.fuelConsamptionChartEnable = sharedPref.getBoolean("FUEL_CONS_CHART", false)

            val strfcChartType: String = sharedPref.getString("preference_ChartType", "0")!!
            stModel.canModel.fcChartType = strfcChartType.toInt()

            stModel.canModel.sheduler = sharedPref.getBoolean("SERVICE_ENABLE", false)
            val str3: String = sharedPref.getString("OIL_REPLACEMENT_INTERVAL", "10000")!!
            stModel.canModel.sheduler_interval = str3.toInt()

            stModel.canModel.assistColor = sharedPref.getInt("assist_preference", 0)

            sharedPref.registerOnSharedPreferenceChangeListener(listener)

            bManager = LocalBroadcastManager.getInstance(this)

// todo
            val filter = IntentFilter(BackgroundUSBService.ACTION)
            LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter)
            // this.registerReceiver(messageReceiver, filter)

            val actionStopFilter = IntentFilter(BackgroundUSBService.ACTION_STOP)
            LocalBroadcastManager.getInstance(this).registerReceiver(stopReceiver, actionStopFilter)
            registerReceiver(stopReceiver, actionStopFilter)

            starting = true

            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // EMULATION

            // runnable.run()
            //val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        }
    }


    private val handler = Handler()

    // Emulate incoming  mcu messages
    private val runnable = object : Runnable {
        override fun run() {
              if(!MyApplication.stModel.radioOn )
              MyApplication.stModel.radioOn = true;
               if(  MyApplication.stModel.currentState != State.OFF)
               MyApplication.stModel.currentState=State.OFF;
            try {
                // rm.seekUp()
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            handler.postDelayed(this, 6000)

            MyApplication.soundNotify()
            val message = LanMessage()
            message.M = intArrayOf(0x4A, 0x53, 0x55, 0x08, 0x15, 0x51, 0x54, 0x1E, 0x02, 0x248C, 0x65, Random.nextInt(255))
            message.A = 0xfff;
            message.T = LanMessageType.CAN
            MyApplication.stModel.canModel.processMessage(message);

            val message3 = LanMessage()
            message3.M = intArrayOf(0x50, 0x01, 0x22, 0x22, 0x01, 0x05, 0x00, 0x00)
            message3.A = 0xfff;
            message3.T = LanMessageType.LAN
          //      MyApplication.stModel.messageAnalizer.ProcessMessage(message3);

            val message2 = LanMessage()
            message2.M = intArrayOf(1, 2, 8, 5, 67, 230, 155, 215)
            message2.A = 0x0;
            message2.T = LanMessageType.MCU
            //  MyApplication.stModel.messageAnalizer.ProcessMessage(message2)
            //  if (MyApplication.stModel.soundSettings.volume <= 100)
            //    MyApplication.stModel.soundSettings.volume = MyApplication.stModel.soundSettings.volume + 1
            //Audio.isAudioAppRunning(MyApplication.appContext,"com.hzbhd.media")
        }
    }

    companion object {
        private lateinit var mp: MediaPlayer
        private lateinit var am: AudioManager
        private var can_play: Boolean = true;

        var incomingCall = false

        var appContext: Context? = null
            private set

        var stModel: StateModel = StateModel()
        var canLogging = false
        var lanLogging = false

        var engineTempWarning = false
        var engineTempWarningValue: Int = 100
        var engineTempShow = false
        var assistEnable = false

        fun activityResumed() {
            isActivityVisible = true
            val notificationManager = this!!.appContext?.let { NotificationManagerCompat.from(it) }
            notificationManager?.cancel(111111)
        }

        var soundSource = 0;

        lateinit var repository: VehicleRepository

        fun activityPaused() {

            if (BuildConfig.DEBUG)
                Log.i("MyApplication", "activityPaused: " + isActivityVisible)

            isActivityVisible = false

            val intent = Intent(appContext!!, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val pendingIntent = PendingIntent.getActivity(appContext, 0, intent, 0)
            var btm = BitmapFactory.decodeResource(appContext!!.resources, R.mipmap.ic_stat_suba_big)
            val mBuilder = NotificationCompat.Builder(appContext)
                    .setSmallIcon(R.mipmap.ic_stat_suba)
                    .setChannelId("234")
                    .setLargeIcon(btm)
                    // .setColor(     R.color.blue)
                    .setContentTitle("Subaru")
                    .setContentText("Subaru service")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(false)
                    .setOngoing(true)

            val notificationManager = this!!.appContext?.let { NotificationManagerCompat.from(it) }
            notificationManager?.notify(111111, mBuilder.build())
        }

        var isActivityVisible: Boolean = false
            private set

        lateinit var application: MyApplication

        fun soundNotify() {
            if (BuildConfig.DEBUG)
                Log.i("tag", "volumeValue: " + DspManager.getInstance().isMute)
            // DspManager.getInstance().playBeep()
            // Log.i("tag", "isTalking: " + DataDao.getInstance().isTalking)

            // Log.i("tag", " Misc): " + MiscService.getMiscService( this!!.appContext).isPowerOffStatus)
            // var amanager = AudioSourceManager.getManager()  as McuCtrlAspAdapter
            // amanager.setRadioMute(true)
            // amanager.gpioMute(257)

            var sourceID: channelManagerDef.MEDIA_SOURCE_ID?
            if (BuildConfig.DEBUG){
                sourceID= channelManagerDef.MEDIA_SOURCE_ID.NORMAL_SOURCE
            }
            else {
                sourceID = SourceSwitchManager.getSourceSwitchManager()
                    .getCurrentValidSource(channelManagerDef.SEAT_TYPE.front_seat)
                // SourceSwitchManager.getSourceSwitchManager().audioChannelRelease(sourceID, channelManagerDef.SEAT_TYPE.front_seat, null as Bundle?)
                if (BuildConfig.DEBUG)
                    Log.i("tag", "volumeValue: " + sourceID)
                // if (sourceID != channelManagerDef.MEDIA_SOURCE_ID.NORMAL_SOURCE)
            }
                var bnd = Bundle()
                bnd.putString(ServiceConstants.Operation_Flag, "release")
                if (sourceID != channelManagerDef.MEDIA_SOURCE_ID.NORMAL_SOURCE)
                    SourceSwitchManager.getSourceSwitchManager().audioChannelRequest(
                        channelManagerDef.MEDIA_SOURCE_ID.NORMAL_SOURCE,
                        channelManagerDef.SEAT_TYPE.front_seat,
                        bnd
                    )

            val result = am.requestAudioFocus(null,
                    // Use the music stream.
                    AudioManager.STREAM_NOTIFICATION,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                val mNextIntent = Intent("com.nwd.ACTION_NAVI_SOUND")
                mNextIntent.putExtra("VOICEPROTOCOL", "play")
                // DeviceUARTContext.sendBroadcast(mNextIntent);
                if (can_play)
                    {
                        try {
                            // am.setStreamMute(AudioManager.STREAM_MUSIC, true);
                            can_play = false
                            mp.start()
                            mp.seekTo(0)
                            val duration = 1005
                            val stopSoundRunnable = Runnable {
                               // val mNextIntent2 = Intent("com.nwd.ACTION_NAVI_SOUND")
                              //  mNextIntent2.putExtra("VOICEPROTOCOL", "stop")
                                if (mp.isPlaying()) {
                                    // mp.stop()MyApplication
                                    // DataDao.getInstance().setNaviSpeech(false);
                                }
                                mp.seekTo(0)
                                mp.pause()
                                can_play = true

                                if(sourceID!= channelManagerDef.MEDIA_SOURCE_ID.NORMAL_SOURCE)
                                SourceSwitchManager.getSourceSwitchManager().audioChannelRelease(channelManagerDef.MEDIA_SOURCE_ID.NORMAL_SOURCE, channelManagerDef.SEAT_TYPE.front_seat, bnd)

                                // mp.pause()
                                am.abandonAudioFocus(null)
                                // DeviceUARTContext.sendBroadcast(mNextIntent2);
                            }
                            val handler = Handler()
                            handler.postDelayed(stopSoundRunnable, duration.toLong())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            }
        }
    }

    val postDelayed = object : Runnable {
        override fun run() {
            view?.setVisibility(View.GONE);
        }
    }

    fun toastNotify() {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "isActivityVisible: " + isActivityVisible)

        if (!isActivityVisible) {
            if (view == null) {

                var LAYOUT_FLAG: Int;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
                }

                val inflater = LayoutInflater.from(this)
                val view = inflater.inflate(R.layout.toast_info, null)
                val volumeBind = DataBindingUtil.bind<ViewDataBinding>(view.findViewById(R.id.volume), SoundSettingsBindComponent())
                volumeBind!!.setVariable(BR.soundModel, MyApplication.stModel.soundSettings)

                val trip = DataBindingUtil.bind<ViewDataBinding>(view.findViewById(R.id.tripinfo_layout), SoundSettingsBindComponent())
                trip!!.setVariable(BR.canModel, MyApplication.stModel.canModel)

                val stateMode = DataBindingUtil.bind<ViewDataBinding>(view.findViewById(R.id.toast_info_mode__layout), SoundSettingsBindComponent())
                stateMode!!.setVariable(BR.stateModel, MyApplication.stModel)
                val params = WindowManager.LayoutParams(
                        LAYOUT_FLAG,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                params.gravity = Gravity.FILL_HORIZONTAL or Gravity.FILL_VERTICAL;

                params.setTitle("Load Average");

                val wm: WindowManager? = getSystemService(WINDOW_SERVICE) as WindowManager;
                if (wm != null) {
                    wm.addView(view, params)
                    this.view = view;
                    view?.postDelayed(postDelayed, 1000)
                };
            } else {
                view?.removeCallbacks(postDelayed);
                view?.setVisibility(View.VISIBLE);
                view?.postDelayed(postDelayed, 1000)
            }
        }
    }

    //Assist
    fun assistNotify() {
        if (assistEnable && !isActivityVisible && !stModel.infoWindowDisable) {
            if (asistView == null) {

                var LAYOUT_FLAG: Int;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
                }
                val inflater = LayoutInflater.from(this)
                var asistView = inflater.inflate(R.layout.assist_info, null)

                val trip = DataBindingUtil.bind<ViewDataBinding>(asistView, SoundSettingsBindComponent())
                trip!!.setVariable(com.hzbhd.alexross.subarulan2.BR.canModel, MyApplication.stModel.canModel)
                trip.setVariable(com.hzbhd.alexross.subarulan2.BR.stateModel, MyApplication.stModel)

                val tripInfo = DataBindingUtil.bind<ViewDataBinding>(asistView.findViewById(R.id.tripinfo_layout), SoundSettingsBindComponent())
                tripInfo!!.setVariable(BR.canModel, MyApplication.stModel.canModel)

                val stateMode = DataBindingUtil.bind<ViewDataBinding>(asistView.findViewById(R.id.toast_info_mode__layout), SoundSettingsBindComponent())
                stateMode!!.setVariable(BR.stateModel, MyApplication.stModel)
                val params = WindowManager.LayoutParams(
                        LAYOUT_FLAG,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                params.gravity = Gravity.FILL_HORIZONTAL or Gravity.FILL_VERTICAL;

                params.setTitle("Load Average");
                var wm: WindowManager = getSystemService(WINDOW_SERVICE) as WindowManager;
                wm.addView(asistView, params);
                this.asistView = asistView;

            } else {
                if (!this.asistView!!.isShown)
                    asistView?.setVisibility(View.VISIBLE);
            }
        } else
            asistView?.setVisibility(View.INVISIBLE);
    }


    fun muteNotify() {
        val i = Intent(ACTION_SET_MUTE)
        i.putExtra("extra_mute", 2)
        MyApplication.appContext?.sendBroadcast(i)
    }


    fun showActivity() {
        if (!isActivityVisible) {
            if (BuildConfig.DEBUG)
                Log.i(TAG, "showActivity: ")
            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            this.startActivity(i)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    @Keep
    fun onMessageEvent(event: BackgroundUSBService.MessageEvent) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "onMessageEvent: " + event.message)
        ProcessIncomeMessage(event.message)
    }


    private val isMyServiceRunning: Boolean
        get() {
            val processes = AndroidProcesses.getRunningAppProcesses()
            for (process in processes) {
                if (process.name.equals("com.hzbhd.alexross.subarulan2:service", ignoreCase = true)) {
                    Log.d(TAG, "Service: started")
                    return true
                }
            }
            if (BuildConfig.DEBUG)
                Log.i(TAG, "Service not started")
            return false
        }


    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i(TAG, "stopReceiver ")
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(0);
        }
    }

    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val resultValue = intent.getStringExtra("Message")
            ProcessIncomeMessage(resultValue)
        }
    }

    private fun ProcessIncomeMessage(resultValue: String) {
        val array = resultValue.trim { it <= ' ' }.split("\\r\\n|\\n|\\r".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (item in array) {
            try {
                //todo enum deserializer error
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "GSON:" + item)

                val message = gson.fromJson(item, LanMessage::class.java)
                /*if (MyApplication.canLogging && message.T == LanMessageType.CAN)
                        write(message.T, message.M)

                    if (MyApplication.lanLogging && message.T == LanMessageType.LAN)
                        write(message.T, message.M)*/

                //  if (MyApplication.lanLogging && message.T == LanMessageType.LAN)
                //     write(message.T, message.M)
                GlobalScope.launch(Dispatchers.Main) {
                    stModel.ProcessMessage(message)
                }
                // Log.i(TAG, "AndroidProcesses.isMyProcessInTheForeground(): " + AndroidProcesses.isMyProcessInTheForeground())
                // Log.i(TAG, "  MyApplication.isActivityVisible(): " + MyApplication.isActivityVisible)
            } catch (e: Exception) {

            }
        }
    }

    private val btCallReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action === "com.bt.ACTION_BT_INCOMING_CALL") {
                incomingCall = true
            }
            if (intent.action === "com.bt.ACTION_BT_BEGIN_CALL_ONLINE") {
                incomingCall = true
            }
            if (intent.action === "com.bt.ACTION_BT_END_CALL")
                incomingCall = false

            if (BuildConfig.DEBUG)
                Log.d(TAG, "Call- $intent")
        }
    }

// CAN messages logging (CAN_***.csv)
    private fun startCANLogging() {
        // Prepare data storage
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val name = "CAN_" + System.currentTimeMillis() + ".csv"

        try {
            val filename = File(directory, name)
            file_CAN = BufferedWriter(FileWriter(filename, true))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    // Media LAN messages logging (LAN_***.csv)
    private fun startLANLogging() {
        // Prepare data storage
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val name = "LAN_" + System.currentTimeMillis() + ".csv"

        try {
            val filename = File(directory, name)
            file_LAN = BufferedWriter(FileWriter(filename, true))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopCANLogging() {
        try {
            file_CAN?.flush();
            file_CAN?.close();
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    private fun stopLANLogging() {
        try {
            file_LAN?.flush();
            file_LAN?.close();
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    private fun write(type: LanMessageType, values: IntArray) {
        when (type) {
            LanMessageType.LAN -> {
                if (file_LAN == null)
                    return

                var line = ""
                if (values != null) {
                    for (value in values) {
                        line += "," + String.format("%02X", value)
                    }
                }

                line = (java.lang.Long.toString(System.currentTimeMillis()) + line + "\n")

                try {
                    file_LAN?.write(line)
                    file_LAN?.flush();
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            LanMessageType.CAN -> {
                if (file_CAN == null)
                    return

                var line = ""
                if (values != null) {
                    for (value in values) {
                        line += "," + String.format("%02X", value)
                    }
                }

                line = (java.lang.Long.toString(System.currentTimeMillis()) + line + "\n")

                try {
                    file_CAN?.write(line)
                    file_CAN?.flush();
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


}
