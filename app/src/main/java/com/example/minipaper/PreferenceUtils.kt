package com.example.minipaper

import android.content.Context

object PreferenceUtils {
    
    fun getBruitageVolume(context: Context): Float {
        val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val bruitageVolume = prefs.getInt("bruitageVolume", 50)
        return bruitageVolume / 100f
    }

    fun getMusicVolume(context: Context): Float {
        val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val musicVolume = prefs.getInt("musicVolume", 50)
        return musicVolume / 100f
    }
}