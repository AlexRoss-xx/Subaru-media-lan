/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import com.nwd.kernel.source.DeviceState

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import android.hardware.usb.UsbDevice
import android.os.IBinder
import android.hardware.usb.UsbManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import com.hzbhd.midware.audio.AudioSourceManager
import com.hzbhd.midware.audio.asp.AspControl.AspData.DataDao
import com.hzbhd.midware.audio.asp.AspControl.AspService
import com.hzbhd.midware.audio.asp.AspControl.MPUCtrlAspAdapter
import com.hzbhd.midware.audio.asp.AspControl.PT2312AspService

import com.nwd.kernel.utils.KernelConstant.ACTION_MCU_STATE_CHANGE
import com.nwd.kernel.utils.KernelConstant.EXTRA_MCU_STATE
import com.hzbhd.midware.constant.channelManagerDef
import com.hzbhd.midware.sourceManager.AppSwitcher
import com.hzbhd.midware.sourceManager.SourceSwitchService
import com.hzbhd.midwareproxy.misc.MiscOperation
import com.hzbhd.midwareproxy.setting.SettingManager
import com.hzbhd.midwareproxy.sourceswitch.SourceSwitchManager
import java.lang.Exception

class StartServiceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = StartServiceBroadcastReceiver::class.java.simpleName

    val ACTION_AFTER_BOOT_COMPLETED = "com.nwd.ACTION_AFTER_BOOT_COMPLETED"
    val ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"

    private var file: BufferedWriter? = null

    val b1: Byte = 1
    val b2: Byte = 2
    val b3: Byte = 3

    companion object {
        var booted = false
        var mcuState: Byte = 0
    }

    override fun onReceive(context: Context, intent: Intent) {
        //android.os.Debug.waitForDebugger();
        val action = intent.action
        Log.v(TAG, "action:" + action!!)
        // write("action:" + action!!);

        if (ACTION_BOOT_COMPLETED.equals(action, ignoreCase = true)/* || (Intent.ACTION_USER_PRESENT.equalsIgnoreCase(action))*/) {
            booted = true;
            onPowerOn(context)
            // onPowerOn(context);
            /* if(!booted) {
               booted = true
               val startServiceIntent = Intent(context, BackgroundUSBService::class.java)
               startServiceIntent.action = BackgroundUSBService.ACTION_START
               //    context.startService(startServiceIntent)

               val intent = Intent(context, MainActivity::class.java)

               intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
               intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
               val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
               // val i = Intent("com.hzbhd.alexross.subarulan2", MainActivity::class.java)
               //i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
               pendingIntent.send()
           }*/

        } else if (ACTION_AFTER_BOOT_COMPLETED.equals(action, ignoreCase = true)/* || (Intent.ACTION_USER_PRESENT.equalsIgnoreCase(action))*/) {
            //onPowerOn(context);
            /* booted = true
             val startServiceIntent = Intent(context, BackgroundUSBService::class.java)
             startServiceIntent.action = BackgroundUSBService.ACTION_START
             //    context.startService(startServiceIntent)

             val intent = Intent(context, MainActivity::class.java)

             intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
             val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
             // val i = Intent("com.hzbhd.alexross.subarulan2", MainActivity::class.java)
             //i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
             pendingIntent.send()*/
        } else if (action.equals("android.intent.action.BOOT_COMPLETED_standy", ignoreCase = true)) {
            onPowerOn(context)
        } else if (action.equals("com.device.poweroff", ignoreCase = true)) {
            onPowerOff(context,3)
        }
        else if (action.equals("com.hzbhd.action.sourceUIfinish", ignoreCase = true)) {
        }

    }


    private fun onPowerOff(context: Context,state:Byte) {
        try {
            var msc=  MiscOperation.getMcuOperationInstant()
            msc.connectService();
           // msc.setVolumeVal(1)
            Log.d(TAG, "isPowerOffStatus:" +MiscOperation.isPowerOffStatus().toString())
            Log.d(TAG, "onPowerOff:" + state)
        }
        catch ( ex:Exception){
            ex.printStackTrace()
        }

        if (state == b3 && state!=mcuState) {
           // SourceSwitchManager.getSourceSwitchManager().audioChannelRequest(channelManagerDef.MEDIA_SOURCE_ID.NORMAL_SOURCE, channelManagerDef.SEAT_TYPE.front_seat, null);
           // SourceSwitchManager.getSourceSwitchManager().audioChannelRequest(channelManagerDef.MEDIA_SOURCE_ID.NORMAL_SOURCE, channelManagerDef.SEAT_TYPE.back_seat, null);

            Log.i(TAG, "power off:"+mcuState)
            booted = false

            val intent = Intent(context, BackgroundUSBService::class.java)
            intent.action = BackgroundUSBService.ACTION_STOP
            context.sendBroadcast(intent)
            // context.stopService(intent)
            context.startService(intent)
            //val intent2 = Intent(BackgroundUSBService.ACTION_STOP)
            // context.sendBroadcast(intent2)
            mcuState = 3;
            // BackgroundUSBService.StopService(context)

        }
    }

    private fun onPowerOn(context: Context) {
        Log.i(TAG, "onPowerOn:" + mcuState)
        if (mcuState != b1) {
           // switch to normal (disable radio)
           // SourceSwitchManager.getSourceSwitchManager().audioChannelRequest(channelManagerDef.MEDIA_SOURCE_ID.NORMAL_SOURCE, channelManagerDef.SEAT_TYPE.front_seat, null as Bundle?)

            mcuState = 1
            Log.i(TAG, "power on booted:" + booted)
            Log.i(TAG, "power on mcuState:" + mcuState)
            // if(!booted) {
            // Thread.sleep(10)
            val startServiceIntent = Intent(context, BackgroundUSBService::class.java)
            startServiceIntent.action = BackgroundUSBService.ACTION_START
            context.startService(startServiceIntent)
            // BackgroundUSBService.SendRESETCommand(context)
            Thread.sleep(1000)

            //SourceSwitchManager.getSourceSwitchManager().audioChannelRequest(channelManagerDef.MEDIA_SOURCE_ID.NORMAL_SOURCE, channelManagerDef.SEAT_TYPE.front_seat, null as Bundle?)

            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP

            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            pendingIntent.send()

            val launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.hzbhd.alexross.subarulan2")
            if (launchIntent != null) {
                context.startActivity(launchIntent)//null pointer check in case package name was not found
            }
            // val launchIntentRadio = context.getPackageManager().getLaunchIntentForPackage("com.hzbhd.radio")
            // if (launchIntentRadio != null) {
            //    context.startActivity(launchIntentRadio)//null pointer check in case package name was not found
            //}
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            if (sharedPref.getBoolean("pref_show_home_enable", false)) {
                val intent = Intent("android.intent.action.MAIN")
                // intent.putExtra("extra_key_value", 20.toByte())
                // intent.putExtra("extra_key_type", 0.toByte())
                context.sendBroadcast(intent)
            }

            if (sharedPref.getBoolean("pref_connect_launch_enable", false)) {
                var toRun = sharedPref.getString("pref_connect_package_name", "")
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "toRun: " + toRun)
                if (toRun != null) {
                    if (toRun.length > 0) {
                        val launchIntentRadio = context.getPackageManager().getLaunchIntentForPackage(toRun)
                        Log.i(TAG, "toRun: " + launchIntentRadio)
                        if (launchIntentRadio != null) {
                            context.startActivity(launchIntentRadio)//null pointer check in case package name was not found
                        }
                    }
                }
            }
        }
    }
}

