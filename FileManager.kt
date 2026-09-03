package com.superbet.app

import android.content.Context
import java.io.File
import java.io.FileOutputStream

class FileManager(private val context: Context) {
    
    fun readLines(): List<String> {
        val file = File(context.filesDir, "notepad.txt")
        if (!file.exists()) {
            return emptyList()
        }
        return file.readLines()
    }
    
    fun saveLines(lines: List<String>) {
        val file = File(context.filesDir, "notepad.txt")
        file.writeText(lines.joinToString("\n"))
    }
}
