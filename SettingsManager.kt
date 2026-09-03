package com.superbet.app

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("superbet_settings", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_USER_TAG = "user_tag"
        private const val KEY_TAG_POSITION = "tag_position"
        private const val KEY_CURRENT_INDEX = "current_index"
        private const val KEY_DELAY = "delay"
        private const val KEY_FILE_NAME = "file_name"
    }
    
    fun getTag(): String = prefs.getString(KEY_USER_TAG, "") ?: ""
    fun saveTag(tag: String) = prefs.edit().putString(KEY_USER_TAG, tag).apply()
    
    fun getTagPosition(): Int = prefs.getInt(KEY_TAG_POSITION, 0)
    fun saveTagPosition(position: Int) = prefs.edit().putInt(KEY_TAG_POSITION, position).apply()
    
    fun getPosition(): Int = prefs.getInt(KEY_CURRENT_INDEX, 0)
    fun savePosition(index: Int) = prefs.edit().putInt(KEY_CURRENT_INDEX, index).apply()
    
    fun getDelay(): Float = prefs.getFloat(KEY_DELAY, 0.05f)
    fun saveDelay(delay: Float) = prefs.edit().putFloat(KEY_DELAY, delay).apply()
    
    fun getFileName(): String = prefs.getString(KEY_FILE_NAME, "notepad.txt") ?: "notepad.txt"
    fun saveFileName(name: String) = prefs.edit().putString(KEY_FILE_NAME, name).apply()
}
