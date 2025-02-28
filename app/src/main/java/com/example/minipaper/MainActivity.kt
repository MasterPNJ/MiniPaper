package com.example.minipaper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Indique le layout que tu veux afficher
        setContentView(R.layout.activity_main)

        // Récupère ton ImageView
        val imageViewOption = findViewById<ImageView>(R.id.imageViewOption)

        // Ajoute un listener de clic
        imageViewOption.setOnClickListener {
            // Au clic, on lance l'activité OptionsActivity
            val intent = Intent(this, OptionsActivity::class.java)
            startActivity(intent)
        }

        val soloButton = findViewById<ImageView>(R.id.imageView9)
        soloButton.setOnClickListener {
            val intent = Intent(this, CountdownActivity::class.java)
            startActivity(intent)
        }

        val multiButton = findViewById<ImageView>(R.id.imageView10)
        multiButton.setOnClickListener {
            val intent = Intent(this, MultiActivity::class.java)
            startActivity(intent)
        }
    }
}
