#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "llama.h"

#define TAG "NovaFR-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static llama_model* g_model = nullptr;
static llama_context* g_ctx = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_ismet_novafr_llm_LlamaInference_nativeLoadModel(JNIEnv* env, jobject thiz, jstring modelPath) {
    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    LOGI("Loading model from path: %s", path);

    llama_backend_init();

    llama_model_params model_params = llama_model_default_params();
    g_model = llama_load_model_from_file(path, model_params);

    env->ReleaseStringUTFChars(modelPath, path);

    if (!g_model) {
        LOGE("Failed to load llama model");
        return JNI_FALSE;
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 2048;   // Context length
    ctx_params.n_threads = 4; // CPU threads

    g_ctx = llama_new_context_with_model(g_model, ctx_params);
    if (!g_ctx) {
        LOGE("Failed to create llama context");
        llama_free_model(g_model);
        g_model = nullptr;
        return JNI_FALSE;
    }

    LOGI("Model loaded successfully!");
    return JNI_TRUE;
}

JNIEXPORT jstring JNICALL
Java_com_ismet_novafr_llm_LlamaInference_nativeGenerate(
        JNIEnv* env, jobject thiz, jstring prompt, jfloat temp, jfloat topP, jint topK, jfloat repeatPenalty, jint maxTokens) {

    if (!g_model || !g_ctx) {
        return env->NewStringUTF("Engine not initialized properly.");
    }

    const char* prompt_cstr = env->GetStringUTFChars(prompt, nullptr);
    std::string full_prompt(prompt_cstr);
    env->ReleaseStringUTFChars(prompt, prompt_cstr);

    // Tokenize prompt
    std::vector<llama_token> tokens(full_prompt.size() + 16);
    int n_tokens = llama_tokenize(g_model, full_prompt.c_str(), full_prompt.size(), tokens.data(), tokens.size(), true, true);

    if (n_tokens < 0) {
        tokens.resize(-n_tokens);
        n_tokens = llama_tokenize(g_model, full_prompt.c_str(), full_prompt.size(), tokens.data(), tokens.size(), true, true);
    }
    tokens.resize(n_tokens);

    // Decode prompt
    llama_batch batch = llama_batch_get_one(tokens.data(), tokens.size(), 0, 0);
    if (llama_decode(g_ctx, batch) != 0) {
        return env->NewStringUTF("Failed to decode prompt.");
    }

    std::string response = "";
    int n_cur = tokens.size();

    // Generation loop
    for (int i = 0; i < maxTokens; ++i) {
        auto n_vocab = llama_n_vocab(g_model);
        auto* logits = llama_get_logits_ith(g_ctx, -1);

        std::vector<llama_token_data> candidates;
        candidates.reserve(n_vocab);
        for (llama_token token_id = 0; token_id < n_vocab; token_id++) {
            candidates.push_back(llama_token_data{token_id, logits[token_id], 0.0f});
        }

        llama_token_data_array candidates_p = { candidates.data(), candidates.size(), false };

        // Apply samplers
        llama_sample_repetition_penalties(g_ctx, &candidates_p, tokens.data(), tokens.size(), repeatPenalty, 0.0f, 0.0f);
        llama_sample_top_k(g_ctx, &candidates_p, topK, 1);
        llama_sample_top_p(g_ctx, &candidates_p, topP, 1);
        llama_sample_temp(g_ctx, &candidates_p, temp);

        const llama_token new_token_id = llama_sample_token(g_ctx, &candidates_p);

        if (new_token_id == llama_token_eos(g_model)) {
            break;
        }

        char buf[128];
        int n = llama_token_to_piece(g_model, new_token_id, buf, sizeof(buf), 0, true);
        if (n > 0) {
            response.append(buf, n);
        }

        tokens.push_back(new_token_id);

        batch = llama_batch_get_one(&new_token_id, 1, n_cur, 0);
        if (llama_decode(g_ctx, batch) != 0) {
            break;
        }
        n_cur++;
    }

    return env->NewStringUTF(response.c_str());
}

JNIEXPORT void JNICALL
Java_com_ismet_novafr_llm_LlamaInference_nativeFreeModel(JNIEnv* env, jobject thiz) {
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_free_model(g_model);
        g_model = nullptr;
    }
    llama_backend_free();
}

}
