package com.example.minipaper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

class StatisticsActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var bestScoreTextView: TextView
    private lateinit var bestRandomTapTextView: TextView
    private lateinit var bestShakeItUpTextView: TextView
    private lateinit var bestVolumeMasterTextView: TextView

    private lateinit var soundHelper: SoundHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        // Initialisation des vues
        bestScoreTextView = findViewById(R.id.BestScore)
        bestRandomTapTextView = findViewById(R.id.bestrandomtap)
        bestShakeItUpTextView = findViewById(R.id.BestShakeItUp)
        bestVolumeMasterTextView = findViewById(R.id.textView31)

        soundHelper = SoundHelper(this)

        // Initialiser Firebase (si ce n'est pas déjà fait)
        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance("https://mini-paper-db-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("leaderboard")

        // Charger les statistiques du joueur actuel depuis Firebase
        loadPlayerStats()

        val returnButton = findViewById<ImageView>(R.id.imageView37)

        // Définir le listener pour le clic
        returnButton.setOnClickListener {
            val intent = Intent(this, LeaderbordActivity::class.java)
            val volume = PreferenceUtils.getBruitageVolume(this)

            soundHelper.playSoundAndLaunchActivity(
                context = this,
                volume = volume,
                intent = intent,
                finishActivity = { finish() }
            )
        }
    }

    private fun loadPlayerStats() {
        // Récupérer l'ID utilisateur unique stocké dans SharedPreferences
        val userId = getOrCreateUserId(this)
        val userRef = database.child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val player = snapshot.getValue(Player::class.java)
                if (player != null) {
                    // Mettre à jour les TextViews avec les statistiques récupérées
                    bestScoreTextView.text = "Best score (global) : ${player.best_score}"
                    bestRandomTapTextView.text = "Best score RandomTap : ${player.randomTap_bestScore}"
                    bestShakeItUpTextView.text = "Best score ShakeItUp : ${player.shakeItUp_bestScore}"
                    bestVolumeMasterTextView.text = "Best score VolumeMaster : ${player.volumeMaster_bestScore}"
                } else {
                    bestScoreTextView.text = "Aucune statistique disponible"
                    bestRandomTapTextView.text = ""
                    bestShakeItUpTextView.text = ""
                    bestVolumeMasterTextView.text = ""
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StatisticsActivity, "Erreur de chargement: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Récupère ou crée un identifiant utilisateur unique stocké dans SharedPreferences.
     */
    private fun getOrCreateUserId(context: Context): String {
        val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val existingId = prefs.getString("userId", null)
        return if (existingId == null) {
            val newId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("userId", newId).apply()
            newId
        } else {
            existingId
        }
    }
}
