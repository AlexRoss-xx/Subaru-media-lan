/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public enum LanMessageType {
    @SerializedName("3")
    MCU(3),
    @SerializedName("2")
    CAN(2),
    @SerializedName("1")
    LAN(1);


    private final int value;
    private LanMessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}