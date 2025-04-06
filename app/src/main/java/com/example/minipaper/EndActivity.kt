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

        // Initialiser Firebase (si pas déjà fait dans l'Application)
        FirebaseApp.initializeApp(this)
        // Récupérer la référence "leaderboard" dans la base
        database = FirebaseDatabase.getInstance(
            "https://mini-paper-db-default-rtdb.europe-west1.firebasedatabase.app/"
        ).getReference("leaderboard")

        // Récupérer la vue "Score :"
        val scoreTextView = findViewById<TextView>(R.id.textView20)

        // Charger le score cumulé depuis les SharedPreferences
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val totalScore = sharedPref.getInt("cumulativeScore", 0)

        // Récupérer le pseudo (en supposant qu'il est stocké sous la clé "username")
        val pseudo = sharedPref.getString("username", "Player") ?: "Player"

        // Mettre à jour le TextView
        scoreTextView.text = "Score : $totalScore"

        // Envoyer le score + pseudo à Firebase en utilisant un ID unique
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
     * Envoie le score + pseudo dans Firebase sous /leaderboard/{userId},
     * où userId est unique et stocké localement.
     */
    private fun sendScoreToFirebase(pseudo: String, newScore: Int) {
        // Récupérer l'ID unique local
        val userId = getOrCreateUserId(this)

        // Récupérer la référence /leaderboard/userId
        val userRef = database.child(userId)

        // Lire la valeur actuelle (s’il y en a une)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convertir le snapshot en Player (ou null s’il n’existe pas encore)
                val existingPlayer = snapshot.getValue(Player::class.java)
                val currentBest = existingPlayer?.best_score ?: 0

                // Comparer avec le nouveau score
                if (newScore > currentBest) {
                    // Nouveau score plus élevé, on met à jour
                    val updatedPlayer = Player(
                        id = userId,
                        pseudo = pseudo,
                        best_score = newScore
                    )
                    userRef.setValue(updatedPlayer)
                }
                // Sinon, on ne fait rien (le score existant est déjà meilleur)
            }

            override fun onCancelled(error: DatabaseError) {
                // Gérer l'erreur, par exemple :
                // Toast.makeText(this@EndActivity, "Erreur : ${error.message}", Toast.LENGTH_SHORT).show()
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
            // Générer un nouvel ID (UUID)
            val newId = UUID.randomUUID().toString()
            // Sauvegarder dans SharedPreferences
            prefs.edit().putString("userId", newId).apply()
            newId
        } else {
            existingId
        }
    }
}