class SoundSourceBroadcastReceiver : BroadcastReceiver() {
    private val TAG = SoundSourceBroadcastReceiver::class.java.simpleName

    companion object {
        val  currentPlayingApp:String
        get()=  SourceSwitchManager.getSourceSwitchManager().getCurrentValidSource(channelManagerDef.SEAT_TYPE.front_seat).name
          var PlayState:Boolean= false
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, intent.action)
        val extras = intent.extras
        if (extras != null) {
            val keys = extras.keySet()
            for (key in keys) {
                val obj = intent.extras!!.get(key)

                if (BuildConfig.DEBUG)
                    Log.d(TAG, " Intent:" + intent.toString() + " key:" + key + ":" + obj!!.toString())
            }
        }

        if (intent.action!!.equals("APP_REQUEST_START_ACTION", ignoreCase = true)) {
            if (intent.getStringExtra("ActionName")!!.equals("com.hzbhd.intent.action.btaudio")) {
               // currentPlayingApp=6
            }else  if (intent.getStringExtra("ActionName")!!.equals("com.hzbhd.intent.action.aux1")) {
                //currentPlayingApp=5
            }else  if (intent.getStringExtra("ActionName")!!.equals("com.hzbhd.intent.action.video")) {
               // currentPlayingApp=4
            }else  if (intent.getStringExtra("ActionName")!!.equals("com.hzbhd.intent.action.btuiphone")) {
                //currentPlayingApp=3
            }else  if (intent.getStringExtra("ActionName")!!.equals("com.hzbhd.intent.action.fm")) {
                //currentPlayingApp=1
            }else  if (intent.getStringExtra("ActionName")!!.equals("com.hzbhd.intent.action.music")) {
               // currentPlayingApp=2
            }
        }
        else  if (intent.action!!.equals("android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION", ignoreCase = true)) {
            //currentPlayingApp=10
        }
        else if(intent.action!!.equals("com.hzbhd.action.sourcerealchange")) {
            if ((intent.getStringExtra("toSourceID")!!.equals("NORMAL_SOURCE")) ) {
               // currentPlayingApp = 10
            }
             if((intent.getStringExtra("fromSourceID")!!.equals("SLEEP")) )  {
                 SourceSwitchManager.getSourceSwitchManager().audioChannelRequest(channelManagerDef.MEDIA_SOURCE_ID.NORMAL_SOURCE, channelManagerDef.SEAT_TYPE.front_seat, null as Bundle?)
            }
        }
        else if(intent.action!!.equals("playstate.change.action")) {
                PlayState = intent.getBooleanExtra("PlayState",false);

        }
        else  if (intent.action!!.equals("CLOSE_AUDIO_EFFECT_CONTROL_SESSION", ignoreCase = true)) {
           // currentPlayingApp=0
        }
        else if (intent.action!!.equals("com.hzbhd.android.MCU_KEY_IND_ACTION", ignoreCase = true)) {
            if(intent.getStringExtra("key_value")!!.equals("EJECT")) {
                val launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.hzbhd.alexross.subarulan2")
                if (launchIntent != null) {
                    context.startActivity(launchIntent)//null pointer check in case package name was not found
                }
            }
        }

        Log.i(TAG, "currentPlayingApp:"+currentPlayingApp)
    }
}