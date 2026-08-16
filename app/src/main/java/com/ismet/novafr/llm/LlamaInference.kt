package com.ismet.novafr.llm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LlamaInference private constructor() {

    private var modelLoaded = false

    companion object {
        const val MODEL_FILENAME = "Atlas-FirstAid.Q4_K_M.gguf"

        const val SYSTEM_PROMPT = """You are Atlas, a first-aid and emergency response assistant. You are built on Qwen2.5-1.5B and are designed by Ismet Beljulji to help people in crisis situations with no internet access. Your goal is to give clear, actionable, and safe advice for common emergencies, injuries, and survival scenarios. If you don't know the answer, say so—do not guess or invent protocols. Always prioritize the safety of the user and remind them to seek professional medical help when possible. Never suggest harmful or unproven remedies (e.g., freezing ticks, applying heat to burns). Stay calm, clear, and concise. You are not a doctor—you are a first-aid companion."""

        const val TEMPERATURE = 0.3f
        const val TOP_P = 0.9f
        const val TOP_K = 40
        const val REPEAT_PENALTY = 1.15f
        const val MAX_NEW_TOKENS = 300

        @Volatile
        private var INSTANCE: LlamaInference? = null

        fun getInstance(): LlamaInference {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LlamaInference().also { INSTANCE = it }
            }
        }
    }

    private external fun nativeLoadModel(modelPath: String): Boolean
    private external fun nativeGenerate(
        prompt: String,
        temperature: Float,
        topP: Float,
        topK: Int,
        repeatPenalty: Float,
        maxTokens: Int
    ): String
    private external fun nativeFreeModel()

    init {
        System.loadLibrary("llama_android")
    }

    suspend fun loadModel(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(modelPath)
        if (!file.exists()) {
            return@withContext false
        }
        val success = nativeLoadModel(modelPath)
        modelLoaded = success
        success
    }

    fun isModelLoaded(): Boolean = modelLoaded

    suspend fun generateResponse(
        conversationHistory: List<Pair<String, String>>,
        newUserMessage: String
    ): String = withContext(Dispatchers.Default) {
        if (!modelLoaded) {
            return@withContext "The AI model isn't loaded yet. Please restart the app."
        }

        val prompt = buildPrompt(conversationHistory, newUserMessage)

        nativeGenerate(
            prompt,
            TEMPERATURE,
            TOP_P,
            TOP_K,
            REPEAT_PENALTY,
            MAX_NEW_TOKENS
        )
    }

    private fun buildPrompt(
        history: List<Pair<String, String>>,
        newUserMessage: String
    ): String {
        val sb = StringBuilder()
        sb.append("<|im_start|>system\n$SYSTEM_PROMPT<|im_end|>\n")
        for ((role, content) in history) {
            sb.append("<|im_start|>$role\n$content<|im_end|>\n")
        }
        sb.append("<|im_start|>user\n$newUserMessage<|im_end|>\n")
        sb.append("<|im_start|>assistant\n")
        return sb.toString()
    }

    fun release() {
        if (modelLoaded) {
            nativeFreeModel()
            modelLoaded = false
        }
    }
}
