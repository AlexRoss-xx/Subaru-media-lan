//
// Created by aliaksei.raskazau on 8/9/2018.
//

#define  ra

#include <jni.h>
#include <string>


//KEYS
extern "C" JNIEXPORT jstring JNICALL
Java_com_hzbhd_alexross_subarulan2_ApplicationConfig_00024Companion_getNativeKey1(
        JNIEnv *env,
        jobject /* this */) {

    std::string key = "QTlTSjc5VEw=";
    return env->NewStringUTF(key.c_str());
}

//  EMAILS
extern "C" JNIEXPORT jstring JNICALL
Java_com_hzbhd_alexross_subarulan2_ApplicationConfig_00024Companion_getNativeKey2(
        JNIEnv *env,
        jobject /* this */) {

    std::string email = "rasskazoff@gmail.com";
    return env->NewStringUTF(email.c_str());
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_hzbhd_alexross_subarulan2_ApplicationConfig_00024Companion_getKeyMCU(
        JNIEnv *env,
        jobject /* this */) {

    std::string mcuid = "43e69bd7";
    return env->NewStringUTF(mcuid.c_str());
}