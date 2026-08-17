package com.ismet.novafr

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))

        val logFile = File(context.filesDir, "last_crash.txt")
        logFile.writeText(sw.toString())

        defaultHandler?.uncaughtException(thread, throwable)
    }
}
