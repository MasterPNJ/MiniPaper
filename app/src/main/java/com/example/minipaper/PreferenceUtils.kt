package com.example.minipaper

import android.content.Context
import java.util.UUID

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

    fun makeUserKey(context: Context): String {
        val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        // récupère ou génère l'UUID lié au téléphone
        val uuid = prefs.getString("userId", null)
            ?: UUID.randomUUID().also {
                prefs.edit().putString("userId", it.toString()).apply()
            }.toString()
        // récupère le pseudo, ou "Player" par défaut
        val pseudo = prefs.getString("username", "Player")!!
        // sécurise le pseudo pour l’utiliser dans une clé Firebase
        val safePseudo = pseudo.replace("\\s+".toRegex(), "_")
        return "${uuid}_$safePseudo"
    }
}