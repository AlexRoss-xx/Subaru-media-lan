/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.ObservableArrayList
import android.util.Log
import com.hzbhd.alexross.subarulan2.*

class CDChangerModel(stateModel: StateModel) : BaseObservable() {
    private val TAG = CDChangerModel::class.java.simpleName
    var soundSettings: SoundSettingsModel = stateModel.soundSettings
    var cdChangerTable: ObservableArrayList<Boolean> = ObservableArrayList<Boolean>()


    var isNewFolder: Boolean = false
    var isNewTrack: Boolean = false

    init {
        cdChangerTable.add(false)
        cdChangerTable.add(false)
        cdChangerTable.add(false)
        cdChangerTable.add(false)
        cdChangerTable.add(false)
        cdChangerTable.add(false)
    }


    var curentCD: Int = 0
        @Bindable get() = field
        set(value) {
            if (field != value) {
                field = value
                BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "D" + value.toString())
                notifyPropertyChanged(BR.curentCD)
                MyApplication.application.toastNotify()
            }
        }

    var changerState: CHANGERSTATE = CHANGERSTATE.NONE
        @Bindable get() = field
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.changerState)
                notifyPropertyChanged(BR.changerStateText)
            }
        }

    var changerStateText: String = ""
        @Bindable get() {
            when (changerState) {
                CHANGERSTATE.EJECTED -> return "Ejected"
                CHANGERSTATE.LOADED -> return "Loaded"
                CHANGERSTATE.EMPTY -> return "Empty"
                CHANGERSTATE.LOADING -> return "Loading"
                CHANGERSTATE.NONE -> return ""
                CHANGERSTATE.PLAYING -> return ""
                CHANGERSTATE.UNLOADING -> return "Unloading"
                else -> return ""
            }
        }


    var track: Int = 0
        @Bindable get() = field
        set(value) {
            if (field != value) {
                field = value
                file = 0
                fileName = ""
                isNewTrack = true
                notifyPropertyChanged(BR.track)
                MyApplication.application.toastNotify()
            }
        }


    var file: Int = 0
        @Bindable get() = field
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.file)
                getTrackInfo = false
                isNewTrack = true
            }
        }

    var folder: Int = 0
        @Bindable get() = field
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.folder)
                isNewFolder = true
            }
        }

    var getTrackInfo: Boolean = false
        @Bindable get() = field
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.getTrackInfo)
            }
        }

    var fileName: String = ""
        @Bindable get() = field
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.fileName)
            }
        }

    var folderName: String = ""
        @Bindable get() = field
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.folderName)
            }
        }

    fun getInfo() {
        if (isNewFolder) {
            BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "F" + folder.toString())
            isNewFolder = false
        } else if (isNewTrack) {
            BackgroundUSBService.SendMCUCommand(MyApplication.appContext, "T" + file.toString())
            isNewTrack = false
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

    var cdType: CDTYPE = CDTYPE.CD
        @Bindable get() = field
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.cdType)
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

    fun clearState() {
        // cdType = CDTYPE.CD
        track = 0
        file = 0
        folder = 0
        folderName = ""
        fileName = ""
    }

    fun process(message: LanMessage) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, message.toString())
        when (message.M[0]) {
            0x60 -> {
                when (message.M[2]) {
                    //       XX                                   |cd num
                    //  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21
                    // 60 02 01 01
                    // 60 02 00 00 00 31 00 00 00 00 01 BB BB 01 01 BB BB BB BB BB BB 00 - loaded
                    // 60 02 00 FF 00 32 00 00 00 00 01 BB BB 01 04 BB BB BB BB BB BB 00 - unload  cd 4
                    // 60 02 00 FF 00 42 00 00 00 00 01 BB BB 01 04 BB BB BB BB BB BB 00 - ejected
                    // 60 02 00 FF 00 51 00 00 00 00 01 BB BB 01 04 BB BB BB BB BB BB 00 - empty
                    //
                    // 60 02 00 00 00 00 00 00 02 00 01 BB BB 01 02 BB BB BB BB BB BB 00
                    // 60 02 00 00 00 00 00 00 02 00 01 BB BB 01 06 BB BB BB BB BB BB 00
                    // 60 02 00 00 00 00 00 00 02 00 01 BB BB 01 01 BB BB BB BB BB BB 00
                    // 60 02 00 00 00 00 00 00 02 00 01 BB BB 01 06 BB BB BB BB BB BB 00
                    // 60 02 00 00 00 00 00 00 02 00 01 BB BB 01 01 BB BB BB BB BB BB 00

                    // 60 02 00 00 00 00 01 00 01 00 05 BB 01 00 01 BB BB BB BB BB BB 00

                    // 60 02 00 00 00 00 01 00 01 00 05 BB 01 04 01 BB BB BB BB BB BB 00


                    // settings                                   |cd num
                    // 60 02 00 FF 00 00 00 00 00 00 01 BB BB 01 06 BB BB BB BB BB BB 00
                    // 60 02 00 FF 00 00 00 00 00 00 01 BB BB 01 02 BB BB BB BB BB BB 00  cd2
                    // 60 02 00 FF 00 00 00 00 00 00 01 BB BB 01 03 BB BB BB BB BB BB 00  cd3
                    // 60 02 00 00 00 00 00 00 02 00 01 BB BB 01 02 BB BB BB BB BB BB 00
                    // 60 02 00 FF 00 00 00 00 01 00 15 BB 01 01 05 BB BB BB BB BB BB 00 BB BB 01 03 00  cd5
                    // 60 02 00 FF 00 00 00 00 01 00 15 BB 01 01 01 BB BB BB BB BB BB 00 BB BB 01 02 00  cd1
                    // 60 02 00 00 00 00 01 00 01 00 05 BB 01 01 02 BB BB BB BB BB BB 00

                    //  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26
                    // playing                                          |seconds                   |folder
                    //                                         |track                           |file
                    //                       |track down & up
                    //                             | 01 scan 03 rpt 04-drpt 05 rdm
                    //
                    // 60 02 00 00 00 00 01 00 01 00 15 BB 01 01 01 B0 00 BB BB BB BB 00 BB BB 01 02 00
                    // 60 02 00 00 00 00 01 00 01 00 15 BB 01 01 01 B0 05 BB BB BB BB 00 BB BB 01 02 03
                    // 60 02 00 00 00 00 01 00 01 00 05 BB 01 01 01 B0 54 BB BB BB BB 00
                    // 60 02 00 00 00 00 01 0C 01 00 15 BB 01 01 02 B0 00 BB BB BB BB 00 BB BB 01 04 03
                    // 60 02 00 00 00 00 01 0C 01 00 15 BB 01 01 02 B0 00 BB BB BB BB 00 BB BB 01 03 00

                    0x00 -> { //cd
                        curentCD = message.M[14]

                        when (message.M[3]) {
                            0x00 -> {
                                when (message.M[5]) {
                                    0x00 -> {
                                        changerState = CHANGERSTATE.NONE
                                        if (message.M[8] == 0x02)
                                            clearState()
                                    }

                                    // 60 02 00 00 00 31 00 00 00 00 01 BB BB 01 01 BB BB BB BB BB BB 00
                                    0x31 -> {
                                        changerState = CHANGERSTATE.LOADED
                                        clearState()
                                    }

                                    // 60 02 00 FF 00 32 00 00 00 00 01 BB BB 01 04 BB BB BB BB BB BB 00 - unload  cd 4
                                    0x32 -> {
                                        changerState = CHANGERSTATE.UNLOADING
                                        clearState()
                                    }

                                    0x41 -> {
                                        changerState = CHANGERSTATE.LOADING
                                        clearState()
                                    }

                                    // 60 02 00 FF 00 42 00 00 00 00 01 BB BB 01 04 BB BB BB BB BB BB 00 - ejected
                                    0x42 -> {
                                        changerState = CHANGERSTATE.EJECTED
                                        clearState()
                                    }

                                    // 60 02 00 FF 00 51 00 00 00 00 01 BB BB 01 04 BB BB BB BB BB BB 00 - removed
                                    0x51 -> {
                                        changerState = CHANGERSTATE.EMPTY
                                        clearState()
                                    }
                                }

                                when (message.M[9]) {
                                    0x00 -> {  //  no
                                        playMode = ""
                                    }
                                    0x01, 0x11 -> {  //  scan
                                        playMode = "SCAN"
                                    }
                                    0x03, 0x14 -> {  //  rpt
                                        playMode = "RPT"
                                    }
                                    0x04 -> {  // disk rpt
                                        playMode = "D RPT"
                                    }
                                    0x05 -> {  // rdm
                                        playMode = "RDM"
                                    }
                                    0x15 -> {  //  folder rpt
                                        playMode = "F RPT"
                                    }
                                    0x17 -> {  //   f-rdm
                                        playMode = "F RDM"
                                    }
                                    0x18 -> {  //   d-rdm
                                        playMode = "D RDM"
                                    }
                                }

                                when (message.M[10]) {
                                    0x15 -> {  // playing mp3
                                        cdType = CDTYPE.MP3
                                        track = message.M[24]//message.M[13]
                                        folder = message.M[25]
                                        file = message.M[24]

                                        var minutes = 0
                                        var seconds = 0

                                        if (message.M[15] != 0xBB)
                                            minutes = if (message.M[15] in 100..186) {
                                                message.M[15] - 0xb0
                                            } else
                                                message.M[15]

                                        if (message.M[16] != 0xBB)
                                            seconds = message.M[16]

                                        playTime = String.format("%02d:%02x", minutes, seconds)

                                        if (message.M[26] == 0x03) {
                                            getInfo()
                                        }
                                    }

                                    0x05 -> {  // playing AUDIO
                                        cdType = CDTYPE.CD
                                        track = message.M[13]

                                        var minutes = 0
                                        var seconds = 0

                                        if (message.M[15] != 0xBB)
                                            minutes = if (message.M[15] in 100..186) {
                                                message.M[15] - 0xb0
                                            } else
                                                message.M[15]

                                        if (message.M[16] != 0xBB)
                                            seconds = message.M[16]


                                        playTime = String.format("%02d:%02x", minutes, seconds)
                                    }
                                }
                            }

                            // Changer CD operating
                            0xFF -> {
                                when (message.M[5]) {
                                    0x00 -> {
                                        changerState = CHANGERSTATE.NONE
                                    }

                                    // 60 02 00 00 00 31 00 00 00 00 01 BB BB 01 01 BB BB BB BB BB BB 00
                                    0x31 -> {
                                        changerState = CHANGERSTATE.LOADED
                                    }

                                    // 60 02 00 FF 00 32 00 00 00 00 01 BB BB 01 04 BB BB BB BB BB BB 00 - unload  cd 4
                                    0x32 -> {
                                        changerState = CHANGERSTATE.UNLOADING
                                    }

                                    0x41 -> {
                                        changerState = CHANGERSTATE.LOADING
                                    }

                                    // 60 02 00 FF 00 42 00 00 00 00 01 BB BB 01 04 BB BB BB BB BB BB 00 - ejected
                                    0x42 -> {
                                        changerState = CHANGERSTATE.EJECTED
                                    }

                                    // 60 02 00 FF 00 51 00 00 00 00 01 BB BB 01 04 BB BB BB BB BB BB 00 - removed
                                    0x51 -> {
                                        changerState = CHANGERSTATE.EMPTY
                                    }
                                }
                            }
                        }
                    }


                    // changer inner CD switching /
                    //       XX XX
                    //  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26
                    //           |cd
                    // 60 02 01 01 00
                    // 60 02 01 04 00  - cd 4 loaded
                    // 60 02 01 04 01  - cd 4 loading & unloading
                    // 60 02 01 06 01
                    // 60 02 01 06 00
                    //              | 03 AUDIO CD   05 MP3
                    // 60 02 01 01 05 A0 00 00 00 FF 00 FF 00
                    // 60 02 01 04 05 4E 00 00 00 FF 00 FF 00
                    // 60 02 01 01 03 13 72 57 00 FF 00 FF 00
                    // 60 02 01 02 03 0F 00 00 00 FF 00 FF 00
                    //:60 02 01 01 03 14 77 55 00 FF 00 FF 00
                    0x01 -> {
                        when (message.M[4]) {
                            0x00 -> {
                                clearState()
                                curentCD = message.M[3]
                            }
                            0x03 -> {
                                cdType = CDTYPE.CD
                                track = 0
                                file = 0
                                folder = 0
                                folderName = ""
                                fileName = ""
                            }
                            0x05 -> {
                                cdType = CDTYPE.MP3
                                track = 0
                                file = 0
                                folder = 0
                                folderName = ""
                                fileName = ""
                            }
                        }
                    }
                    // Set CD changer table
                    //          XX
                    //  0  1  2  3  4  5  6  7  8
                    // 60 02 01 01 01
                    // 60 02 02 11 31 11 11 FF FF
                    // 60 02 02 11 33 11 11 FF FF
                    // 60 02 02 11 33 31 11 FF FF
                    // 60 02 02 11 33 33 11 FF FF
                    // 60 02 02 11 33 33 31 FF FF
                    // 60 02 02 11 33 33 33 FF FF
                    0x02 -> {// changer cd table
                        when (message.M[3]) {
                            0x11 -> {
                                for (i in 0..2) {
                                    val value = message.M[i + 4]
                                    when (value) {
                                        0x31 -> {
                                            cdChangerTable[i * 2] = true
                                            cdChangerTable[i * 2 + 1] = false
                                        }
                                        0x13 -> {
                                            cdChangerTable[i * 2] = false
                                            cdChangerTable[i * 2 + 1] = true
                                        }
                                        0x33 -> {
                                            cdChangerTable[i * 2] = true
                                            cdChangerTable[i * 2 + 1] = true
                                        }
                                        else -> {
                                            cdChangerTable[i * 2] = false
                                            cdChangerTable[i * 2 + 1] = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Set file name or Folder name
            //                   XX
            //  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26
            // 65 02 00 01 01 02 00 04 00 00 32 20 42 69 20 4D 61 63 68 69 6E 65 00 00 00 00 00
            // 65 02 00 01 01 02 01 04 01 00 30 30 31 20 2D 20 55 6E 20 2D 20 44 65 63 65 69 00
            // 65 02 00 01 01 02 01 04 01 01 76 65 64 00 00 00 00 00 00 00 00 00 00 00 00 00 00
            0x65 -> {
                when (message.M[6]) {
                    0x00 -> {
                        folder = message.M[7]
                        // folderName = "";
                        var str = ""
                        for (a in 10..26) {
                            if (message.M[a] != 0x00)
                                str += message.M[a].toChar()
                        }
                        folderName = str
                    }
                    0x01 -> {
                        file = message.M[8]

                        if (message.M[9] == 0x00) {
                            //  fileName = "";
                            var str = ""
                            for (a in 10..26) {
                                if (message.M[a] != 0x00)
                                    str += message.M[a].toChar()
                            }
                            fileName = str.trim()
                        } else {
                            var str = ""
                            for (a in 10..26) {
                                if (message.M[a] != 0x00)
                                    str += message.M[a].toChar()
                            }
                            fileName += str
                        }
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
}

enum class CHANGERSTATE {
    NONE,
    PLAYING,
    EMPTY,
    LOADING,
    LOADED,
    UNLOADING,
    EJECTED
}

enum class CDTYPE {
    CD,
    MP3
}

enum class PLAYMODE {
    NONE,
    SCAN,
    RPT,
    FOLDER_RPT,
    DISK_RPT,
    RDM,
    FOLDER_RDM,
    DISK_RDM
}