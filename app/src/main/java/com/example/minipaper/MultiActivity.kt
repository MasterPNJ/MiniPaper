package com.example.minipaper

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MultiActivity : AppCompatActivity() {
    private lateinit var soundHelper: SoundHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundHelper = SoundHelper(this)

        setContentView(R.layout.multi_menu)

        // Récupération de l'ImageView "Main Menu"
        val mainMenuButton = findViewById<ImageView>(R.id.imageView24)

        // Définir le listener pour le clic
        mainMenuButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val volume = PreferenceUtils.getBruitageVolume(this)

            soundHelper.playSoundAndLaunchActivity(
                context = this,
                volume = volume,
                intent = intent,
                finishActivity = { finish() }
            )
        }
    }
}