package com.example.minipaper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SoloGameControllerActivity : AppCompatActivity() {

    // Liste des mini-jeux disponibles (assurez-vous qu'ils sont déclarés dans le manifeste)
    private val gamesList = listOf(
        RandomtapActivity::class.java,
        ShakeItUpActivity::class.java,
        VolumeMasterActivity::class.java
    )

    // Pour éviter de jouer deux fois de suite le même jeu
    private var lastGame: Class<*>? = null

    // Nombre de mini-jeux joués (excluant le cooldown)
    private var gamesPlayed = 0
    private val totalGames = 5

    // Flag indiquant que le cooldown initial a été effectué
    private var cooldownDone = false

    companion object {
        const val REQUEST_GAME = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Optionnel : affichez un layout de chargement ou laissez vide
        launchNextGame()
    }

    private fun launchNextGame() {
        when {
            // Tous les mini-jeux ont été joués, lancer EndActivity
            gamesPlayed >= totalGames -> {
                startActivity(Intent(this, EndActivity::class.java))
                finish()
            }
            // Si le cooldown n'a pas encore été fait, lancer CountdownActivity
            !cooldownDone -> {
                val intent = Intent(this, CountdownActivity::class.java)
                startActivityForResult(intent, REQUEST_GAME)
            }
            // Sinon, choisir un mini-jeu aléatoire différent du précédent
            else -> {
                val availableGames = gamesList.filter { it != lastGame }
                val nextGame = availableGames.random()
                lastGame = nextGame
                gamesPlayed++
                val intent = Intent(this, nextGame)
                startActivityForResult(intent, REQUEST_GAME)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Si l'activité annulée (par exemple, le countdown a été cancelé)
        if (resultCode != RESULT_OK) {
            // Retourner au menu principal (ou gérer autrement l'annulation)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Si on reçoit RESULT_OK du cooldown, on marque que le cooldown est terminé
        if (!cooldownDone) {
            cooldownDone = true
        }
        // Lancer le prochain jeu
        launchNextGame()
    }
}
