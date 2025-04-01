package com.example.minipaper

import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt

class ShakeItUpActivity : AppCompatActivity(), SensorEventListener {

    // Vues
    private lateinit var shakeInfoText: TextView
    private lateinit var progressBar: ProgressBar

    // Gestion du capteur
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // Paramètres de détection de shake
    private val shakeThreshold = 2.7f
    private var lastShakeTime = 0L
    private val shakeCooldown = 300 // en ms

    // Score (nombre de secousses)
    private var shakeCount = 0

    // Compte à rebours
    private var gameTimer: CountDownTimer? = null
    private val gameDuration = 10_000L // 10 secondes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Verrouiller l'orientation en portrait (optionnel)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContentView(R.layout.activity_shakeitup)

        // 1) Récupérer la TextView et la ProgressBar
        shakeInfoText = findViewById(R.id.textView23)
        progressBar = findViewById(R.id.progressBarShake)

        // 2) Initialiser l'affichage
        shakeInfoText.text = "Shake fast !!\nScore : 0"
        progressBar.max = 50
        progressBar.progress = 0

        // 3) Initialiser le SensorManager et l'accéléromètre
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // 4) Démarrer un compte à rebours
        gameTimer = object : CountDownTimer(gameDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // On pourrait afficher un timer si besoin
            }

            override fun onFinish() {
                // Temps écoulé
                Toast.makeText(this@ShakeItUpActivity, "Time's up! Final shakes: $shakeCount", Toast.LENGTH_LONG).show()

                // Enregistrer le score dans les préférences
                saveScoreToPreferences(shakeCount)

                // Aller à EndActivity
                //startActivity(Intent(this@ShakeItUpActivity, EndActivity::class.java))

                //startActivity(Intent(this@ShakeItUpActivity, VolumeMasterActivity::class.java))
                setResult(RESULT_OK)
                finish()
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        // Enregistrer le listener pour l'accéléromètre
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        // Désenregistrer le listener
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Annuler le timer s'il existe encore
        gameTimer?.cancel()
    }

    /**
     * Détection du shake : on calcule la "force" de l'accélération par rapport à la gravité.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calcul de l'accélération
            val acceleration = sqrt(
                (x * x + y * y + z * z).toDouble() /
                        (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH)
            ).toFloat()

            if (acceleration > shakeThreshold) {
                val currentTime = System.currentTimeMillis()
                // Éviter de compter 10 shakes en 1 seconde à cause d'oscillations
                if (currentTime - lastShakeTime > shakeCooldown) {
                    shakeCount++
                    shakeInfoText.text = "Shake fast !!\nScore : $shakeCount"

                    // 5) Mettre à jour la ProgressBar
                    progressBar.progress = shakeCount

                    // Si on veut stopper dès qu'on atteint le max de la barre
                    if (shakeCount >= progressBar.max) {
                        gameTimer?.cancel()
                        Toast.makeText(this, "You reached ${progressBar.max} shakes!", Toast.LENGTH_SHORT).show()

                        // Enregistrer le score
                        saveScoreToPreferences(shakeCount)
                        startActivity(Intent(this, EndActivity::class.java))
                        finish()
                    }

                    lastShakeTime = currentTime
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Pas utilisé ici
    }

    /**
     * Ajoute le score local (shakeCount) au score cumulé dans SharedPreferences.
     */
    private fun saveScoreToPreferences(gameScore: Int) {
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val oldScore = sharedPref.getInt("cumulativeScore", 0)
        val newScore = oldScore + gameScore
        sharedPref.edit()
            .putInt("cumulativeScore", newScore)
            .apply()
    }
}
