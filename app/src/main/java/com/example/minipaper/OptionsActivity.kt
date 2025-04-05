package com.example.minipaper

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class OptionsActivity : AppCompatActivity() {

    private lateinit var seekBarMusic: SeekBar
    private lateinit var seekBarBruitage: SeekBar
    private lateinit var prefs: SharedPreferences
    private lateinit var soundHelper: SoundHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.option_menu)

        // Initialisation SoundHelper
        soundHelper = SoundHelper(this)

        // Récupérer la référence du bouton "Main Menu"
        val mainMenuButton = findViewById<ImageView>(R.id.imageView5)

        // Récupérer les SeekBar
        seekBarMusic = findViewById(R.id.seekBarMusic)
        seekBarBruitage = findViewById(R.id.seekBarBruitage)

        // Charger les préférences
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val musicVolume = prefs.getInt("musicVolume", 50)
        val bruitageVolume = prefs.getInt("bruitageVolume", 50)

        // Appliquer les valeurs initiales
        seekBarMusic.progress = musicVolume
        seekBarBruitage.progress = bruitageVolume

        // Gérer les changements pour la musique
        seekBarMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    prefs.edit().putInt("musicVolume", it.progress).apply()
                }
            }
        })

        // Gérer les changements pour le bruitage
        seekBarBruitage.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    prefs.edit().putInt("bruitageVolume", it.progress).apply()
                }
            }
        })

        // Exemple : dans le clic sur "Main Menu", jouer le son puis lancer MainActivity
        mainMenuButton.setOnClickListener {
            // Récupérer le volume de bruitage (convertir en valeur 0.0 - 1.0)
            val volume = getBruitageVolume()
            soundHelper.playButtonSound(volume)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getBruitageVolume(): Float {
        val bruitageVolume = prefs.getInt("bruitageVolume", 50)
        return bruitageVolume / 100f
    }

    override fun onDestroy() {
        super.onDestroy()
        soundHelper.release()
    }
}
