 /*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.analyzer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

import com.hzbhd.midware.constant.channelManagerDef;
import com.hzbhd.midwareproxy.radio.RadioManager;
import com.hzbhd.midwareproxy.sourceswitch.SourceSwitchManager;
import com.hzbhd.alexross.subarulan2.ApplicationConfig;
import com.hzbhd.alexross.subarulan2.BackgroundUSBService;
import com.hzbhd.alexross.subarulan2.BuildConfig;
import com.hzbhd.alexross.subarulan2.models.LanMessage;
import com.hzbhd.alexross.subarulan2.MyApplication;
import com.hzbhd.alexross.subarulan2.SoundSourceBroadcastReceiver;
import com.hzbhd.alexross.subarulan2.models.State;
import com.hzbhd.alexross.subarulan2.models.StateModel;

import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;

/**
 * Created by Alexey Rasskazov
 */

public class MessageAnalyzer {
    final protected static char[] encoding = "0123456789ABCDEF".toCharArray();
    private static final String TAG = MessageAnalyzer.class.getSimpleName();
    private StateModel stModel;
    private boolean mcuVersionRecived = false;
    private boolean versionSend = false;
    private boolean isKeySwitch = false;

    final Handler handler = new Handler();

    public MessageAnalyzer(StateModel stmodel) {
        this.stModel = stmodel;
    }

