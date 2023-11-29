/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.models

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.hzbhd.alexross.subarulan2.*
import java.lang.Exception
import java.util.*
import kotlin.math.floor

// CAN Sate
class CANModel(stateModel: StateModel) : BaseObservable() {
    private val TAG = CANModel::class.java.simpleName

    private var cQueue: LinkedList<Int> = LinkedList<Int>()
    private var cQueue_FC: LinkedList<Int> = LinkedList<Int>()

    var soundSettings: SoundSettingsModel = stateModel.soundSettings

    var currentMileage: Int = 0
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.currentMileage)
        }

    var currentTime: String = ""
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.currentTime)
        }

    var assistColor: Int = Color.parseColor("#FFFFFF")
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.assistColor)
        }
    /**
     *
     *///id 040 8 80 55 00 82 00 5F 47 00   55  or 45  trip A or B
    var tripMode: TRIPMODE = TRIPMODE.A
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.tripMode)
            }
        }

    var sheduler: Boolean = false
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.sheduler)
        }

    var sheduler_interval: Int = 8000
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.sheduler_interval)
        }

    var fuelConsamptionEnable: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.fuelConsamptionEnable)
            }
        }

    var fuelConsumption: Int = 0
        @Bindable get() = field
        set(value) {
            if (value != field) {
                if (cQueue.size == 3) {
                    cQueue.removeFirst()
                    cQueue.addLast(value)
                } else
                    cQueue.addLast(value)

                var vs: Float = 0f
                for (v in cQueue) {
                    vs += v
                }

                field = (vs / cQueue.size).toInt()
                notifyPropertyChanged(BR.fuelConsumption)
                // MyApplication.repository.insertFuelConsumption(avgFC)
                // notifyPropertyChanged(BR.records)
                // MyApplication.application.toastNotify()
            }

            if (cQueue_FC.size == 200) {
                cQueue_FC.removeFirst()
                cQueue_FC.addLast(value)
            } else
                cQueue_FC.addLast(value)

        }

    var records: List<fcData>
        @Bindable get() =
            MyApplication.repository.getFuelConsumption()
        set(value) {
        }

    var fuelConsamptionChartEnable: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.fuelConsamptionChartEnable)
            }
        }

    var fcChartType: Int = 0
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.fcChartType)
            }
        }

    var showFCChart: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.showFCChart)
            }
        }

    var avgFuelConsumptionA: Float = 0f
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.avgFuelConsumptionA)
                MyApplication.application?.toastNotify()
            }
        }

    var avgFuelConsumptionB: Float = 0f
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.avgFuelConsumptionB)
                MyApplication.application.toastNotify()
            }
        }

    var fuelCorrection: Float = 1.53f
        get() = field
        set(value) {
            if (value != field) {
                field = value
            }
        }

    var tempCorrection: Float = 0f
        get() = field
        set(value) {
            if (value != field) {
                field = value
            }
        }

    var temperature: String = "-"
        @Bindable get() = field
        set(value) {
            MyApplication.application.assistNotify()

            if (value != field) {
                field = value
                notifyPropertyChanged(BR.temperature)
                MyApplication.application.toastNotify()
            }
        }

    var engine_temperature_value: Int = 0
        @Bindable get() = field
        set(value) {
            MyApplication.application.assistNotify()
            if (value != field) {
                field = value
                engine_temperature = engine_temperature_value.toString()
                notifyPropertyChanged(BR.engine_temperature_value)
            }
            if (engine_temperature_value >= engine_temperature_warning_value) {
                engine_temperatureWarning = true
            } else {
                engine_temperatureWarning = false
            }
        }

    var engine_temperature_warning_value: Int = 0
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.engine_temperature_warning_value)
            }
        }

    /**
     *
     */
    var engine_temperature: String = ""
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.engine_temperature)
            }
        }

    var engine_temperatureWarning: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.engine_temperatureWarning)
            }
            if (value) {
                MyApplication.application.toastNotify()
                MyApplication.soundNotify()
            }
        }

    var engine_temperatureEnable: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.engine_temperatureEnable)
            }
        }

    var tireSignalIsActive: Boolean = false
    var tireSignal: Int = 0
        get() = field
        set(value) {
            // Log.e(TAG, "tireSignal: "+value);
            if (value != field) {
                if (value == 0) {
                    if (tireSignalIsActive)
                        tireAlert = true
                    else
                        tireSignalIsActive = true
                } else {
                    tireSignalIsActive = false
                    tireAlert = false
                }
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "tireSignal: " + value)
                field = value
            }
        }

    var tpmsEnable: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.tpmsEnable)
            }
        }


    var tireAlert: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.tireAlert)
                if (value)
                    MyApplication.soundNotify()

                MyApplication.application.toastNotify()
            }
        }

    var range: Int = 0
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.range)
                if (value < 100)
                    MyApplication.soundNotify()
                MyApplication.application.toastNotify()
            }
        }

    var lowFuel: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.lowFuel)
                MyApplication.soundNotify()
                MyApplication.application.toastNotify()
            }
        }


    // Keyless entry system programming
    var lightOn: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.lightOn)
            }
        }

    var beepOn: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.beepOn)
            }
        }

    var keyAlert: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.keyAlert)
            }
        }

    var roomLight: String = "Normal"
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.roomLight)
            }
        }


    /**
     *
     */
    var defogger: Boolean = false // false -15min  true -continues
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.defogger)
            }
        }


    /**
     *
     */
    var deicer: Boolean = false // false -15min  true -continues
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.deicer)
            }
        }


    //Alarm system and shock sensor
