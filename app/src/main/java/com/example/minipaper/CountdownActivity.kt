package com.example.minipaper

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CountdownActivity : AppCompatActivity() {

    private lateinit var postit3: ImageView
    private lateinit var text3: TextView
    private lateinit var postit2: ImageView
    private lateinit var text2: TextView
    private lateinit var postit1: ImageView
    private lateinit var text1: TextView
    private lateinit var cancelText: TextView

    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)
        // Assurez-vous que c'est bien le nom de votre fichier XML

        // Récupération des vues
        postit3 = findViewById(R.id.imageView6)
        text3   = findViewById(R.id.textView8)
        postit2 = findViewById(R.id.imageView11)
        text2   = findViewById(R.id.textView9)
        postit1 = findViewById(R.id.imageView7)
        text1   = findViewById(R.id.textView10)
        cancelText = findViewById(R.id.textView6) // "Cancel"

        // Initialiser l'affichage
        show3()

        // Créer et démarrer le CountDownTimer
        countDownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt() + 1
                when (secondsRemaining) {
                    3 -> show3()
                    2 -> show2()
                    1 -> show1()
                }
            }

            override fun onFinish() {
                setResult(RESULT_OK)
                finish()
            }
        }
        countDownTimer.start()

        // Gérer le "Cancel"
        cancelText.setOnClickListener {
            // Annuler le compte à rebours et fermer l'activité
            countDownTimer.cancel()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    /**
     * Montre le post-it + texte "3" et cache les autres
     */
    private fun show3() {
        postit3.visibility = View.VISIBLE
        text3.visibility   = View.VISIBLE

        postit2.visibility = View.INVISIBLE
        text2.visibility   = View.INVISIBLE
        postit1.visibility = View.INVISIBLE
        text1.visibility   = View.INVISIBLE
    }

    /**
     * Montre le post-it + texte "2" et cache les autres
     */
    private fun show2() {
        postit2.visibility = View.VISIBLE
        text2.visibility   = View.VISIBLE

        postit3.visibility = View.INVISIBLE
        text3.visibility   = View.INVISIBLE
        postit1.visibility = View.INVISIBLE
        text1.visibility   = View.INVISIBLE
    }

    /**
     * Montre le post-it + texte "1" et cache les autres
     */
    private fun show1() {
        postit1.visibility = View.VISIBLE
        text1.visibility   = View.VISIBLE

        postit2.visibility = View.INVISIBLE
        text2.visibility   = View.INVISIBLE
        postit3.visibility = View.INVISIBLE
        text3.visibility   = View.INVISIBLE
    }
}