package com.ismet.novafr.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ismet.novafr.data.ChatDatabase
import com.ismet.novafr.data.Message
import com.ismet.novafr.data.Sender
import com.ismet.novafr.llm.LlamaInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ChatDatabase.getInstance(application).messageDao()
    private val llm = LlamaInference.getInstance()

    private val _messagesFlow = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messagesFlow.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _modelReady = MutableStateFlow(false)
    val modelReady: StateFlow<Boolean> = _modelReady.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    // ⚠️ Update this with your Hugging Face model URL when ready ⚠️
    private val modelUrl = "https://huggingface.co/username/repository/resolve/main/Atlas-FirstAid.Q4_K_M.gguf"

    init {
        viewModelScope.launch {
            dao.getAllMessages().collect { history ->
                _messagesFlow.value = history
            }
        }
        initializeModel(application)
    }

    private fun initializeModel(application: Application) {
        viewModelScope.launch {
            val targetFile = File(application.filesDir, LlamaInference.MODEL_FILENAME)

            if (!targetFile.exists()) {
                _isDownloading.value = true
                val success = downloadModel(targetFile)
                _isDownloading.value = false
                if (!success) return@launch
            }

            val success = llm.loadModel(targetFile.absolutePath)
            _modelReady.value = success
        }
    }

    private suspend fun downloadModel(targetFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(modelUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext false
            }

            val fileLength = connection.contentLength
            val input = connection.inputStream
            val output = FileOutputStream(targetFile)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int

            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                if (fileLength > 0) {
                    _downloadProgress.value = ((total * 100) / fileLength).toInt()
                }
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            if (targetFile.exists()) targetFile.delete()
            false
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() || _isGenerating.value || !_modelReady.value) return

        viewModelScope.launch {
            dao.insert(Message(content = userText, sender = Sender.USER))
            _isGenerating.value = true

            val history = dao.getAllMessages().first()
                .takeLast(10)
                .map { msg ->
                    val role = if (msg.sender == Sender.USER) "user" else "assistant"
                    role to msg.content
                }

            val response = llm.generateResponse(history, userText)

            dao.insert(Message(content = response, sender = Sender.AI))
            _isGenerating.value = false
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        llm.release()
    }
}
