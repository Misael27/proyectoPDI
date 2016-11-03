#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_android_misael_pdi_1ocr_MainActivity_getMsgFromJni(JNIEnv *env, jobject instance) {

    // TODO


    return (*env)->NewStringUTF(env, "Hola desde JNI mundo");
}