//ALARM
    var alarm: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.alarm)
            }
        }


    var delay: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.delay)
            }
        }


    var motorhours: Long = 0
        @Bindable get() =
            MyApplication.repository.vehicle().motorHours
        set(value) {

            field = value
            notifyPropertyChanged(BR.motorhours)
        }

    var motorhoursStr: String = ""
        @Bindable get() =
            "" + floor(MyApplication.repository.vehicle().motorHours / 60f).toInt() + " h: " + floor(MyApplication.repository.vehicle().motorHours % 60f).toInt() + " min"
        set(value) {
            field = value
            notifyPropertyChanged(BR.motorhoursStr)
        }

    var maintenance: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.maintenance)
            }
        }

    //SHOCK SENSOR
    var shockSensor: Int = 0
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.shockSensor)
            }
        }

    fun processMessage(message: LanMessage) {
        when (message.A) {
            /*     0x40 -> {

                     val x= if(ApplicationConfig.US) 0.293f else 0.13f
                     avgFuelConsumptionA = message.M[5] * x
                     avgFuelConsumptionB = message.M[6] * x
                     if (message.M[1] == 0x45)
                         tripMode = TRIPMODE.A
                     else
                         tripMode = TRIPMODE.B

                     val bits = BooleanArray(4)
                     for (i in 0..3) {
                         bits[4 - 1 - i] = 1 shl i and message.M[7] != 0
                     }
                     //low fuel
                     if (bits[3])
                         lowFuel = true
                     else
                         lowFuel = false

                 }
                 0x41 -> range = message.M[2] * 10

                 0x80 -> {

                     if (message.M[3] != 0xFE)
                         temperature = (message.M[3] / 2f - 40).toString()
                     else
                         temperature = "-"
                 }
                 0x82 -> {
                     val bits0 = BooleanArray(8)
                     for (i in 0..7) {
                         bits0[8 - 1 - i] = 1 shl i and message.M[0] != 0
                     }

                     //RoomLight
                     if (bits0[1] && bits0[2])
                         roomLight = "Long"
                     else if (!bits0[1] && !bits0[2])
                         roomLight = "0 sec"
                     else if (!bits0[1] && bits0[2])
                         roomLight = "Short"
                     else
                         roomLight = "Normal"

                     //Alarm
                     alarm = bits0[5]

                     //Defogger
                     defogger = bits0[6]


                     val bits1 = BooleanArray(8)
                     for (i in 0..7) {
                         bits1[8 - 1 - i] = 1 shl i and message.M[1] != 0
                     }

                     //Deicer
                     deicer = bits1[7]
                     beepOn = bits1[6]
                     lightOn = bits1[5]


                     val bits4 = BooleanArray(8)
                     for (i in 0..7) {
                         bits4[8 - 1 - i] = 1 shl i and message.M[4] != 0
                     }
                     keyAlert = bits4[6]
                     delay = bits4[7]
                 }
                 0x140 -> {
                     if (tpmsEnable)
                         tireSignal = message.M[0]
                 }*/
            0xfff -> {
                val c = Calendar.getInstance()
                val dateformat = java.text.SimpleDateFormat("HH:mm")
                val datetime = dateformat.format(c.time)

                currentTime = datetime

                val x = if (ApplicationConfig.US) 0.191f else 0.13f
                if (message.M[1] != 0xff) {
                    avgFuelConsumptionA = message.M[1] * x * fuelCorrection
                    avgFuelConsumptionB = message.M[2] * x * fuelCorrection

                    //mpg to L    do not use X    toad
                    //avgFuelConsumptionA = (525.58 / message.M[1] *  fuelCorrection).toFloat()
                    //avgFuelConsumptionB = (525.58 / message.M[2] *  fuelCorrection).toFloat()
                }
                // fuelConsumption =   (Random.nextInt(255)* x * fuelCorrection).toInt()
                if (message.M.size > 10 && message.M[11] > 0) {
                    fuelConsumption = (message.M[11] * x * fuelCorrection).toInt()
                    //mpg to L  do not use X
                    // fuelConsumption =(235.215 / message.M[11] *  fuelCorrection).toInt()
                } else
                    fuelConsumption = 0;

                //Range
                range = message.M[4] * 10
                //miles to km
                //range = (message.M[4] * 10* 1.603f).toInt() // us to eu

                val bitsTrip = BooleanArray(4)
                for (i in 4..7) {
                    bitsTrip[8 - 1 - i] = 1 shl i and message.M[0] != 0
                }

                if (bitsTrip[3])
                    tripMode = TRIPMODE.B
                else
                    tripMode = TRIPMODE.A

                val bits = BooleanArray(4)
                for (i in 0..3) {
                    bits[4 - 1 - i] = 1 shl i and message.M[3] != 0
                }
                //low fuel
                lowFuel = bits[3]

                //Temperature
                if (message.M[5] != 0xFE) {
                    temperature = String.format("%.1f", (message.M[5]) / 2f - 40 + tempCorrection)
                    if (!runnable.status)
                        startRepeatingTask();
                    if (!runnable_FC.status)
                        startFCTask();
                } else {
                    temperature = "-"
                    stopRepeatingTask()
                    stopFCTask()
                }

                // BUI SETTINGS
                val bits0 = BooleanArray(8)
                for (i in 0..7) {
                    bits0[8 - 1 - i] = 1 shl i and message.M[6] != 0
                }

                //RoomLight
                if (bits0[1] && bits0[2])
                    roomLight = "Long"
                else if (!bits0[1] && !bits0[2])
                    roomLight = "0 sec"
                else if (!bits0[1] && bits0[2])
                    roomLight = "Short"
                else
                    roomLight = "Normal"

                //Alarm
                alarm = bits0[5]

                //Defogger
                defogger = bits0[6]

                val bits1 = BooleanArray(8)
                for (i in 0..7) {
                    bits1[8 - 1 - i] = 1 shl i and message.M[7] != 0
                }

                //Deicer
                deicer = bits1[7]
                beepOn = bits1[6]
                lightOn = bits1[5]

                val bits4 = BooleanArray(8)
                for (i in 0..7) {
                    bits4[8 - 1 - i] = 1 shl i and message.M[8] != 0
                }
                keyAlert = bits4[6]
                delay = bits4[7]

                if (tpmsEnable)
                    tireSignal = message.M[9]

                if (message.M.size > 9)
                    if (MyApplication.engineTempWarning) {
                        if (message.M[10] != 0xFE) {
                            engine_temperature_value = (message.M[10] - 40)
                        } else {
                            engine_temperatureWarning = false
                            engine_temperature = ""
                        }
                    }
            }
        }
    }

    fun onClickSetting(view: View) {
        MyApplication.stModel.ChangeState(State.CARINFO)
    }

    fun onRealConsumptionClick(view: View) {
       showFCChart = !showFCChart
    }


    fun onClickLight(view: View) {
        BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "C" + 0x28.toString())
    }

    fun onClickShowInfo(view: View) {
        MyApplication.stModel.infoWindowDisable = !MyApplication.stModel.infoWindowDisable
    }

    fun onClickBeep(view: View) {
        // Log.i(TAG,"Click BEEP")
        // MyApplication.soundNotify()
        // var ACTION_KEY_VALUE = "com.nwd.action.ACTION_KEY_VALUE"
        // val mNextIntent: Intent = Intent(Intent.ACTION_MEDIA_BUTTON);
        // mNextIntent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_MEDIA_NEXT))

        val EXTRA_KEY_VALUE = "extra_key_value";
        val EXTRA_KEY_TYPE = "extra_key_type"

        // mNextIntent.putExtra("extra_key_value", 5.toByte());
        // mNextIntent.putExtra(EXTRA_KEY_VALUE, 5.toByte())
        // mNextIntent.putExtra(EXTRA_KEY_TYPE, 0.toByte())
        // MyApplication.appContext!!.sendBroadcast(mNextIntent);exc

        // val mNextIntent = Intent("hzbhd_can_keypress")
        // mNextIntent.putExtra("value", 20)
        // mNextIntent.putExtra(EXTRA_KEY_TYPE, 2.toByte())
        // MyApplication.appContext!!.sendBroadcast(mNextIntent)

        BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "C" + 0x27.toString())
    }


    fun onClickKeyAlert(view: View) {
        BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "C" + 0x2D.toString())
    }


    fun onClickRoomLightUp(view: View) {
        BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "C" + 0x24.toString())
    }

    fun onClickRoomLightDown(view: View) {
        BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "C" + 0x25.toString())
    }


    fun onClickDefogger(view: View) {
        BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "C" + 0x20.toString())
    }


    fun onClickDeicer(view: View) {
        BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "C" + 0x26.toString())
    }

    fun onClickAlarm(view: View) {
        BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "C" + 0x21.toString())
    }

    fun onClickDelay(view: View) {
        BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "C" + 0x2C.toString())
    }

    fun onClickReset(view: View) {
        BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "C" + 0x30.toString())
        BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "C" + 0x31.toString())
    }

    fun onClickResetMCU(view: View) {
        BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "RESET" )

    }


    fun onClickMaintenency(view: View) {
        val multiChoiceItems = MyApplication.appContext!!.resources.getStringArray(R.array.serv)
        val checkedItems = booleanArrayOf(false, false, false)
        AlertDialog.Builder(view.context)
                .setTitle("Select replaced parts ")
                .setMultiChoiceItems(multiChoiceItems, checkedItems, DialogInterface.OnMultiChoiceClickListener { dialog, index, isChecked -> Log.d("MainActivity", "clicked item index is $index") })
                .setPositiveButton("Ok") { dialog, i ->
                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.appContext)

                    with(sharedPref.edit()) {
                        putInt("lastMaintenance", currentMileage)
                        commit()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
    }

    fun onClickUpdate(view: View) {
        AlertDialog.Builder(view.context)
                .setTitle("Update MCU")
                .setPositiveButton("Ok") { dialog, i ->
                    BackgroundUSBService.UpdateMCU(MyApplication.appContext);
                }
                .setNegativeButton("Cancel", null)
                .show()
    }


    private val INTERVAL: Long = 1000 * 60 * 1 //2 minutes
    private val handler = android.os.Handler()
    private val runnable = object : Runnable {
        var status: Boolean = false
        var isFirstRun =true;
        override fun run() {
            status = true
            if (!isFirstRun) {
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "Motor hours: " + MyApplication.repository.vehicle().motorHours)

                if (sheduler)
                    MyApplication.repository.update(1)

                maintenance = MyApplication.repository.vehicle().motorHours >= (sheduler_interval * 60)

                notifyPropertyChanged(BR.motorhours)
                notifyPropertyChanged(BR.motorhoursStr)
            }
                handler.postDelayed(this, INTERVAL)

        }
    }

    fun startRepeatingTask() {
        runnable.run()
    }

    fun stopRepeatingTask() {
        runnable.status = false;
        handler.removeCallbacks(runnable)
    }


    private val INTERVAL_FC: Long = 1000 * 10 * 1 //2 minutes
    private val handler_FC = android.os.Handler()
    private val runnable_FC = object : Runnable {
        var status: Boolean = false
        var isFirstRun = true;
        override fun run() {
            if (BuildConfig.DEBUG)
                Log.i(TAG, "runnable_FC: ")

            status = true
            if (!isFirstRun) {
            try {
                    var avgFC: Float = 0f
                    var _fc: Long = 0
                    for (v in cQueue_FC) {
                        _fc += v
                    }
                    if (cQueue_FC.size > 0)
                        avgFC = (_fc / cQueue_FC.size) * 1.0f
                    MyApplication.repository.insertFuelConsumption(avgFC)
                    if (BuildConfig.DEBUG)
                        Log.i(TAG, "runnable_FC: " + avgFC)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                notifyPropertyChanged(BR.records)
            }
            else
                isFirstRun=false

            handler_FC.postDelayed(this, INTERVAL_FC)
        }
    }

    fun startFCTask() {
        runnable_FC.run()
    }

    fun stopFCTask() {
        runnable_FC.status = false;
        handler_FC.removeCallbacks(runnable_FC)
    }
}

enum class ROOMLIGHT {
    NONE,
    SHORT,
    NORMAL,
    LONG
}

enum class TRIPMODE {
    A,
    B,
}

