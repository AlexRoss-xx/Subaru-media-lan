/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.hzbhd.alexross.subarulan2.*
import com.hzbhd.alexross.subarulan2.analyzer.MessageAnalyzer
import com.hzbhd.alexross.subarulan2.BackgroundUSBService.SendStateCommand


/**
 * Created by Alexey Rasskazov.
 */
class StateModel : BaseObservable() {
    internal var messageAnalizer = MessageAnalyzer(this)
    private var listener: ChangeListener? = null

    var soundSettings: SoundSettingsModel = SoundSettingsModel(this);
    var tunerModel: TunerModel = TunerModel(this);
    var cdChangerModel: CDChangerModel = CDChangerModel(this)
    var canModel: CANModel = CANModel(this);
    var rseModel: RSEModel = RSEModel(this);
    var satModel: SATModel = SATModel(this);
    var RSECDCModel: RSECDCModel = RSECDCModel(this);

    var infoWindowEnable:  Boolean = false
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.infoWindowEnable)
        }

    var infoWindowType:  Int = 1
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.infoWindowType)
        }

    var infoWindowDisable:  Boolean = false
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.infoWindowDisable)
        }

    var prevState: State = State.UNKNOWN


    var currentState: State = State.OFF
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.currentState)
        }

    var stateText: String = ""
        @Bindable get() =
            when (currentState) {
                State.CD -> {
                    "CD"
                }
                State.SAT -> {
                    "SAT"
                }
                State.RSE -> {
                    "RSE"
                }
                State.RSECDC -> {
                    "RSE CDC"
                }
                State.TUNER -> {
                    ""
                }
                State.SOUNDSETTINGS -> {
                    "S:"
                }
                else -> {
                    ""
                }
            }

    fun ChangeState(newSate: State) {
        prevState = currentState;
        currentState = newSate;

        notifyPropertyChanged(BR.stateText)

        SendStateCommand(MyApplication.appContext, currentState)
        listener?.onChange();
        MyApplication.application.toastNotify()
    }

    var radioOn: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.radioOn)
                MyApplication.application.toastNotify()
            }
        }

    fun ProcessMessage(message: LanMessage) {
        messageAnalizer.ProcessMessage(message);
    }

    fun getListener(): ChangeListener? {
        return listener
    }

    fun setListener(listener: ChangeListener) {
        this.listener = listener
    }

    interface ChangeListener {
        fun onChange()
    }
}

enum class State {
    UNKNOWN,
    OFF,
    TUNER,
    CD,
    RSE,
    RSECDC,
    SAT,
    SOUNDSETTINGS,
    CARINFO
}

enum class HUState {
    UNREGISTERED,
    REGISTERED,
}