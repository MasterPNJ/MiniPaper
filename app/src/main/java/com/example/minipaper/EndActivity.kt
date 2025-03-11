package com.example.minipaper

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EndActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end)

        // Récupérer la vue "Score :"
        val scoreTextView = findViewById<TextView>(R.id.textView20)

        // Charger le score cumulé depuis les SharedPreferences
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val totalScore = sharedPref.getInt("cumulativeScore", 0)

        // Mettre à jour le TextView
        scoreTextView.text = "Score : $totalScore"

        // Gérer le bouton "Main Menu" (si vous en avez un dans le layout, ex: textView22)
        val mainMenuTextView = findViewById<TextView>(R.id.textView22)
        mainMenuTextView.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
