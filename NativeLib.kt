package com.ismet.novafr

object NativeLib {
    init {
        System.loadLibrary("llama_android")
    }

    external fun nativeLoadModel(modelPath: String): Boolean
    external fun nativeGenerate(prompt: String): String
}
