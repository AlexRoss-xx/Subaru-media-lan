/*
 *  2023.
 * Alexey Rasskazov
 */

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

class TunerModel(stateModel: StateModel) : BaseObservable() {

    private val TAG = TunerModel::class.java.simpleName

    var tunerMode: TUNERMODE = TUNERMODE.FM1
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.tunerMode)
            MyApplication.application.toastNotify()
        }

    var soundSettings: SoundSettingsModel = stateModel.soundSettings

    private var FM1StationsTable: Array<String> = arrayOf("1", "2", "3", "4", "5", "6")
    private var FM2StationsTable: Array<String> = arrayOf("1", "2", "3", "4", "5", "6")
    private var FM3StationsTable: Array<String> = arrayOf("1", "2", "3", "4", "5", "6")
    private var AMStationsTable: Array<String> = arrayOf("1", "2", "3", "4", "5", "6")

    private var _currentFRQ: String = ""

    var rdsMessage: String = ""
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.rdsMessage)
        }


    var currentFRQ: String
        @Bindable get() = _currentFRQ
        set(value) {
            _currentFRQ = value
            notifyPropertyChanged(BR.currentFRQ)
            MyApplication.application.toastNotify()
        }


    var currentStations: Array<String> = FM1StationsTable
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.currentStations)
        }


    var stationNumber: Int? = null
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.stationNumber)
        }

    var isStereo: Boolean = false
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.stereo)
        }

    var isScan: Boolean = false
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.scan)
        }


    //                      |stereo
    //             |seek_scan  |fm                  |stantion number
    // 0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16
    //60 06 00 00 00 03 00 01 01 BB BB BB BB 99 5B 0B FF   radio FM
    //60 06 00 00 00 03 00 00 10 BB BB BB BB 53 0B 01 FF   radio AM
    //60 07 00 00 00 00 03 00 00 00 20 BB BB BB BB 15 3B 01 00 00 00 00 00
    //60 06 00 00 03 03 00 00 01 BB BB BB B1 04 5B 0B FF   seek up
    //60 06 00 00 04 03 00 00 02 BB BB BB B1 05 7B 0B FF   seek down
    //60 06 00 00 19 03 10 00 01 BB BB BB B1 07 9B 0B FF   scan on fm1
    //60 06 00 00 19 03 10 00 02 BB BB BB BB 91 1B 0B FF   scan fm2
    //60 06 00 00 09 03 10 01 01 BB BB BB B1 07 9B 06 FF
    //60 06 02 11 02 BB 87 9B BB 87 9B BB 90 1B BB 98 1B B1 06 1B B1 07 9B  fm stations
    //60 06 02 11 10 BB 53 0B BB 60 0B B1 00 0B B1 40 0B B1 62 0B B1 62 0B
    //60 06 02 11 01 BB 87 9B BB 87 9B BB 90 1B BB 98 1B B1 06 1B B1 07 9B
    //60 07 02 11 01 BB 87 50 BB 87 90 BB 98 10 B1 05 10 B1 07 90 B1 08 00
    //60 07 00 00 23 00 03 00 21 00 01 BB BB BB BB 94 30 0B 00 00 00 00 00
    //60 07 02 11 02 BB 87 50 BB 87 90 BB 98 10 B1 05 10 B1 07 90 B1 08 00
    //60 07 00 00 00 00 03 00 21 00 02 BB BB BB BB 87 55 0B 00 00 00 00 00
    //60 07 00 00 00 00 03 00 00 00 20 BB BB BB BB 15 3B 01 00 00 00 00 00
    //60 07 01 00 02 00 03 03 52 45 4C 41 58 20 20 20 rds
    //60 07 01 00 01 06 03 03 00 00 00 00 00 00 00 00
    //60 07 00 FF
    fun process(message: LanMessage) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, message.toString())
        when (message.M[1]) {
            0x06 -> {
                when (message.M[2]) {
                    0x00 -> { //station
                        when {
                            message.M[8] == 0x01 -> {
                                tunerMode = TUNERMODE.FM1
                                currentStations = FM1StationsTable
                            }
                            message.M[8] == 0x02 -> {
                                tunerMode = TUNERMODE.FM2
                                currentStations = FM2StationsTable
                            }
                            message.M[8] == 0x03 -> {
                                tunerMode = TUNERMODE.FM3
                                currentStations = FM3StationsTable
                            }
                            else -> {
                                tunerMode = TUNERMODE.AM
                                currentStations = AMStationsTable
                            }
                        }


                        when (message.M[4]) {
                            0x19, 0x23 -> {
                                isScan = true
                            }
                            0x9 -> {
                            }
                            else -> {
                                isScan = false
                            }
                        }

                        val strpes = convertToString(message.M)
                        currentFRQ = if (strpes[25] == 'B') {
                            if (tunerMode == TUNERMODE.AM)
                                String.format("%s%s%s", strpes[26], strpes[27], strpes[28])
                            else
                                String.format("%s%s.%s", strpes[26], strpes[27], strpes[28])
                        } else {
                            if (tunerMode == TUNERMODE.AM)
                                String.format("%s%s%s%s", strpes[25], strpes[26], strpes[27], strpes[28])
                            else
                                String.format("%s%s%s.%s", strpes[25], strpes[26], strpes[27], strpes[28])
                        }

                        stationNumber = if (strpes[31] != 'B')
                            message.M[15]
                        else
                            null

                        if (message.M[7] == 0x01)
                            isStereo = true
                        else
                            isStereo = false

                    }
                    0x02 -> {// stations table
                        when (message.M[3]) {
                            0x11 -> {
                                when (message.M[4]) {
                                    0x01 -> {
                                        fillStationTable(convertToString(message.M), FM1StationsTable, false)
                                        //  currentStations = FM1StationsTable;
                                        notifyPropertyChanged(BR.currentStations)
                                    }
                                    0x02 -> {
                                        fillStationTable(convertToString(message.M), FM2StationsTable, false)
                                        //  currentStations = FM2StationsTable;
                                        notifyPropertyChanged(BR.currentStations)
                                    }
                                    0x03 -> {
                                        fillStationTable(convertToString(message.M), FM3StationsTable, false)
                                        //  currentStations = FM2StationsTable;
                                        notifyPropertyChanged(BR.currentStations)
                                    }

                                    0x30, 0x10 -> {
                                        fillStationTable(convertToString(message.M), AMStationsTable, true)
                                        // currentStations = AMStationsTable;
                                        notifyPropertyChanged(BR.currentStations)
                                    }
                                    else -> {
                                    }
                                }
                            }
                            else -> {
                            }
                        }
                    }
                    else -> {
                    }
                }
            }
            0x07 -> {
                if (message.M[3] != 0xFF)
                    when (message.M[2]) {
                        0x00 -> { //station
                            val strpes = convertToString(message.M)
                            currentFRQ = if (strpes[29] == 'B') {
                                if (tunerMode == TUNERMODE.AM)
                                    String.format("%s%s%s", strpes[30], strpes[31], strpes[32])
                                else
                                    String.format("%s%s.%s%s", strpes[30], strpes[31], strpes[32], strpes[33])
                            } else {
                                if (tunerMode == TUNERMODE.AM)
                                    String.format("%s%s%s%s", strpes[29], strpes[30], strpes[31], strpes[32])
                                else
                                    String.format("%s%s%s.%s%s", strpes[29], strpes[30], strpes[31], strpes[32], strpes[33])
                            }
//todo
                            stationNumber = if (strpes[35] != 'B')
                                message.M[17]
                            else
                                null

                            if (message.M[9] == 0x01)
                                isStereo = true
                            else
                                isStereo = false

                            when {
                                message.M[10] == 0x01 -> {
                                    tunerMode = TUNERMODE.FM1
                                    currentStations = FM1StationsTable
                                }
                                message.M[10] == 0x02 -> {
                                    tunerMode = TUNERMODE.FM2
                                    currentStations = FM2StationsTable
                                }
                                message.M[10] == 0x03 -> {
                                    tunerMode = TUNERMODE.FM3
                                    currentStations = FM3StationsTable
                                }
                                else -> {
                                    tunerMode = TUNERMODE.AM
                                    currentStations = AMStationsTable
                                }
                            }


                            when (message.M[4]) {
                                0x19 -> {
                                    isScan = true
                                }
                                0x9 -> {
                                }
                                else -> {
                                    isScan = false
                                }
                            }


                        }
                        0x01 -> {
                            when (message.M[4]) {
                                0x02 -> {
                                    var str = ""
                                    for (a in 8..15) {
                                        if (message.M[a] != 0x00)
                                            str += message.M[a].toChar()
                                    }
                                    rdsMessage = str
                                }
                            }
                        }
                        0x02 -> {// stations table
                            when (message.M[3]) {
                                0x11 -> {
                                    when (message.M[4]) {
                                        0x01 -> {
                                            fillStationTable(convertToString(message.M), FM1StationsTable, false)
                                            //  currentStations = FM1StationsTable;
                                            notifyPropertyChanged(BR.currentStations)
                                        }
                                        0x02 -> {
                                            fillStationTable(convertToString(message.M), FM2StationsTable, false)
                                            //  currentStations = FM2StationsTable;
                                            notifyPropertyChanged(BR.currentStations)
                                        }
                                        0x03 -> {
                                            fillStationTable(convertToString(message.M), FM3StationsTable, false)
                                            //  currentStations = FM2StationsTable;
                                            notifyPropertyChanged(BR.currentStations)
                                        }

                                        0x30, 0x10 -> {
                                            fillStationTable(convertToString(message.M), AMStationsTable, true)
                                            // currentStations = AMStationsTable;
                                            notifyPropertyChanged(BR.currentStations)
                                        }
                                        else -> {
                                        }
                                    }
                                }
                                else -> {
                                }
                            }
                        }
                        else -> {
                        }
                    }
            }
        }

    }


    private fun convertToString(arr: IntArray): String {
        var formater = ""
        for (i in arr) {
            formater += String.format("%02X", i)
        }
        return formater
    }

    private fun fillStationTable(str: String, table: Array<String>, isAM: Boolean) {
        var n = 0
        while (n < 6) {
            if (str[11 + n * 6] == 'B')
                if (isAM)
                    table[n] = String.format("%s%s%s", str[12 + n * 6], str[13 + n * 6], str[14 + n * 6])
                else
                    table[n] = String.format("%s%s.%s", str[12 + n * 6], str[13 + n * 6], str[14 + n * 6])
            else
                if (isAM)
                    table[n] = String.format("%s%s%s%s", str[11 + n * 6], str[12 + n * 6], str[13 + n * 6], str[14 + n * 6])
                else
                    table[n] = String.format("%s%s%s.%s", str[11 + n * 6], str[12 + n * 6], str[13 + n * 6], str[14 + n * 6])

            n++
        }

    }
}

enum class TUNERMODE {
    FM1,
    FM2,
    FM3,
    AM
}