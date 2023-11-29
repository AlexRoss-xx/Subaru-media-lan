/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.models;

import com.google.gson.annotations.SerializedName;
import com.hzbhd.alexross.subarulan2.models.LanMessageType;


public class LanMessage {
    /**
     * Message type LAN or CAN
     */
    @SerializedName("T")
    public LanMessageType T;
    /**
     * DataAdress
     */
    @SerializedName("A")
    public int A;

    /**
     * Message
     */
    @SerializedName("M")
    public int[] M = new int[]{};

    @Override
    public java.lang.String toString() {
        String message="T:%" +T+" A:"+String.format("%02X",A)+" M:";
        for (int i = 0; i < M.length; i++) {
            message=message+ String.format("%02X", M[i])+" ";
        }
        return   message;
    }
}
