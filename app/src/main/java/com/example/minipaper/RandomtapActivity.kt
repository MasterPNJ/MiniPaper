package com.example.minipaper

import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.Random

class RandomtapActivity : AppCompatActivity() {

    private lateinit var rootLayout: ConstraintLayout

    private lateinit var postit1: ImageView
    private lateinit var postit2: ImageView
    private lateinit var postit3: ImageView

    private lateinit var scoreText: TextView
    private var score = 0

    private var screenWidth = 0
    private var screenHeight = 0

    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_randomtap)

        // 1) Récupérer la référence du parent ConstraintLayout
        rootLayout = findViewById(R.id.rootConstraintLayout)

        // 2) Récupérer les ImageView (post-its)
        postit1 = findViewById(R.id.imageView15)
        postit2 = findViewById(R.id.imageView16)
        postit3 = findViewById(R.id.imageView17)

        // 3) Récupérer le TextView du score
        scoreText = findViewById(R.id.textView11) // "Score : 0"

        // 4) Mesurer la taille de la zone d'affichage dès que le layout est prêt
        rootLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Retirer le listener pour éviter qu'il ne se répète
                rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Récupérer la largeur et la hauteur du parent
                screenWidth = rootLayout.width
                screenHeight = rootLayout.height

                // Placer aléatoirement chaque post-it une première fois
                movePostitRandomly(postit1)
                movePostitRandomly(postit2)
                movePostitRandomly(postit3)
            }
        })

        // 5) Gérer le clic sur chaque post-it
        val onPostitClickListener = { postit: ImageView ->
            // Incrémenter le score
            score++
            scoreText.text = "Score : $score"

            // Replacer le post-it cliqué
            movePostitRandomly(postit)
        }

        // 6) Associer le listener à chaque post-it
        postit1.setOnClickListener { onPostitClickListener(postit1) }
        postit2.setOnClickListener { onPostitClickListener(postit2) }
        postit3.setOnClickListener { onPostitClickListener(postit3) }
    }

    /**
     * Déplace un post-it (ImageView) à une position aléatoire dans rootLayout.
     */
    private fun movePostitRandomly(postit: ImageView) {
        // Empêcher un crash si le layout n'est pas encore mesuré
        if (screenWidth == 0 || screenHeight == 0) return

        // Calculer la zone disponible
        val maxX = screenWidth - postit.width
        val maxY = screenHeight - postit.height

        // Générer des coordonnées aléatoires
        val randomX = random.nextInt(maxX.coerceAtLeast(1))
        val randomY = random.nextInt(maxY.coerceAtLeast(1))

        // Positionner le post-it (en coordonnées absolues dans le parent)
        postit.x = randomX.toFloat()
        postit.y = randomY.toFloat()
    }
}