package com.example.minipaper

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.FirebaseApp

class LeaderbordActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var scoreListView: TextView
    private lateinit var soundHelper: SoundHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.leaderbord_menu)

        soundHelper = SoundHelper(this)

        // Initialiser Firebase Database
        database = FirebaseDatabase.getInstance("https://mini-paper-db-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("leaderboard")

        // Récupérer l'élément TextView
        scoreListView = findViewById(R.id.listeScore)

        // Charger les scores
        loadScores()

        // Récupération de l'ImageView "Main Menu"
        val mainMenuButton = findViewById<ImageView>(R.id.imageView4)

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

        val statisticsMenu = findViewById<ImageView>(R.id.imageView34)

        statisticsMenu.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            val volume = PreferenceUtils.getBruitageVolume(this)

            soundHelper.playSoundAndLaunchActivity(
                context = this,
                volume = volume,
                intent = intent,
                finishActivity = { finish() }
            )
        }
    }

    private fun loadScores() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = mutableListOf<Player>()

                // Récupérer les joueurs et les ajouter à la liste
                for (scoreSnapshot in snapshot.children) {
                    val player = scoreSnapshot.getValue(Player::class.java)
                    player?.let {
                        players.add(it)
                    }
                }

                // Trier les joueurs par score décroissant
                val sortedPlayers = players.sortedByDescending { it.best_score }

                // Construire la liste des scores à afficher
                val scores = sortedPlayers.map { "${it.pseudo} - Score: ${it.best_score}" }

                // Afficher les scores dans le TextView
                scoreListView.text = scores.joinToString("\n")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    applicationContext,
                    "Erreur de chargement des scores : ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}