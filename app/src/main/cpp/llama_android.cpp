#include <jni.h>
#include <string>
#include <vector>
#include "llama.h"

static llama_model* g_model = nullptr;
static llama_context* g_ctx = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_ismet_novafr_NativeLib_nativeLoadModel(JNIEnv* env, jobject thiz, jstring model_path) {
    const char* path = env->GetStringUTFChars(model_path, nullptr);

    llama_backend_init();

    llama_model_params model_params = llama_model_default_params();
    g_model = llama_model_load_from_file(path, model_params);

    env->ReleaseStringUTFChars(model_path, path);

    if (!g_model) {
        return JNI_FALSE;
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 2048;
    ctx_params.n_threads = 4;

    g_ctx = llama_init_from_model(g_model, ctx_params);
    return (g_ctx != nullptr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_com_ismet_novafr_NativeLib_nativeGenerate(JNIEnv* env, jobject thiz, jstring prompt) {
    if (!g_model || !g_ctx) {
        return env->NewStringUTF("Error: Model not loaded in C++ engine.");
    }

    const char* prompt_str = env->GetStringUTFChars(prompt, nullptr);
    std::string text_prompt(prompt_str);
    env->ReleaseStringUTFChars(prompt, prompt_str);

    // Tokenize prompt
    const int n_prompt_max = text_prompt.size() + 128;
    std::vector<llama_token> tokens(n_prompt_max);
    
    int n_tokens = llama_tokenize(
        g_model, 
        text_prompt.c_str(), 
        text_prompt.size(), 
        tokens.data(), 
        tokens.size(), 
        true, 
        true
    );

    if (n_tokens < 0) {
        tokens.resize(-n_tokens);
        n_tokens = llama_tokenize(
            g_model, 
            text_prompt.c_str(), 
            text_prompt.size(), 
            tokens.data(), 
            tokens.size(), 
            true, 
            true
        );
    }

    if (n_tokens < 0) {
        return env->NewStringUTF("Error: Tokenization failed.");
    }

    tokens.resize(n_tokens);

    // Initial batch (tokens, n_tokens, pos_0, seq_id)
    llama_batch batch = llama_batch_get_one(tokens.data(), tokens.size(), 0, 0);
    if (llama_decode(g_ctx, batch) != 0) {
        return env->NewStringUTF("Error: Initial decode failed.");
    }

    std::string output = "";
    const int max_tokens = 256;
    int n_vocab = llama_n_vocab(g_model);
    int curr_pos = n_tokens;

    for (int i = 0; i < max_tokens; i++) {
        float* logits = llama_get_logits_ith(g_ctx, -1);

        std::vector<llama_token_data> candidates;
        candidates.reserve(n_vocab);
        for (llama_token token_id = 0; token_id < n_vocab; token_id++) {
            candidates.push_back({token_id, logits[token_id], 0.0f});
        }

        llama_token_data_array cur_p = { candidates.data(), candidates.size(), false };
        llama_sample_temp(g_ctx, &cur_p, 0.7f);
        llama_token new_token_id = llama_sample_token_greedy(g_ctx, &cur_p);

        if (new_token_id == llama_token_eos(g_model)) {
            break;
        }

        char buf[128];
        int n = llama_token_to_piece(g_model, new_token_id, buf, sizeof(buf), 0, true);
        if (n > 0) {
            output.append(buf, n);
        }

        // Single token batch update (token_ptr, count, position, seq_id)
        batch = llama_batch_get_one(&new_token_id, 1, curr_pos, 0);
        curr_pos++;

        if (llama_decode(g_ctx, batch) != 0) {
            break;
        }
    }

    return env->NewStringUTF(output.c_str());
}

}
