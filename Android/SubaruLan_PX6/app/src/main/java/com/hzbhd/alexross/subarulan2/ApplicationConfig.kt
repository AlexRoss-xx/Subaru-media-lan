/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2

import android.util.Base64
import com.stringcare.library.SC
import  android.os.Bundle


class ApplicationConfig {
    companion object {
        init {
            SC.init(MyApplication.appContext)
            try {
                System.loadLibrary("native-lib")
            } catch ( er:UnsatisfiedLinkError){

            }
        }

        external fun getNativeKey1(): String
        external fun getNativeKey2(): String
        external fun getKeyMCU(): String

        var mcuVersion: String = ""
        var mcuVersionAvailible: String =""

        var device: String? = ""

        //!!!!!!!!!!!!!!!!!!!!!!!!!!
        val US = false      // miles F
        val UStoKM = false

        //CDC Emulation
        val rsecdcEmulation = false  // false if external device
        val unmuteMode: com.hzbhd.alexross.subarulan2.models.State = com.hzbhd.alexross.subarulan2.models.State.SAT

        val deviceSerialNumber: String =  "12345678"// String((Base64.decode(getNativeKey1(), Base64.DEFAULT)))
      //  val mcuId: String = getKeyMCU()
        val registeredTo: String = "Alexey" // getNativeKey2()

    }
}