/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.models

import android.content.Intent
import androidx.databinding.BaseObservable
import com.hzbhd.alexross.subarulan2.MyApplication

class SATModel(stateModel: StateModel) : BaseObservable() {
    var soundSettings: SoundSettingsModel = stateModel.soundSettings

    fun onNext() {
        val i = Intent("com.nwd.ACTION_PLAY_COMMAND")
        i.putExtra("extra_command", 3)
        MyApplication.appContext?.sendBroadcast(i)
    }

    fun onPrev() {
        val i = Intent("com.nwd.ACTION_PLAY_COMMAND")
        i.putExtra("extra_command", 2)
        MyApplication.appContext?.sendBroadcast(i)
    }
    fun process(message: LanMessage) {
        //  0  1  2  3  4  5  6  7
        // 50 01 42 42 01 01 00 00   seek  up
        // 50 04 42 42 01 01 00 00
        // 50 01 42 42 02 02 00 00   seek down
        // 50 04 42 42 02 02 00 00
        // 50 02 42 42 31 00 00 00   seek fldr up
        // 50 02 42 42 32 00 00 00   seek fldr down
        // 50 02 42 42 45 00 00 00   scan

        // 50 01 42 42 0B 0C 01 01  1
        // 50 04 42 42 0B 0C 01 01
        // 50 01 42 42 0B 0C 02 02  2
        // 50 04 42 42 0B 0C 02 02


        when (message.M[1]) {
            0x01 -> {
                when (message.M[4]) {
                    0x01 -> {
                        onNext()
                    }
                    0x02 -> {
                        onPrev()
                    }
                    0x10 -> {
                    }
                }
            }
            0x04 -> {
            }
        }
    }
}