    public void ProcessMessage(LanMessage message) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, message.toString());

        switch (message.T) {
            case CAN:
                ProcessCanMessage(message);
                break;
            case LAN:
                ProcessLanMessage(message);
                break;
            case MCU:
                ProcessMCUMessage(message);
                break;
            default:
                break;
        }
    }

    public void ProcessLanMessage(LanMessage message) {
        switch (message.A) {
            case 0x140: //HeadUnit
            case 0x100:// CDCEmu
                ProcessHeadUnitMessage(message);
                break;
            case 0x260: // SAT
                ProcessSatMessage(message);
                break;
            case 0xFFF:
                ProcessBroadCastMessage(message);
                break;
            default:
                ProcessBroadCastMessage(message);
                break;
        }
    }

    public void ProcessCanMessage(LanMessage message) {
        stModel.getCanModel().processMessage(message);
        if (!mcuVersionRecived && !versionSend) {
            BackgroundUSBService.SendMCUCommand(MyApplication.Companion.getAppContext(), "V1");
            versionSend = true;
        }
    }

    public void ProcessMCUMessage(LanMessage message) {
        String vers = "";

        vers = String.format("%d.%d.%d.%d", message.M[0], message.M[1], message.M[2], message.M[3]);
        ApplicationConfig.Companion.setMcuVersion(vers);

        String id = "";
        for (int i = 4; i < message.M.length; i++) {
            id = id + String.format("%02X", message.M[i]) + "";
        }

        ApplicationConfig.Companion.setDevice(id);
        String str = "";
        mcuVersionRecived = true;
     }


    public void ProcessHeadUnitMessage(LanMessage message) {
        switch (message.M[0]) {
            case 0x00: {
                // BackgroundUSBService.SendLANCommand(MyApplication.Companion.getAppContext(), "R" + 2);
                break;
            }
            case 0x60:
            case 0x65: {
                //CD
                // 0  1  2  3  4  5  6  7  8  9 10 11
                //60 02 00 ff 00 32 00 00 00 00 01 bb bb 01 05 bb bb bb bb bb bb 00
                //60 02 00 ff 00 42 00 00 00 00 01 bb bb 01 05 bb bb bb bb bb bb 00
                //60 02 00 ff 00 51 00 00 00 00 01 bb bb 01 05 bb bb bb bb bb bb 00 cd removed ,waiting
                //60 02 00 00 00 41 00 00 00 00 01 bb bb 01 03 bb bb bb bb bb bb 00
                //60 02 00 00 00 41 00 00 00 00 01 bb bb 01 04 bb bb bb bb bb bb 00
                //60 02 00 00 00 31 00 00 00 00 01 bb bb 01 03 bb bb bb bb bb bb 00

                //60 02 01 01 01
                //60 02 01 03 00
                //60 02 01 03 01
                //60 02 01 04 01

                //60 02 00 ff 00 00 00 00 00 00 01 bb bb 01 03 bb bb bb bb bb bb 00
                //60 02 00 00 00 00 00 00 02 00 01 bb bb 01 03 bb bb bb bb bb bb 00
                //60 02 00 00 00 00 01 00 01 00 05 bb 01 01 03 bb bb bb bb bb bb 00
                //60 02 00 00 00 00 01 00 01 00 05 bb 01 01 03 b0 00 bb bb bb bb 00
                //60 02 00 00 00 00 01 00 01 00 05 bb 01 01 03 b0 01 bb bb bb bb 00


                //60 02 01 03 03 13 72 57 00 ff 00 ff 00

                //60 02 00 00 00 00 01 00 01 00 15 bb 01 01 01 bb bb bb bb bb bb 00 bb bb 01 01 00

                switch (message.M[1]) {
                    case 0x12:
                    case 0x02:
                        stModel.getCdChangerModel().process(message);
                        break;

                    case 0x22:
                        stModel.getRSECDCModel().process(message);
                        break;

                    case 0x06:
                    case 0x07:
                        //TUNER
                        // 0  1  2  3  4  5  6  7  8  9 10 11
                        //60 06 00 00 00 03 00 01 01 BB BB BB BB 99 5B 0B FF   radio FM
                        //60 06 00 00 00 03 00 00 10 BB BB BB BB 53 0B 01 FF   radio AM
                        //60 06 02 11 02 BB 87 9B BB 87 9B BB 90 1B BB 98 1B B1 06 1B B1 07 9B  fm stations
                        stModel.getTunerModel().process(message);
                        break;

                    case 0x83:
                        stModel.getSoundSettings().process(message);
                        break;

                    default:
                        break;
                }
                break;
            }
            default:
                break;
        }
    }

    public void ProcessBroadCastMessage(LanMessage message) {
        switch (message.M[0]) {
            case 0x50:
                if ((ApplicationConfig.Companion.getRsecdcEmulation() && stModel.getCurrentState() == State.RSECDC) || (stModel.getCurrentState() == State.SAT)) {
                    //0   1  2  3  4  5  6  7
                    //seek up
                    //50 01 22 22 01 05 00 00
                    //50 04 22 22 01 05 00 00
                    //seek down
                    //50 01 22 22 02 06 00 00
                    //50 04 22 22 02 06 00 00
                    //50 01 42 42 01 01 00 00
                    if (message.M[1] == 0x04 || message.M[1] == 0x01)
                        if (BuildConfig.DEBUG)
                            Log.i(TAG,"isKeySwitch"+ isKeySwitch);
                        if (!isKeySwitch)
                            switch (message.M[5]) {
                                case 0x01:
                                case 0x05:
                                    isKeySwitch = true;
                                    if (MyApplication.Companion.getIncomingCall()) {
                                        Intent mNextIntent = new Intent("hzbhd_can_keypress");
                                        mNextIntent.putExtra("value", 14);
                                        MyApplication.Companion.getAppContext().sendBroadcast(mNextIntent);
                                    } else {
                                        channelManagerDef.MEDIA_SOURCE_ID sourceID = SourceSwitchManager.getSourceSwitchManager().getCurrentValidSource(channelManagerDef.SEAT_TYPE.front_seat);
                                        if (sourceID == channelManagerDef.MEDIA_SOURCE_ID.FM) {
                                            try {
                                                RadioManager.getRadioManager().seekUp();
                                            } catch (RemoteException e) {
                                                e.printStackTrace();
                                            }
                                        } else if ((sourceID == channelManagerDef.MEDIA_SOURCE_ID.MUSIC | sourceID == channelManagerDef.MEDIA_SOURCE_ID.VIDEO | sourceID == channelManagerDef.MEDIA_SOURCE_ID.BTAUDIO) & SoundSourceBroadcastReceiver.Companion.getPlayState()) {
                                            Intent mNextIntent = new Intent("hzbhd_can_keypress");
                                            mNextIntent.putExtra("value", 20);
                                            MyApplication.Companion.getAppContext().sendBroadcast(mNextIntent);
                                        } else {
                                            System.out.println("ACTION_MEDIA_BUTTON");
                                            Intent mNextIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                                            mNextIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KEYCODE_MEDIA_NEXT));
                                            //   MyApplication.Companion.getAppContext().sendBroadcast(mNextIntent);
                                            @SuppressLint("WrongConstant") AudioManager audioManager = (AudioManager) MyApplication.Companion.getAppContext().getSystemService("audio");
                                            long uptimeMillis = SystemClock.uptimeMillis() - 1;
                                            long j = uptimeMillis;
                                            long j2 = uptimeMillis;
                                            int i2 = KEYCODE_MEDIA_NEXT;
                                            KeyEvent keyEvent = new KeyEvent(j, j2, 0, i2, 0);
                                            KeyEvent keyEvent2 = new KeyEvent(j, j2, 1, i2, 0);
                                            audioManager.dispatchMediaKeyEvent(keyEvent);
                                            audioManager.dispatchMediaKeyEvent(keyEvent2);
                                        }
                                    }

                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            isKeySwitch=false;
                                        }
                                    }, 200);
                                    break;

                                case 0x02:
                                case 0x06:
                                    isKeySwitch = true;
                                    if (MyApplication.Companion.getIncomingCall()) {
                                        Intent mNextIntent = new Intent("hzbhd_can_keypress");
                                        mNextIntent.putExtra("value", 15);
                                        MyApplication.Companion.getAppContext().sendBroadcast(mNextIntent);
                                    } else {
                                        channelManagerDef.MEDIA_SOURCE_ID sourceID = SourceSwitchManager.getSourceSwitchManager().getCurrentValidSource(channelManagerDef.SEAT_TYPE.front_seat);
                                        if (sourceID == channelManagerDef.MEDIA_SOURCE_ID.FM) {
                                            try {
                                                RadioManager.getRadioManager().seekDown();
                                            } catch (RemoteException e) {
                                                e.printStackTrace();
                                            }
                                        } else if ((sourceID == channelManagerDef.MEDIA_SOURCE_ID.MUSIC | sourceID == channelManagerDef.MEDIA_SOURCE_ID.VIDEO | sourceID == channelManagerDef.MEDIA_SOURCE_ID.BTAUDIO) & SoundSourceBroadcastReceiver.Companion.getPlayState()) {
                                            Intent mNextIntent = new Intent("hzbhd_can_keypress");
                                            mNextIntent.putExtra("value", 21);
                                            MyApplication.Companion.getAppContext().sendBroadcast(mNextIntent);//
                                        } else {
                                            Intent mNextIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                                            mNextIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KEYCODE_MEDIA_PREVIOUS));
                                            //   MyApplication.Companion.getAppContext().sendBroadcast(mNextIntent);
                                            @SuppressLint("WrongConstant") AudioManager audioManager = (AudioManager) MyApplication.Companion.getAppContext().getSystemService("audio");
                                            long uptimeMillis = SystemClock.uptimeMillis() - 1;
                                            long j = uptimeMillis;
                                            long j2 = uptimeMillis;
                                            int i2 = KEYCODE_MEDIA_PREVIOUS;
                                            KeyEvent keyEvent = new KeyEvent(j, j2, 0, i2, 0);
                                            KeyEvent keyEvent2 = new KeyEvent(j, j2, 1, i2, 0);
                                            audioManager.dispatchMediaKeyEvent(keyEvent);
                                            audioManager.dispatchMediaKeyEvent(keyEvent2);
                                        }
                                    }
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            isKeySwitch=false;
                                        }
                                    }, 200);
                                    break;
                            }
                }
                break;
            case 0x60:
                switch (message.M[1]) {
                    case 0xC0: {
                        //MODE
                        // 0  1  2  3  4  5  6  7  8  9 10 11
                        //60 C0 00 CF FF 00 00 02 00 02 00 00 -CD
                        //60 C0 00 4F FF 00 00 06 00 06 00 00 -FM
                        //60 C0 00 4F FF 00 00 0B 00 0B 00 00 -RSE
                        //60 C0 00 4F FF 00 00 1B 00 1B 00 00 -RSE
                        //60 C0 00 4F FF 00 00 42 00 42 00 00 -SAT
                        //60 C0 00 CF FF 00 04 06 00 06 00 00 -FM-tonebal
                        //60 C0 00 CF FF 00 04 02 00 02 00 00 -CD tone
                        //60 C0 00 CF FF 00 00 02 00 02 00 00 -end tone
                        //60 C0 00 4F 85 00 00 00 00 00 00 00 -audio off
                        //60 C0 00 0E FF 00 00 42 00 42 00 00
                        //60 C0 00 4F FF 00 00 02 00 02 00 00
                        //60 C0 00 4F FF 00 00 07 00 07 00 00
                        //60 C0 00 4F FF 00 00 22 00 22 00 00
                        //60 C0 00 4F FF 00 00 00 00 00 00 00
                        //60 C0 00 4F FF 00 00 07 00 07 00 00  radio
                        switch (message.M[2]) {
                            case 0x00: {
                                if (BuildConfig.DEBUG)
                                    Log.i(TAG, "Mode:" + message.toString());
                                switch (message.M[3]) {
                                    case 0x0E:
                                    case 0xCF:
                                    case 0x47:
                                    case 0x4F: {
                                        switch (message.M[6]) {
                                            case 0x00: {
                                                if ((message.M[7] == 0x02 && message.M[9] == 0x02) || (message.M[7] == 0x12 && message.M[9] == 0x12)) {          //CD
                                                    stModel.setRadioOn(true);
                                                    stModel.ChangeState(State.CD);
                                                } else if ((message.M[7] == 0x06 && message.M[9] == 0x06) || (message.M[7] == 0x07 && message.M[9] == 0x07)) {   //FM
                                                    stModel.setRadioOn(true);
                                                    stModel.ChangeState(State.TUNER);
                                                } else if ((message.M[7] == 0x0B && message.M[9] == 0x0B) ||
                                                        (message.M[7] == 0x1B && message.M[9] == 0x1B) ||
                                                        (message.M[7] == 0x2B && message.M[9] == 0x2B)) {   //RSE
                                                    stModel.setRadioOn(true);
                                                    stModel.ChangeState(State.RSE);
                                                } else if ((message.M[7] == 0x22 && message.M[9] == 0x22)) {   //RSE 2
                                                    stModel.setRadioOn(true);
                                                    stModel.ChangeState(State.RSECDC);
                                                } else if (message.M[7] == 0x42 && message.M[9] == 0x42) {   //SAT
                                                    stModel.setRadioOn(true);
                                                    stModel.ChangeState(State.SAT);
                                                } else if (message.M[7] == 0x00 && message.M[9] == 0x00) {
                                                    stModel.setRadioOn(false);
                                                    stModel.ChangeState(State.OFF);
                                                }
                                                break;
                                            }
                                            case 0x04: {
                                                stModel.setRadioOn(true);
                                                stModel.ChangeState(State.SOUNDSETTINGS);
                                                break;
                                            }
                                            default:
                                                break;
                                        }
                                        break;
                                    }
                                    default:
                                        break;
                                }
                                break;
                            }
                            default:
                                break;
                        }
                        break;
                    }
                    default:
                        break;
                }
        }
    }

    public void ProcessSatMessage(LanMessage message) {
        switch (message.A) {
            default:
                break;
        }
    }
}
