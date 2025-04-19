package com.example.minipaper

import android.content.Intent
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.random.Random

class FlappyPapierActivity : AppCompatActivity() {

    private lateinit var rootLayout: ConstraintLayout
    private lateinit var avion: ImageView
    private lateinit var regleTop: ImageView
    private lateinit var regleBottom: ImageView
    private lateinit var scoreText: TextView

    private var screenWidth  = 0
    private var screenHeight = 0
    private var density      = 1f

    private var avionY    = 0f
    private var velocity  = 0f
    private val gravity   = 1.2f
    private val jumpForce = -20f

    private var regleX = 0f
    private var gap    = 0f   // calculé en px

    private var score     = 0
    private var hasScored = false

    private val handler        = Handler(Looper.getMainLooper())
    private val updateInterval = 20L
    private var isGameOver     = false

    private var pipeSpeed      = 6f
    private val speedBoost     = 0.5f

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateGame()
            if (!isGameOver) handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flappy_papier)

        rootLayout  = findViewById(R.id.rootConstraintFlappy)
        avion       = findViewById(R.id.avionenpapier)
        regleTop    = findViewById(R.id.regle1)
        regleBottom = findViewById(R.id.regle2)
        scoreText   = findViewById(R.id.scoreFlappy)
        density     = resources.displayMetrics.density

        gap = 100f * density

        rootLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                screenWidth  = rootLayout.width
                screenHeight = rootLayout.height

                gap = avion.height * 2f

                avionY = screenHeight / 2f
                avion.y = avionY
                regleX  = screenWidth.toFloat()

                placeRegles()
                handler.post(updateRunnable)
            }
        })

        rootLayout.setOnClickListener { velocity = jumpForce }
    }

    private fun updateGame() {
        velocity += gravity
        avionY   += velocity

        // Si on touche le plafond ou le sol → game over
        if (avionY < 0f || avionY + avion.height > screenHeight) {
            return gameOver()
        }
        avion.y = avionY

        // Déplacement horizontale des règles
        regleX -= pipeSpeed
        if (regleX + regleTop.width < 0) {
            // Réapparition à droite
            regleX = screenWidth.toFloat()
            placeRegles()
        }
        regleTop.x    = regleX
        regleBottom.x = regleX

        // Score
        if (!hasScored && regleX + regleTop.width < avion.x) {
            score++
            scoreText.text = "Score : $score"
            hasScored = true

            pipeSpeed += speedBoost
        }

        // Collision
        if (checkCollision(avion, regleTop) || checkCollision(avion, regleBottom)) {
            gameOver()
        }
    }

    private fun placeRegles() {
        // Centre du gap aléatoire entre 25% et 75% de l'écran
        val minCenter = screenHeight * 0.25f
        val maxCenter = screenHeight * 0.75f
        val center    = Random.nextFloat() * (maxCenter - minCenter) + minCenter

        val topH = (center - gap / 2f).toInt()
        (regleTop.layoutParams as ConstraintLayout.LayoutParams).also {
            it.height = topH
            regleTop.layoutParams = it
        }
        regleTop.rotation = 180f
        regleTop.x = regleX
        regleTop.y = 0f

        val bottomY = center + gap / 2f
        val bottomH = (screenHeight - bottomY).toInt()
        (regleBottom.layoutParams as ConstraintLayout.LayoutParams).also {
            it.height = bottomH
            regleBottom.layoutParams = it
        }
        regleBottom.rotation = 0f
        regleBottom.x = regleX
        regleBottom.y = bottomY

        hasScored = false
    }

    private fun checkCollision(v: View, o: View): Boolean {
        // Hit‑box avion à 60 % de sa taille
        val offXv = v.width  * 0.2f
        val offYv = v.height * 0.2f
        val rAvion = RectF(
            v.x + offXv,
            v.y + offYv,
            v.x + v.width  - offXv,
            v.y + v.height - offYv
        )

        val offXo = o.width  * 0.1f
        val offYo = o.height * 0.1f
        val rObs = RectF(
            o.x + offXo,
            o.y + offYo,
            o.x + o.width  - offXo,
            o.y + o.height - offYo
        )

        // Intersection AABB
        return rAvion.left <  rObs.right &&
                rAvion.right > rObs.left  &&
                rAvion.top   <  rObs.bottom&&
                rAvion.bottom>  rObs.top
    }

    private fun gameOver() {
        pipeSpeed = 6f

        isGameOver = true
        Toast.makeText(this, "Game Over! Score final : $score", Toast.LENGTH_LONG).show()
        handler.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500)
    }
}
