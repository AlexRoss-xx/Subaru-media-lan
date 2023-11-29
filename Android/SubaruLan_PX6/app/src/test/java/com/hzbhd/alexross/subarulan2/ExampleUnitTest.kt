/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2


import com.google.gson.Gson
import com.hzbhd.alexross.subarulan2.models.*

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    @Throws(Exception::class)
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }

    @Test
    @Throws(Exception::class)
    fun StringTuner() {

        var t: String = ""
        val res = "187,187,187,187,153,91, 11, 255]}\r\n{\"T\":1,\"A\":336,\"M\":[96,6,0,0,0,3,0,0,1,"
        t = strMethod(res, t)
        val res2 = "187,187,187,187,153,91, 11, 255]}\r\n{\"T\":1,\"A\":336,\"M\":[96,6,0,0,0,3,0,0,1,"
        t = strMethod(res2, t)

        val res3 = "187,187,187,187,153,91, 11, 255]}\r\n{\"T\":1,\"A\":336,\"M\":[96,6,0,0,0,3,0,0,1,"
        strMethod(res3, t)
        val res4 = "187,187,187,187,153,91, 11, 255]}\r\n{\"T\":1,\"A\":336,\"M\":[96,6,0,0,0,3,0,0,1,187,187,187,187,153,91, 11, 255]}\r\n{\"T\":1,\"A\":336,\"M\":[96,6,0,0,0,3,0,0,1,"
        strMethod(res4, t)
    }


    fun strMethod(res: String, t: String): String {
        var t1 = t
        val array = res.trim { it <= ' ' }.split("\\r\\n|\\n|\\r".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var n = array.size
        var i = 0;
        if (n >= 2)
            while (i <= n - 1) {
                if (t1.length == 0) {
                    if (i == n - 1)
                        t1 = array[i];
                    else {
                        if(array[i].startsWith("{") && array[i].endsWith("}"))
                        {
                            var toSend = t1
                        }
                        else {
                            t1 = array[i + 1];
                            i += 1
                        }
                    }

                } else {
                    t1 += array[i]
                    if (t1.startsWith("{") && t1.endsWith("}")) {
                        var toSend = t1
                        t1 = ""
                    }
                }
                i += 1
            }
        return t1
    }


    @Test
    @Throws(Exception::class)
    fun TestTuner() {

        val tuner = TunerModel(StateModel())
        val res = "{\"T\":1,\"A\":336,\"M\":[96,6,0,0,0,3,0,0,1,187,187,187,187,153,91, 11, 255]}"
        val gson = Gson()
        val message = gson.fromJson(res, LanMessage::class.java)
        tuner.process(message)


    }


    @Test
    @Throws(Exception::class)
    fun TestCAN() {
        val can = CANModel(StateModel())

        val message = LanMessage()
        message.M = intArrayOf(0x4A , 0x53 , 0x55 , 0x08 , 0x15 , 0x51 , 0x54 , 0x1E , 0x02 , 0x248C , 0x65 , 0x00)
        message.A=0xfff;
        message.T= LanMessageType.CAN
        can.processMessage(message);
      var v=  can.fuelConsumption
        message.M = intArrayOf(0x4A , 0x53 , 0x55 , 0x08 , 0x15 , 0x51 , 0x54 , 0x1E , 0x02 , 0x248C , 0x65 , 0x02)
         v=  can.fuelConsumption
        message.M = intArrayOf(0x4A , 0x53 , 0x55 , 0x08 , 0x15 , 0x51 , 0x54 , 0x1E , 0x02 , 0x248C , 0x65 , 0x10)
        v=  can.fuelConsumption
        message.M = intArrayOf(0x4A , 0x53 , 0x55 , 0x08 , 0x15 , 0x51 , 0x54 , 0x1E , 0x02 , 0x248C , 0x65 , 0x20)
        v=  can.fuelConsumption
        message.M = intArrayOf(0x4A , 0x53 , 0x55 , 0x08 , 0x15 , 0x51 , 0x54 , 0x1E , 0x02 , 0x248C , 0x65 , 0x30)
        v=  can.fuelConsumption
        message.M = intArrayOf(0x4A , 0x53 , 0x55 , 0x08 , 0x15 , 0x51 , 0x54 , 0x1E , 0x02 , 0x248C , 0x65 , 0x05)
        v=  can.fuelConsumption
    }


    @Test
    @Throws(Exception::class)
    fun TestCD() {

        val changer = CDChangerModel(StateModel())

        val message = LanMessage()
        message.M = intArrayOf(0x60, 0x02, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x15, 0xBB, 0x01, 0x01, 0x01, 0xB0, 0x05, 0xBB, 0xBB, 0xBB, 0xBB, 0x00, 0xBB, 0xBB, 0x01, 0x02, 0x03)

        //  String res="{\"T\":1,\"A\":336,\"M\":[96,6,0,0,0,3,0,0,1,187,187,187,187,153,91, 11, 255]} " ;
        //   Gson gson = new Gson();
        //   LanMessage message = gson.fromJson(res, LanMessage.class);
        changer.process(message)

        message.M = intArrayOf(0x60, 0x02, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x18, 0x15, 0xBB, 0x01, 0x01, 0x01, 0xB0, 0x06, 0xBB, 0xBB, 0xBB, 0xBB, 0x00, 0xBB, 0xBB, 0x14, 0x04, 0x03)
        changer.process(message)

        message.M = intArrayOf(0x60, 0x02, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x15, 0xBB, 0x01, 0x01, 0x01, 0xB0, 0x20, 0xBB, 0xBB, 0xBB, 0xBB, 0x00, 0xBB, 0xBB, 0x01, 0x02, 0x03)
        changer.process(message)

        message.M = intArrayOf(0x60, 0x02, 0x02, 0x11, 0x33, 0x33, 0x33, 0xFF, 0xFF)
        changer.process(message)
    }
}