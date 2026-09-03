package com.superbet.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.*

class SuperbetAccessibilityService : AccessibilityService() {
    
    private var isActive = false
    private var userTag = ""
    private var tagPosition = 0 // 0=start, 1=middle, 2=end
    private var currentIndex = 0
    private var wordsList = mutableListOf<String>()
    private var isRunning = false
    
    private val settingsManager = SettingsManager(this)
    private val fileManager = FileManager(this)
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        loadWords()
        loadPosition()
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle events if needed
    }
    
    override fun onInterrupt() {
        isActive = false
        isRunning = false
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra("action")) {
            "start" -> {
                userTag = intent.getStringExtra("tag") ?: ""
                tagPosition = intent.getIntExtra("position", 0)
                isActive = true
                if (!isRunning) {
                    startTyping()
                }
            }
            "stop" -> {
                isActive = false
                isRunning = false
            }
        }
        return START_STICKY
    }
    
    private fun startTyping() {
        isRunning = true
        serviceScope.launch {
            writeWords()
        }
    }
    
    private suspend fun writeWords() {
        while (isActive) {
            if (currentIndex >= wordsList.size) {
                currentIndex = 0
            }
            
            for (i in currentIndex until wordsList.size) {
                if (!isActive) break
                
                val word = wordsList[i]
                
                if (word.isEmpty() && userTag.isNotEmpty()) {
                    // Send tag + Enter
                    typeText(userTag)
                    pressEnter()
                } else if (word.isNotEmpty()) {
                    // Send text with tag
                    val textToSend = when (tagPosition) {
                        0 -> "$userTag $word"      // Beginning
                        1 -> {
                            // Middle - insert tag after first word
                            val parts = word.split(" ", limit = 2)
                            if (parts.size > 1) "${parts[0]} $userTag ${parts[1]}" else "$userTag $word"
                        }
                        else -> "$word $userTag"   // End
                    }
                    
                    // Type character by character
                    for (char in textToSend) {
                        if (!isActive) break
                        typeChar(char)
                        delay((20..50).random().toLong()) // 0.02-0.05s
                    }
                    
                    // Shift + Enter
                    pressKey(KeyEvent.KEYCODE_SHIFT_LEFT)
                    pressKey(KeyEvent.KEYCODE_ENTER)
                    releaseKey(KeyEvent.KEYCODE_ENTER)
                    releaseKey(KeyEvent.KEYCODE_SHIFT_LEFT)
                    
                    delay((10..70).random().toLong()) // 0.01-0.07s
                    currentIndex = i + 1
                    settingsManager.savePosition(currentIndex)
                    
                    // Update UI via broadcast
                    sendBroadcast(Intent("UPDATE_PREVIEW").apply {
                        putExtra("text", textToSend)
                    })
                    sendBroadcast(Intent("UPDATE_COUNTER"))
                }
                
                // Random delay between lines
                delay((50..400).random().toLong()) // 0.05-0.4s
            }
        }
        isRunning = false
    }
    
    private fun typeChar(char: Char) {
        val event = KeyEvent(
            KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_UNKNOWN,
            0,
            0,
            char.code,
            0,
            char.code
        )
        // Use dispatch gesture for typing
        performGlobalAction(GLOBAL_ACTION_ACCESSIBILITY_ALL)
    }
    
    private fun typeText(text: String) {
        for (char in text) {
            typeChar(char)
            delay((20..50).random().toLong())
        }
    }
    
    private fun pressEnter() {
        pressKey(KeyEvent.KEYCODE_ENTER)
        releaseKey(KeyEvent.KEYCODE_ENTER)
    }
    
    private fun pressKey(keyCode: Int) {
        val event = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        // Send key event
    }
    
    private fun releaseKey(keyCode: Int) {
        val event = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        // Send key event
    }
    
    private fun loadWords() {
        wordsList = fileManager.readLines().toMutableList()
        if (wordsList.isEmpty()) {
            // Create default file
            val defaultWords = listOf(
                "Bună ziua!",
                "Cum mai ești?",
                "Sper că ești bine.",
                "Am o întrebare pentru tine.",
                "Vrei să discutăm?",
                "Aștept răspunsul tău.",
                "Mulțumesc!",
                "La revedere!"
            )
            fileManager.saveLines(defaultWords)
            wordsList = defaultWords.toMutableList()
        }
    }
    
    private fun loadPosition() {
        currentIndex = settingsManager.getPosition()
    }
    
    private suspend fun delay(time: Long) {
        delay(time)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
