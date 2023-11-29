/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import android.util.Log
import com.hzbhd.alexross.subarulan2.BR
import com.hzbhd.alexross.subarulan2.BuildConfig
import com.hzbhd.alexross.subarulan2.MyApplication
import java.util.ArrayList

/**
 * Created by Alexey Rasskazov.
 */
public class SoundSettingsModel(stateModel: StateModel) : BaseObservable() {
    private val TAG = SoundSettingsModel::class.java.simpleName
    var state: StateModel = stateModel

    companion object {
        fun getIntervals6(): List<String> {
            return object : ArrayList<String>() {
                init {
                    add("-6")
                    add("-5")
                    add("-4")
                    add("-3")
                    add("-2")
                    add("-1")
                    add("0")
                    add("1")
                    add("2")
                    add("3")
                    add("4")
                    add("5")
                    add("6")
                }
            }
        }

        fun getIntervals9(): List<String> {
            return object : ArrayList<String>() {
                init {
                    add("-9")
                    add("-8")
                    add("-7")
                    add("-6")
                    add("-5")
                    add("-4")
                    add("-3")
                    add("-2")
                    add("-1")
                    add("0")
                    add("1")
                    add("2")
                    add("3")
                    add("4")
                    add("5")
                    add("6")
                    add("7")
                    add("8")
                    add("9")
                }
            }
        }
    }


    var active: Int = 0
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.active)
        }

    var activeString: String = ""
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.activeString)
        }


    var mute: Boolean = false
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.mute)
            MyApplication.application.toastNotify()
        }

    var volume: Int = 0
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.volume)
            MyApplication.application.toastNotify()

           // AudioSourceManager.getManager().setVolumeValue(volume);
        }

    var bass: Int = 0
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.bass)
        }

    var mid: Int = 0
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.mid)
        }

    var treble: Int = 0
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.treble)
        }

    var balance: Int = 0
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.balance)
        }

    var fade: Int = 0
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.fade)
        }


    //  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16
    //                    |vol     |mid
    //                 |act  |bass    |treble
    // 60 83 00 00 00 04 04 03 03 02 00 00 00 00
    // 60 83 00 00 00 01 04 02 02 00 00 00 00 00
    // 60 83 00 00 00 01 04 03 02 00 00 00 00 00
    // 60 83 00 00 00 02 04 03 02 00 00 00 00 00
    // 60 83 00 00 00 03 04 03 03 02 00 00 00 00
    // 60 83 00 00 00 05 04 03 03 02 00 00 00 00
    // 60 83 00 00 00 00 05 03 03 02 00 00 00 00-vol
    //
    //              |?
    // 60 83 00 00 02 00 00 03 03 02 00 00 00 00 mute
    // 60 83 00 00 02 00 03 03 03 02 00 00 00 00 unmute

    fun process(message: LanMessage) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, message.toString())

        volume = if (message.M[6] > 0x80) (message.M[6] - 0x80) * -1 else message.M[6]
        bass = if (message.M[7] > 0x80) (message.M[7] - 0x80) * -1 else message.M[7]
        mid = if (message.M[8] > 0x80) (message.M[8] - 0x80) * -1 else message.M[8]
        treble = if (message.M[9] > 0x80) (message.M[9] - 0x80) * -1 else message.M[9]
        balance = if (message.M[10] > 0x80) (message.M[10] - 0x80) * -1 else message.M[10]
        fade = if (message.M[11] > 0x80) (message.M[11] - 0x80) * -1 else message.M[11]
        mute = message.M[4] == 0x02

        when (message.M[5]) {
            0x00 -> { //volume
                active = 0
                activeString = "Vol:" + volume
            }

            0x01 -> { //bass
                active = 1
                activeString = "Bass:" + bass
            }

            0x02 -> {//mid
                active = 2
                activeString = "Mid:" + mid
            }

            0x03 -> {//tre
                active = 3
                activeString = "Tre:" + treble
            }

            0x04 -> {//
                active = 4
                activeString = "Bal:" + balance
            }

            0x05 -> {//
                active = 5
                activeString = "Fed:" + fade
            }
            0x06 -> {//
                active = 6
            }

            else -> {
            }
        }
//todo

    }
}