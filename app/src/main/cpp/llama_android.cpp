#include <jni.h>
#include <string>
#include <vector>
#include "llama.h"

static llama_model* g_model = nullptr;
static llama_context* g_ctx = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_novafr_app_NativeLib_nativeLoadModel(JNIEnv* env, jobject thiz, jstring model_path) {
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
Java_com_novafr_app_NativeLib_nativeGenerate(JNIEnv* env, jobject thiz, jstring prompt) {
    if (!g_model || !g_ctx) {
        return env->NewStringUTF("Error: Model not loaded in C++ engine.");
    }

    const char* prompt_str = env->GetStringUTFChars(prompt, nullptr);
    std::string text_prompt(prompt_str);
    env->ReleaseStringUTFChars(prompt, prompt_str);

    const llama_vocab* vocab = llama_model_get_vocab(g_model);

    // Tokenize prompt
    const int n_prompt_max = text_prompt.size() + 128;
    std::vector<llama_token> tokens(n_prompt_max);
    
    int n_tokens = llama_tokenize(
        vocab, 
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
            vocab, 
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

    // Prepare initial batch
    llama_batch batch = llama_batch_get_one(tokens.data(), tokens.size());
    if (llama_decode(g_ctx, batch) != 0) {
        return env->NewStringUTF("Error: Initial decode failed.");
    }

    // Configure sampler chain
    llama_sampler* sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(0.7f));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));

    std::string output = "";
    const int max_tokens = 256;

    for (int i = 0; i < max_tokens; i++) {
        llama_token new_token_id = llama_sampler_sample(sampler, g_ctx, -1);

        if (llama_vocab_is_eog(vocab, new_token_id)) {
            break;
        }

        char buf[128];
        int n = llama_token_to_piece(vocab, new_token_id, buf, sizeof(buf), 0, true);
        if (n > 0) {
            output.append(buf, n);
        }

        batch = llama_batch_get_one(&new_token_id, 1);
        if (llama_decode(g_ctx, batch) != 0) {
            break;
        }
    }

    llama_sampler_free(sampler);

    return env->NewStringUTF(output.c_str());
}

}
