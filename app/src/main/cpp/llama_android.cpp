#include <jni.h>
#include <string>

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_ismet_novafr_llm_LlamaInference_nativeLoadModel(JNIEnv* env, jobject thiz, jstring modelPath) {
    return JNI_TRUE;
}

JNIEXPORT jstring JNICALL
Java_com_ismet_novafr_llm_LlamaInference_nativeGenerate(
        JNIEnv* env, jobject thiz, jstring prompt, jfloat temp, jfloat topP, jint topK, jfloat repeatPenalty, jint maxTokens) {
    std::string response = "Atlas Engine initialized locally.";
    return env->NewStringUTF(response.c_str());
}

JNIEXPORT void JNICALL
Java_com_ismet_novafr_llm_LlamaInference_nativeFreeModel(JNIEnv* env, jobject thiz) {
}

}
