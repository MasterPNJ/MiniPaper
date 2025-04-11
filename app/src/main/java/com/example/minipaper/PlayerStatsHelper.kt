package com.example.minipaper

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.util.UUID

// Modèle de données pour un joueur.
data class Player(
    val id: String = "",
    val pseudo: String = "",
    val best_score: Int = 0,
    val shakeItUp_bestScore: Int = 0,
    val randomTap_bestScore: Int = 0,
    val volumeMaster_bestScore: Int = 0,
    val volumeMaster_bestTime: Float = 0f
)

/**
 * Objet utilitaire pour mettre à jour les statistiques d'un joueur dans Firebase.
 *
 * @param context Le contexte (pour accéder aux SharedPreferences).
 * @param database La référence Firebase vers le noeud "leaderboard".
 * @param game Le nom du mini‑jeu ("shakeItUp", "randomTap", "volumeMaster").
 * @param newScore Le nouveau score obtenu dans ce mini‑jeu.
 * @param newTime (Optionnel) Pour VolumeMaster, le temps (en secondes) mis pour réussir.
 */
object PlayerStatsHelper {

    fun updatePlayerStats(
        context: Context,
        database: DatabaseReference,
        game: String,
        newScore: Int,
        newTime: Float? = null
    ) {
        val userId = getOrCreateUserId(context)
        val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val pseudo = prefs.getString("username", "Player") ?: "Player"

        val userRef = database.child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val existingPlayer = snapshot.getValue(Player::class.java)

                // Récupérer les statistiques existantes, ou utiliser 0/Float.MAX_VALUE par défaut.
                val currentShakeScore = existingPlayer?.shakeItUp_bestScore ?: 0
                val currentRandomScore = existingPlayer?.randomTap_bestScore ?: 0
                val currentVolumeScore = existingPlayer?.volumeMaster_bestScore ?: 0
                val currentVolumeTime = existingPlayer?.volumeMaster_bestTime ?: Float.MAX_VALUE
                val globalBest = existingPlayer?.best_score ?: 0

                // Mettre à jour selon le jeu concerné
                val updatedShakeScore = if (game == "shakeItUp" && newScore > currentShakeScore) newScore else currentShakeScore
                val updatedRandomScore = if (game == "randomTap" && newScore > currentRandomScore) newScore else currentRandomScore
                val updatedVolumeScore = if (game == "volumeMaster" && newScore > currentVolumeScore) newScore else currentVolumeScore
                val updatedVolumeTime = if (game == "volumeMaster" && newTime != null && newTime < currentVolumeTime) newTime else currentVolumeTime

                // On conserve globalBest sans modification ici, sauf si vous souhaitez le calculer différemment.
                val updatedPlayer = Player(
                    id = userId,
                    pseudo = pseudo,
                    best_score = globalBest,
                    shakeItUp_bestScore = updatedShakeScore,
                    randomTap_bestScore = updatedRandomScore,
                    volumeMaster_bestScore = updatedVolumeScore,
                    volumeMaster_bestTime = updatedVolumeTime
                )
                userRef.setValue(updatedPlayer)
            }

            override fun onCancelled(error: DatabaseError) {
                // Vous pouvez loguer l'erreur ou afficher un message ici.
            }
        })
    }

    /**
     * Crée ou récupère un userId unique stocké dans SharedPreferences.
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
