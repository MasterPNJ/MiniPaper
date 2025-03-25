package com.example.minipaper

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.option_menu)

        // 1) Récupérer la référence du bouton "Main Menu"
        val mainMenuButton = findViewById<ImageView>(R.id.imageView5)

        // 2) Récupérer les SeekBar
        seekBarMusic = findViewById(R.id.seekBarMusic)
        seekBarBruitage = findViewById(R.id.seekBarBruitage)

        // 3) Charger les préférences
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        // 4) Récupérer la valeur stockée ou 50 par défaut
        val musicVolume = prefs.getInt("musicVolume", 50)
        val bruitageVolume = prefs.getInt("bruitageVolume", 50)

        // 5) Appliquer la valeur initiale aux SeekBar
        seekBarMusic.progress = musicVolume
        seekBarBruitage.progress = bruitageVolume

        // 6) Gérer les changements pour la musique
        seekBarMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Si vous voulez réagir en "temps réel", c'est ici
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Sauvegarder quand l'utilisateur relâche
                if (seekBar != null) {
                    prefs.edit().putInt("musicVolume", seekBar.progress).apply()
                }
            }
        })

        // 7) Gérer les changements pour le bruitage
        seekBarBruitage.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Réagir en temps réel si besoin
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    prefs.edit().putInt("bruitageVolume", seekBar.progress).apply()
                }
            }
        })

        // 8) Clique sur "Main Menu" => revenir à MainActivity
        mainMenuButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
