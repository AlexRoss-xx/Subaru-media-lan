/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class MCUBroadcastReceiver : BroadcastReceiver() {
    private val TAG = MCUBroadcastReceiver::class.java.simpleName
    val ACTION_MCU_STATE_CHANGE = "com.nwd.action.ACTION_MCU_STATE_CHANGE"
    val EXTRA_MCU_STATE = "extra_mcu_state"

    override fun onReceive(context: Context, intent: Intent) {

    }
}