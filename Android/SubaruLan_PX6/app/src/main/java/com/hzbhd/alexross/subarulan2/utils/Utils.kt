/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2

class IncomingMessageUtil {
    companion object {

        fun toArray(incomeStr: String): Array<String> {
            val array: Array<String> = incomeStr.trim { it <= ' ' }.split("\\r\\n|\\n|\\r".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return array
        }

        fun strMethod(res: String, t: String): String {
            var t1 = t
            val array: Array<String> = res.trim { it <= ' ' }.split("\\r\\n|\\n|\\r".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var n = array.size
            var i = 0;
            if (n >= 2)
                while (i <= n - 1) {
                    if (t1.length == 0) {
                        if (i == n - 1)
                            t1 = array[i];
                        else {
                            t1 = array[i + 1];
                            i += 1
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
    }

}