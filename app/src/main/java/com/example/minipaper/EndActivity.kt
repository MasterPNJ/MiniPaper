package com.example.minipaper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class EndActivity : AppCompatActivity() {
    private lateinit var soundHelper: SoundHelper
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end)

        soundHelper = SoundHelper(this)

        // Initialiser Firebase
        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance(
            "https://mini-paper-db-default-rtdb.europe-west1.firebasedatabase.app/"
        ).getReference("leaderboard")

        // Récupérer la vue "Score :"
        val scoreTextView = findViewById<TextView>(R.id.textView20)

        // Charger le score cumulé depuis SharedPreferences
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val totalScore = sharedPref.getInt("cumulativeScore", 0)

        // Récupérer le pseudo (stocké lors de la première connexion)
        val pseudo = sharedPref.getString("username", "Player") ?: "Player"

        // Mettre à jour le TextView
        scoreTextView.text = "Score : $totalScore"

        // Envoyer le score + pseudo à Firebase
        sendScoreToFirebase(pseudo, totalScore)

        // Gérer le bouton "Main Menu"
        val mainMenuTextView = findViewById<TextView>(R.id.textView22)
        mainMenuTextView.setOnClickListener {
            val volume = PreferenceUtils.getBruitageVolume(this)
            val intent = Intent(this, MainActivity::class.java)
            soundHelper.playSoundAndLaunchActivity(
                context = this,
                volume = volume,
                intent = intent,
                finishActivity = { finish() }
            )
        }
    }

    /**
     * Met à jour les statistiques du joueur dans Firebase.
     * Il ne met à jour le champ best_score que si le nouveau score (newScore) est supérieur
     * au score actuellement stocké, tout en préservant les autres statistiques.
     */
    private fun sendScoreToFirebase(pseudo: String, newScore: Int) {
        val userId = getOrCreateUserId(this)
        val userRef = database.child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Récupérer l'objet Player existant, ou utiliser des valeurs par défaut
                val existingPlayer = snapshot.getValue(Player::class.java)
                val currentBest = existingPlayer?.best_score ?: 0

                // Mettre à jour uniquement si le nouveau score est supérieur
                if (newScore > currentBest) {
                    val updatedPlayer = Player(
                        id = userId,
                        pseudo = pseudo,
                        best_score = newScore,
                        shakeItUp_bestScore = existingPlayer?.shakeItUp_bestScore ?: 0,
                        randomTap_bestScore = existingPlayer?.randomTap_bestScore ?: 0,
                        volumeMaster_bestScore = existingPlayer?.volumeMaster_bestScore ?: 0,
                        volumeMaster_bestTime = existingPlayer?.volumeMaster_bestTime ?: 0f,
                        flappyPaper_bestScore = existingPlayer?.flappyPaper_bestScore ?: 0
                    )
                    userRef.setValue(updatedPlayer)
                }
                // Sinon, ne pas écraser les autres statistiques
            }

            override fun onCancelled(error: DatabaseError) {
                // Gestion de l'erreur si nécessaire
            }
        })
    }

    /**
     * Crée ou récupère un userId unique, stocké dans SharedPreferences.
     */
    fun getOrCreateUserId(context: Context): String {
        val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val existingId = prefs.getString("userId", null)
        return if (existingId == null) {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString("userId", newId).apply()
            newId
        } else {
            existingId
        }
    }
}
