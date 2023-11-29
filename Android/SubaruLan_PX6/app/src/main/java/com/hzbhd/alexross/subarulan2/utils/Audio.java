/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.utils;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import java.lang.reflect.Method;

public class Audio {
    public static boolean isAudioAppRunning(Context mContext, String packageName) {
        boolean result = false;

        AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        Class ownerClass = mAudioManager.getClass();

        try {
            Class[] argsClass = new Class[1];
            argsClass[0] = String.class;

            Method method = ownerClass.getMethod("isAppInFocus", argsClass);

            Object[] params = new Object[1];
            params[0] = packageName;

            boolean isMusicActive = mAudioManager.isMusicActive();
            if (isMusicActive) {
                boolean isAppInFocus = (Boolean) method.invoke(mAudioManager, params);
                result = isMusicActive && isAppInFocus;

                Log.d(Audio.class.getSimpleName(), packageName + ", isAudioAppRunning isMusicActive= " + isMusicActive +
                        ", isAppInFocus = " + isAppInFocus + ", result = " + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
