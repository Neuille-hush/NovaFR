package com.ismet.novafr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ismet.novafr.ui.ChatScreen
import com.ismet.novafr.ui.ChatViewModel
import com.ismet.novafr.ui.theme.NovaFRTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register crash handler FIRST, before anything else can crash
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(applicationContext))

        // If a crash happened last run, show it instead of the normal app
        val crashFile = File(filesDir, "last_crash.txt")
        if (crashFile.exists()) {
            setContent {
                NovaFRTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Last crash log:", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(crashFile.readText())
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                crashFile.delete()
                                recreate()
                            }) {
                                Text("Clear and continue")
                            }
                        }
                    }
                }
            }
            return
        }

        setContent {
            NovaFRTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isDownloading by viewModel.isDownloading.collectAsState()
                    val downloadProgress by viewModel.downloadProgress.collectAsState()
                    val modelReady by viewModel.modelReady.collectAsState()

                    when {
                        isDownloading -> {
                            DownloadScreen(progress = downloadProgress)
                        }
                        modelReady -> {
                            ChatScreen(viewModel = viewModel)
                        }
                        else -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadScreen(progress: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Downloading AI Model...",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$progress%",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
