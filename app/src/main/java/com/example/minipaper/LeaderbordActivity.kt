package com.example.minipaper

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
class LeaderbordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Assurez-vous que "option" correspond bien au nom de votre fichier XML dans res/layout
        setContentView(R.layout.leaderbord_menu)

        // Récupération de l'ImageView "Main Menu"
        val mainMenuButton = findViewById<ImageView>(R.id.imageView4)

        // Définir le listener pour le clic
        mainMenuButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Ferme l'activité actuelle
        }
    }
}
