package com.example.minipaper // Adaptez selon votre package

import android.content.Intent
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.random.Random

class FlappyPapierActivity : AppCompatActivity() {

    // Vues principales
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var avion: ImageView
    private lateinit var regleTop: ImageView
    private lateinit var regleBottom: ImageView
    private lateinit var scoreText: TextView

    // Dimensions de l'écran
    private var screenWidth = 0
    private var screenHeight = 0

    // Position & physique de l'avion
    private var avionY = 0f
    private var velocity = 0f
    private val gravity = 1.2f
    private val jumpForce = -20f

    // Position horizontale des règles
    private var regleX = 0f
    private val gap = 350f

    // Gestion du score
    private var score = 0
    private var hasScored = false // Pour éviter d'incrémenter plusieurs fois par passage

    // Boucle de jeu
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 20L // 20 ms => ~50 FPS

    // État du jeu
    private var isGameOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flappy_papier)

        // 1) Récupérer les vues
        rootLayout = findViewById(R.id.rootConstraintFlappy)
        avion = findViewById(R.id.avionenpapier)
        regleTop = findViewById(R.id.regle1)
        regleBottom = findViewById(R.id.regle2)
        scoreText = findViewById(R.id.scoreFlappy)

        // 2) Attendre la mesure du layout pour connaître la taille de l'écran
        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            rootLayout.viewTreeObserver.removeOnGlobalLayoutListener { }

            screenWidth = rootLayout.width
            screenHeight = rootLayout.height

            // Initialiser l'avion au centre vertical
            avionY = (screenHeight / 2).toFloat()
            avion.y = avionY

            // Initialiser la position horizontale des règles (hors de l'écran à droite)
            regleX = screenWidth.toFloat()

            // Placer les règles de façon aléatoire
            randomizeReglesPosition()

            // Lancer la boucle de jeu
            startGameLoop()
        }

        // 3) Quand on touche l'écran, l'avion saute
        rootLayout.setOnClickListener {
            jump()
        }
    }

    /**
     * Boucle de jeu ~50 FPS.
     */
    private fun startGameLoop() {
        handler.post(object : Runnable {
            override fun run() {
                if (!isGameOver) {
                    updateGame()
                    handler.postDelayed(this, updateInterval)
                }
            }
        })
    }

    /**
     * L'avion monte quand on "tape" l'écran.
     */
    private fun jump() {
        velocity = jumpForce
    }

    /**
     * Met à jour la physique de l'avion, le déplacement des règles, le score, et la collision.
     */
    private fun updateGame() {
        // 1) Physique de l'avion
        velocity += gravity
        avionY += velocity

        // Empêche l'avion de sortir par le haut
        if (avionY < 0) {
            avionY = 0f
            velocity = 0f
        }
        // Game Over si on touche le bas
        if (avionY + avion.height > screenHeight) {
            avionY = (screenHeight - avion.height).toFloat()
            gameOver()
        }

        avion.y = avionY

        // 2) Déplacement horizontal des règles
        regleX -= 6f
        if (regleX < -regleTop.width) {
            // Quand les règles sortent à gauche, on les remet à droite
            regleX = screenWidth.toFloat()
            randomizeReglesPosition()
        }

        regleTop.x = regleX
        regleBottom.x = regleX

        // 3) Incrémenter le score si l'avion dépasse les règles
        // Condition : la partie droite des règles < position X de l'avion
        if (!hasScored && (regleX + regleTop.width) < avion.x) {
            score++
            scoreText.text = "Score : $score"
            hasScored = true
        }

        // 4) Vérifier la collision
        if (checkCollision(avion, regleTop) || checkCollision(avion, regleBottom)) {
            gameOver()
        }
    }

    /**
     * Calcul d'un rectangle plus petit pour l'avion, et normal pour les règles.
     */
    private fun checkCollision(viewAvion: View, viewObstacle: View): Boolean {
        // Ajustez offsetX / offsetY selon la forme de l'avion
        val offsetX = 10
        val offsetY = 10

        val rAvion = RectF(
            viewAvion.x + offsetX,
            viewAvion.y + offsetY,
            viewAvion.x + viewAvion.width - offsetX,
            viewAvion.y + viewAvion.height - offsetY
        )

        val rObstacle = RectF(
            viewObstacle.x,
            viewObstacle.y,
            viewObstacle.x + viewObstacle.width,
            viewObstacle.y + viewObstacle.height
        )

        return rAvion.intersect(rObstacle)
    }

    /**
     * Position verticale aléatoire pour laisser un 'gap' de passage.
     * Reset du booléen hasScored à false pour la nouvelle paire.
     */
    private fun randomizeReglesPosition() {
        val minTop = 50
        val maxTop = screenHeight - gap - 50
        val topRuleY = Random.nextInt(minTop, maxTop.coerceAtLeast(minTop.toFloat()).toInt())

        regleTop.y = (topRuleY - regleTop.height).toFloat()
        regleBottom.y = (topRuleY + gap)
        hasScored = false
    }

    /**
     * Fin de partie : on affiche le score et on retourne au menu (MainActivity).
     */
    private fun gameOver() {
        isGameOver = true
        Toast.makeText(this, "Game Over! Score final : $score", Toast.LENGTH_LONG).show()

        // Retour au menu principal après un court délai
        handler.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500)
    }
}
