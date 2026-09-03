package com.superbet.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var statusText: TextView
    private lateinit var statusDot: View
    private lateinit var previewText: TextView
    private lateinit var positionText: TextView
    private lateinit var counterText: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var panicButton: Button
    private lateinit var stealthButton: Button
    private lateinit var resetButton: Button
    private lateinit var userTagInput: EditText
    private lateinit var positionSpinner: Spinner
    private lateinit var settingsPanel: LinearLayout
    private lateinit var settingsToggle: Button
    
    private val settingsManager = SettingsManager(this)
    private val fileManager = FileManager(this)
    private var isActive = false
    private var messageCount = 0
    private var currentIndex = 0
    private var isStealth = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupListeners()
        loadSettings()
        updateStatus()
        checkAccessibility()
        startVolumeService()
        updatePositionInfo()
    }
    
    private fun initViews() {
        statusText = findViewById(R.id.statusText)
        statusDot = findViewById(R.id.statusDot)
        previewText = findViewById(R.id.previewText)
        positionText = findViewById(R.id.positionText)
        counterText = findViewById(R.id.counterText)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        panicButton = findViewById(R.id.panicButton)
        stealthButton = findViewById(R.id.stealthButton)
        resetButton = findViewById(R.id.resetButton)
        userTagInput = findViewById(R.id.userTagInput)
        positionSpinner = findViewById(R.id.positionSpinner)
        settingsPanel = findViewById(R.id.settingsPanel)
        settingsToggle = findViewById(R.id.settingsToggle)
        
        // Setup spinner
        val positions = arrayOf("Început", "Mijloc", "Sfârșit")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, positions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        positionSpinner.adapter = adapter
    }
    
    private fun setupListeners() {
        startButton.setOnClickListener { startTyping() }
        stopButton.setOnClickListener { stopTyping() }
        panicButton.setOnClickListener { panicStop() }
        stealthButton.setOnClickListener { toggleStealth() }
        resetButton.setOnClickListener { resetPosition() }
        settingsToggle.setOnClickListener {
            settingsPanel.visibility = if (settingsPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            settingsToggle.text = if (settingsPanel.visibility == View.VISIBLE) "▲ Setări" else "▼ Setări"
        }
    }
    
    private fun startTyping() {
        isActive = true
        saveSettings()
        updateStatus()
        updatePositionInfo()
        
        val intent = Intent(this, SuperbetAccessibilityService::class.java)
        intent.putExtra("action", "start")
        intent.putExtra("tag", userTagInput.text.toString())
        intent.putExtra("position", positionSpinner.selectedItemPosition)
        startService(intent)
    }
    
    private fun stopTyping() {
        isActive = false
        updateStatus()
        
        val intent = Intent(this, SuperbetAccessibilityService::class.java)
        intent.putExtra("action", "stop")
        startService(intent)
    }
    
    private fun panicStop() {
        isActive = false
        updateStatus()
        Toast.makeText(this, "Panică activată!", Toast.LENGTH_SHORT).show()
    }
    
    private fun toggleStealth() {
        isStealth = !isStealth
        if (isStealth) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            stealthButton.text = "Mod Normal"
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            stealthButton.text = " Mod Stealth"
        }
    }
    
    private fun resetPosition() {
        settingsManager.savePosition(0)
        currentIndex = 0
        updatePositionInfo()
        Toast.makeText(this, "Poziție resetată", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateStatus() {
        if (isActive) {
            statusText.text = "ACTIV"
            statusText.setTextColor(ContextCompat.getColor(this, R.color.superbet_green))
            statusDot.setBackgroundColor(ContextCompat.getColor(this, R.color.superbet_green))
        } else {
            statusText.text = "OPRIT"
            statusText.setTextColor(ContextCompat.getColor(this, R.color.superbet_red))
            statusDot.setBackgroundColor(ContextCompat.getColor(this, R.color.superbet_red))
        }
    }
    
    private fun updatePositionInfo() {
        val lines = fileManager.readLines()
        currentIndex = settingsManager.getPosition()
        positionText.text = "Poziție: ${currentIndex + 1}/${lines.size}"
    }
    
    private fun loadSettings() {
        userTagInput.setText(settingsManager.getTag())
        positionSpinner.setSelection(settingsManager.getTagPosition())
    }
    
    private fun saveSettings() {
        settingsManager.saveTag(userTagInput.text.toString())
        settingsManager.saveTagPosition(positionSpinner.selectedItemPosition)
    }
    
    private fun checkAccessibility() {
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, " Activați Accessibility Service pentru Superbet", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "${packageName}/${SuperbetAccessibilityService::class.java.canonicalName}"
        try {
            val settings = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            return settings?.contains(service) ?: false
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun startVolumeService() {
        val intent = Intent(this, VolumeControlService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    fun updatePreview(text: String) {
        runOnUiThread {
            previewText.text = text.takeLast(50)
        }
    }
    
    fun updateCounter() {
        messageCount++
        runOnUiThread {
            counterText.text = "Mesaje: $messageCount"
            updatePositionInfo()
        }
    }
}
