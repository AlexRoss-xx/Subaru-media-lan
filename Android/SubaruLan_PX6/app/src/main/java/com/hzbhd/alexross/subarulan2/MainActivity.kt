/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*

import android.hardware.usb.UsbManager
import android.os.*

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

import com.hzbhd.alexross.subarulan2.controls.seekBar.HorizontalSeekbarWithIntervals
import com.hzbhd.alexross.subarulan2.controls.seekBar.VerticalSeekbarWithIntervals
import com.jaredrummler.android.processes.AndroidProcesses
import com.jaredrummler.android.processes.models.AndroidAppProcess

import java.util.Calendar
import androidx.core.view.GestureDetectorCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.hzbhd.alexross.subarulan2.BackgroundUSBService.*
import com.hzbhd.alexross.subarulan2.models.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    private val TAG = MainActivity::class.java.simpleName
    private var mDetector: GestureDetectorCompat? = null

    private val SWIPE_DISTANCE_THRESHOLD = 50
    private val SWIPE_VELOCITY_THRESHOLD = 50

    internal var isServiceRunning: Boolean = false

    private var initialX: Float = 0.toFloat()

    private lateinit var pd: ProgressDialog

    private var nn = 0

    private val bReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "bReceiver: ")
            if (intent.action == RECEIVE_JSON) {
                val serviceJsonString = intent.getStringExtra("json")
                //Do something with the string
                if (BuildConfig.DEBUG)
                    Log.e(TAG, "New Data: " + serviceJsonString)
            }
        }
    }

    private var label: TextView? = null
    private var mViewFlipper: ViewFlipper? = null
    private var mContext: Context? = null

    private val tunerModel: TunerModel? = null

    private var scroll: ScrollView? = null

    private val handler = Handler()

    private var toaast: Toast? = null



    // Emulate incoming messages
    private val runnable = object : Runnable {
        override fun run() {
            MyApplication.stModel.cdChangerModel.cdChangerTable[0] = !MyApplication.stModel.cdChangerModel.cdChangerTable[0]

            // mViewFlipper.showNext();
            //  MyApplication.stModel.ChangeState(State.TUNER);
            /* switch (MyApplication.stModel.getCurrentState()) {
                case UNKNOWN:
                    MyApplication.stModel.ChangeState(State.TUNER);
                    break;
                case TUNER:
                    MyApplication.stModel.ChangeState(State.CD);
                    break;
                case CD:
                    MyApplication.stModel.ChangeState(State.RSE);
                    break;
                case RSE:
                    MyApplication.stModel.ChangeState(State.UNKNOWN);
                    break;
                case SAT:
                    break;
                case SOUNDSETTINGS:
                    mViewFlipper.setDisplayedChild(4);
                    break;
                default:
                    break;
            }
*/
            val message = LanMessage()
            //   MyApplication.stModel.currentState = State.SOUNDSETTINGS
            MyApplication.stModel.canModel.range = nn * 10
            //  if (MyApplication.stModel.currentState == State.CD) {
            //      MyApplication.stModel.ChangeState(State.TUNER)
//                message.T = LanMessageType.LAN
//                message.A = 0xfff
//                message.M = intArrayOf(0x60, 0xC0, 0x00, 0xCF, 0xFF, 0x00, 0x00, 0x02, 0x00, 0x02, 0x00, 0x00)
//                MyApplication.stModel.ProcessMessage(message);
            // } else {
            //     MyApplication.stModel.ChangeState(State.CD)
            //    MyApplication.stModel.cdChangerModel.curentCD = 2
//                message.T = LanMessageType.LAN
//                message.A = 0xfff
//               // message.M = new int[]{0x60, 0xC0, 0x00, 0xCF, 0xFF, 0x00, 0x00, 0x02, 0x00, 0x02, 0x00, 0x00};
//                    message.M = intArrayOf(0x60, 0xC0, 0x00, 0xCF, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
//                MyApplication.stModel.ProcessMessage(message);
            // }

//            message.T = LanMessageType.LAN
//            message.A = 0x140
//            message.M = intArrayOf(0x60, 0x02, 0x02, 0x11, 0x33, 0x31, 0x13, 0xFF, 0xFF)
//            MyApplication.stModel.ProcessMessage(message)
//
//           message.A = 0x140
//           message.M = intArrayOf(0x60, 0x02, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x15, 0xBB, 0x01, 0x05, 0x01, 0xB0, 0x05, 0xBB, 0xBB, 0xBB, 0xBB, 0x00, 0xBB, 0xBB, 0x01, 0x02, 0x03)
//           MyApplication.stModel.ProcessMessage(message)

//
//
//            message.A = 0x140
//            message.M = intArrayOf(0x65, 0x02, 0x00, 0x01, 0x01, 0x02, 0x00, 0x04, 0x00, 0x00, 0x32, 0x20, 0x42, 0x69, 0x20, 0x4D, 0x61, 0x63, 0x68, 0x69, 0x6E, 0x65, 0x00, 0x00, 0x00, 0x00, 0x00)
//            MyApplication.stModel.ProcessMessage(message)
//
//            message.A = 0x140
//            message.M = intArrayOf(0x65, 0x02, 0x00, 0x01, 0x01, 0x02, 0x01, 0x04, 0x01, 0x00, 0x30, 0x30, 0x31, 0x20, 0x2D, 0x20, 0x55, 0x6E, 0x20, 0x2D, 0x20, 0x44, 0x65, 0x63, 0x65, 0x69, 0x00)
//            MyApplication.stModel.ProcessMessage(message)
//
//            message.A = 0x140
//            message.M = intArrayOf(0x65, 0x02, 0x00, 0x01, 0x01, 0x02, 0x01, 0x04, 0x01, 0x01, 0x76, 0x65, 0x64, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
//            MyApplication.stModel.ProcessMessage(message)
//
//            message.T = LanMessageType.CAN
//            message.A = 0x041
//            message.M = intArrayOf(0x05, 0x00, 0x0D, 0x21, 0x40, 0x11, 0x05, 0x00)
//            MyApplication.stModel.ProcessMessage(message)
//
//            message.T = LanMessageType.CAN
//            message.A = 0x040
//            message.M = intArrayOf(0x81, 0x45, 0x00, 0x55, 0x00, 0x5A, 0x62, 0x08)
//            MyApplication.stModel.ProcessMessage(message)
//
//
//
            message.T = LanMessageType.CAN
            message.A = 0xFFF
            message.M = intArrayOf(0x00, 0x00, 0x00, 0x00, 17, 43, 43, 0x00, 0x00, 0x00, 0x00, 0x00)
            MyApplication.stModel.ProcessMessage(message)


            // MyApplication.stModel.canModel.tireAlert = !MyApplication.stModel.canModel.tireAlert
            // MyApplication.stModel.getCanModel().setLowFuel(!MyApplication.stModel.getCanModel().getLowFuel());


            if (MyApplication.stModel.soundSettings.active == 1 || MyApplication.stModel.soundSettings.active == 0)
                MyApplication.stModel.soundSettings.active = 2
            else
                MyApplication.stModel.soundSettings.active = 1


            MyApplication.stModel.soundSettings.bass = 5
            MyApplication.stModel.soundSettings.mid = -3

            MyApplication.stModel.tunerModel.currentFRQ = "" + nn

            if (MyApplication.stModel.tunerModel.stationNumber == 1 || MyApplication.stModel.tunerModel.stationNumber == 0)
                MyApplication.stModel.tunerModel.stationNumber = 2
            else
                MyApplication.stModel.tunerModel.stationNumber = 1


            if (MyApplication.stModel.soundSettings.fade <= 9)
                MyApplication.stModel.soundSettings.fade = MyApplication.stModel.soundSettings.fade + 1
            if (MyApplication.stModel.soundSettings.fade == 9)
                MyApplication.stModel.soundSettings.fade = -9

            if (MyApplication.stModel.soundSettings.balance <= 9)
                MyApplication.stModel.soundSettings.balance = MyApplication.stModel.soundSettings.balance + 1
            if (MyApplication.stModel.soundSettings.balance == 9)
                MyApplication.stModel.soundSettings.balance = -9

            MyApplication.stModel.soundSettings.volume = nn

            MyApplication.stModel.soundSettings.mute = true

            MyApplication.stModel.satModel.onNext()
            handler.postDelayed(this, 5000)
            nn++

            MyApplication.stModel.soundSettings.mute = false
            val c = Calendar.getInstance()
            val dateformat = java.text.SimpleDateFormat("HH:mm")
            val datetime = dateformat.format(c.time)


        }
    }



    companion object {
        val RECEIVE_JSON = "com.hzbhd.alexross.subarulan2.RECEIVE_JSON"
        lateinit var activity: AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mDetector = GestureDetectorCompat(this, this)
        activity = this;

        val intent = getIntent();
        if (intent != null) {
            val action = intent.action;
            if (BuildConfig.DEBUG)
                Log.v(TAG, "onCreate:" + intent + "-" + action)
        }

        mViewFlipper = this.findViewById<View>(R.id.view_flipper) as ViewFlipper

        mContext = this

        thread {
            this.runOnUiThread {

                val settings = findViewById<View>(R.id.carinfo_layout)
                val settingsbind = DataBindingUtil.bind<ViewDataBinding>(settings, SoundSettingsBindComponent())
                settingsbind!!.setVariable(BR.canModel, MyApplication.stModel.canModel)

                val content = findViewById<View>(R.id.tuner_layout)
                val bind = DataBindingUtil.bind<ViewDataBinding>(content, SoundSettingsBindComponent())
                bind!!.setVariable(BR.tunerModel, MyApplication.stModel.tunerModel)

                val soundcontent = findViewById<View>(R.id.sound_layout)
                val bindSound = DataBindingUtil.bind<ViewDataBinding>(soundcontent, SoundSettingsBindComponent())
                bindSound!!.setVariable(BR.soundModel, MyApplication.stModel.soundSettings)

                val changercontent = findViewById<View>(R.id.changer_layout)
                val bindChanger = DataBindingUtil.bind<ViewDataBinding>(changercontent, SoundSettingsBindComponent())
                bindChanger!!.setVariable(BR.cdChangerModel, MyApplication.stModel.cdChangerModel)

                val rsecdccontent = findViewById<View>(R.id.rsecdc_layout)
                val bindRseCdc = DataBindingUtil.bind<ViewDataBinding>(rsecdccontent, SoundSettingsBindComponent())
                bindRseCdc!!.setVariable(BR.rsecdcModel, MyApplication.stModel.RSECDCModel)

                val alertview = findViewById<View>(R.id.alerts)
                val alertbind = DataBindingUtil.bind<ViewDataBinding>(alertview, SoundSettingsBindComponent())
                alertbind!!.setVariable(BR.canModel, MyApplication.stModel.canModel)

                val tripinfo_content = findViewById<View>(R.id.tripinfo)
                val tripInfoBind = DataBindingUtil.bind<ViewDataBinding>(tripinfo_content, SoundSettingsBindComponent())
                tripInfoBind!!.setVariable(BR.canModel, MyApplication.stModel.canModel)
                tripInfoBind!!.setVariable(BR.stateModel, MyApplication.stModel)
                tripInfoBind!!.setVariable(BR.showChart, true)

                val volume_content = findViewById<View>(R.id.volume)
                val volumeBind = DataBindingUtil.bind<ViewDataBinding>(volume_content, SoundSettingsBindComponent())
                volumeBind!!.setVariable(BR.soundModel, MyApplication.stModel.soundSettings)

                val cdscontent = findViewById<View>(R.id.cds)
                val cdsBind = DataBindingUtil.bind<ViewDataBinding>(cdscontent, SoundSettingsBindComponent())
                cdsBind!!.setVariable(BR.cdChangerModel, MyApplication.stModel.cdChangerModel)

                val satcontent = findViewById<View>(R.id.sat_layout)
                val satBind = DataBindingUtil.bind<ViewDataBinding>(satcontent, SoundSettingsBindComponent())
                satBind!!.setVariable(BR.canModel, MyApplication.stModel.canModel)

                var button = settings.findViewById<Button>(R.id.button2_4)
                button.setOnClickListener(View.OnClickListener {
                    // Code here executes on main thread after user presses button
                    showSettings()
                });
            }
        }
        MyApplication.stModel.setListener(object : StateModel.ChangeListener {
            override fun onChange() {
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "State  Flipper:" + MyApplication.stModel.currentState.toString())

                when (MyApplication.stModel.currentState) {
                    State.OFF -> mViewFlipper!!.displayedChild = 0
                    State.TUNER -> mViewFlipper!!.displayedChild = 2
                    State.CD -> mViewFlipper!!.displayedChild = 3
                    State.RSE -> mViewFlipper!!.displayedChild = 4
                    State.RSECDC -> mViewFlipper!!.displayedChild = 7
                    State.SAT -> mViewFlipper!!.displayedChild = 5
                    State.SOUNDSETTINGS -> mViewFlipper!!.displayedChild = 6
                    State.CARINFO -> {
                        if (MyApplication.stModel.prevState == State.UNKNOWN) {
                            MyApplication.stModel.prevState = MyApplication.stModel.currentState
                            mViewFlipper!!.displayedChild = 1
                        } else {
                            MyApplication.stModel.ChangeState(MyApplication.stModel.prevState)
                            MyApplication.stModel.prevState = State.UNKNOWN
                        }
                    }
                }
            }
        })

        when (MyApplication.stModel.currentState) {
            State.OFF -> mViewFlipper!!.displayedChild = 0
            State.TUNER -> mViewFlipper!!.displayedChild = 2
            State.CD -> mViewFlipper!!.displayedChild = 3
            State.RSE -> mViewFlipper!!.displayedChild = 4
            State.RSECDC -> mViewFlipper!!.displayedChild = 7
            State.SAT -> mViewFlipper!!.displayedChild = 5
            State.SOUNDSETTINGS -> mViewFlipper!!.displayedChild = 6
            State.CARINFO -> {
                if (MyApplication.stModel.prevState == State.UNKNOWN) {
                    MyApplication.stModel.prevState = MyApplication.stModel.currentState
                    mViewFlipper!!.displayedChild = 1
                } else {
                    MyApplication.stModel.ChangeState(MyApplication.stModel.prevState)
                    MyApplication.stModel.prevState = State.UNKNOWN
                }
            }
        }

        //  Emulate incoming messages
        //   runnable.run()

        setVSeekbarWithIntervals()
        registrateReceivers()
    }

 // MCU  firmware  updating flow
    private fun registrateReceivers() {
        val mcuFilter = IntentFilter(BackgroundUSBService.MCU_UPDATING)
        registerReceiver(mcuReceiver, mcuFilter)

        val mcuFilter1 = IntentFilter(BackgroundUSBService.MCU_UPDATED)
        registerReceiver(mcuReceiver, mcuFilter1)

        val mcuFilter2 = IntentFilter(BackgroundUSBService.ACTION_STOP)
        registerReceiver(stopReceiver, mcuFilter2)
    }

    private val mcuReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "mcuReceiver: " + intent.action.toString())
            if (intent.action.equals(BackgroundUSBService.MCU_UPDATING)) {
                pd = ProgressDialog.show(mContext, "MCU updating", "Do not turn off the power!")

            } else if (intent.action.equals(BackgroundUSBService.MCU_UPDATED)) {
                pd.dismiss()
                var dialog = AlertDialog.Builder(context)
                        .setTitle("MCU updated").setMessage("Connecting")
                        .show()

                Handler().postDelayed({ dialog.cancel() }, 1000)

            }
        }
    }

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "stopReceiver: " + intent.action.toString())
            android.os.Process.killProcess(android.os.Process.myPid());
            finishAffinity();
            System.exit(0);

        }
    }

    private fun showSettings() {
        startActivity(Intent("com.hzbhd.alexross.subarulan2.activities.SettingsActivity"))
    }

    override fun onNewIntent(intent: Intent) {

    }

    override fun onTouchEvent(touchevent: MotionEvent): Boolean {
        if (mDetector!!.onTouchEvent(touchevent)) {
            return true
        }
        return super.onTouchEvent(touchevent);
    }

    override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val distanceX = event2.getX() - event1.getX()
        val distanceY = event2.getY() - event1.getY()
        if (MyApplication.stModel.currentState != State.SOUNDSETTINGS)
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0) {
                    if (MyApplication.stModel.prevState == State.UNKNOWN || MyApplication.stModel.prevState == MyApplication.stModel.currentState) {
                        MyApplication.stModel.prevState = MyApplication.stModel.currentState
                        mViewFlipper!!.displayedChild = 1
                        MyApplication.stModel.currentState = State.CARINFO
                    }
                } else {
                    if (MyApplication.stModel.prevState != State.UNKNOWN) {
                        MyApplication.stModel.ChangeState(MyApplication.stModel.prevState)
                        MyApplication.stModel.prevState = State.UNKNOWN
                    }
                }
            }
        return true
    }

    override fun onShowPress(event: MotionEvent) {
        Log.d(TAG, "onShowPress: " + event.toString())
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        Log.d(TAG, "onSingleTapUp: " + event.toString())
        return true
    }

    fun onDoubleTap(event: MotionEvent): Boolean {
        Log.d(TAG, "onDoubleTap: " + event.toString())
        return true
    }

    override fun onDown(event: MotionEvent): Boolean {
        Log.d(TAG, "onDown: " + event.toString())
        return true
    }

    override fun onScroll(event1: MotionEvent, event2: MotionEvent, distanceX: Float,
                          distanceY: Float): Boolean {
        Log.d(TAG, "onScroll: " + event1.toString() + event2.toString())
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        Log.d(TAG, "onLongPress: " + event.toString())
    }


    private fun setVSeekbarWithIntervals() {
        (findViewById<View>(R.id.vseekbarWithIntervals_Bass) as VerticalSeekbarWithIntervals).setIntervals(SoundSettingsModel.getIntervals6())
        (findViewById<View>(R.id.vseekbarWithIntervals_Mid) as VerticalSeekbarWithIntervals).setIntervals(SoundSettingsModel.getIntervals6())
        (findViewById<View>(R.id.vseekbarWithIntervals_Treble) as VerticalSeekbarWithIntervals).setIntervals(SoundSettingsModel.getIntervals6())
        (findViewById<View>(R.id.vseekbarWithIntervals_Fade) as VerticalSeekbarWithIntervals).setIntervals(SoundSettingsModel.getIntervals9())
        (findViewById<View>(R.id.hseekbarWithIntervals_Balance) as HorizontalSeekbarWithIntervals).setIntervals(SoundSettingsModel.getIntervals9())
    }

    override fun onPause() {
        super.onPause()
        if (BuildConfig.DEBUG)
            Log.d(TAG, "onPause:")
        MyApplication.activityPaused()
        // EventBus.getDefault().unregister(this);
        //this.unregisterReceiver(testReceiver);
    }

    override fun onResume() {
        super.onResume()
        if (BuildConfig.DEBUG)
            Log.d(TAG, "onResume:")
        MyApplication.activityResumed()

        val intent = getIntent()
        if (intent != null) {
            Log.i(TAG, "onResume:" + intent.action)
           // if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                // val usbDevice = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE)
                // findSerialPortDevice();
                // Create a new intent and put the usb device in as an extra
                // val broadcastIntent = Intent(ACTION_USB_DEVICE_ATTACHED)
                //  broadcastIntent.putExtra(UsbManager.EXTRA_DEVICE, usbDevice)
                // Broadcast this event so we can receive it
                //  sendBroadcast(broadcastIntent)
           // }
        }
    }

    private fun GetProcess(): AndroidAppProcess? {
        val processes = AndroidProcesses.getRunningAppProcesses()
        for (process in processes) {
            if (process.name.equals("com.hzbhd.alexross.subarulan2:service", ignoreCase = true)) {
                Log.d(TAG, "Process foregraund:" + process.foreground)
                return process
            }
        }
        return null
    }

    override fun onStop() {
        // call the superclass method first
        super.onStop()
        //  BackgroundUSBService.StopUSBService(this);
    }

    override fun onDestroy() {
        // call the superclass method first
        super.onDestroy()
        //  BackgroundUSBService.StopUSBService(this);
        unregisterReceiver(stopReceiver)
        unregisterReceiver(mcuReceiver)
    }



}
