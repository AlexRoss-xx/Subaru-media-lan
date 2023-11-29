/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.models

import android.util.Log
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.hzbhd.alexross.subarulan2.*

class RSECDCModel(stateModel: StateModel) : BaseObservable() {
    private val TAG = RSECDCModel::class.java.simpleName

    val ACTION_ON_WIDGET_BUTTON_CLICK = "com.nwd.action.music.on_widget_button_click"
    val EXTRA_BUTTON = "extra_button"

    var folder: Int = 0
        @Bindable get() = field
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.folder)
                MyApplication.application.toastNotify()
            }
        }

    var track: Int = 0
        @Bindable get() = field
        set(value) {
            if (field != value) {
                field = value

                notifyPropertyChanged(BR.track)
                MyApplication.application.toastNotify()
            }
        }

    var playTime: String = ""
        @Bindable get() = field
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.playTime)
            }
        }

    var playMode: String = ""
        @Bindable get() = field
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.playMode)
            }
        }

    var changerState: CHANGERSTATE = CHANGERSTATE.NONE
        @Bindable get() = field
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.changerState)

            }
        }


    fun process(message: LanMessage) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, message.toString())
        //  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26
        //                                        58-no disk
        // 60 22 00 00 00 00 01 00 01 00 04 BB 01 0C 06 02 09 30 30 30 30 00
        // 60 22 00 00 00 00 01 00 01 00 04 BB 01 58 06 00 00 30 30 30 30 00
        // 60 22 00 00 00 00 01 00 01 00 04 BB 01 0C 06 02 07 30 30 30 30 00
        // 60 22 01 06 03 63 73 59 02 FF FF FF FF
        when (message.M[0]) {
            0x60 -> {
                if (message.M[2] == 0x00)
                    if (message.M[13] == 0x58) {
                        changerState = CHANGERSTATE.EMPTY
                    } else {
                        changerState = CHANGERSTATE.PLAYING
                        track = message.M[13]
                        folder = message.M[14]
                        playTime = String.format("%02d:%02x", message.M[15], message.M[16])
                    }
            }
        }
    }


    enum class CHANGERSTATE {
        NONE,
        PLAYING,
        EMPTY
    }

    enum class PLAYMODE {
        NONE,
        SCAN,
        RPT,
        FOLDER_RPT,
        RDM,
        FOLDER_RDM,
    }
}