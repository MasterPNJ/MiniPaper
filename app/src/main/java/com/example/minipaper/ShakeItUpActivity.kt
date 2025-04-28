package com.example.minipaper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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

    // Firebase Database
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_shakeitup)

        // Initialiser Firebase
        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance("https://mini-paper-db-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("leaderboard")

        // Récupérer la TextView et la ProgressBar
        shakeInfoText = findViewById(R.id.textView23)
        progressBar = findViewById(R.id.progressBarShake)

        // Initialiser l'affichage
        shakeInfoText.text = "Shake fast !!\nScore : 0"
        progressBar.max = 50
        progressBar.progress = 0

        // Initialiser le SensorManager et l'accéléromètre
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Démarrer un compte à rebours
        gameTimer = object : CountDownTimer(gameDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Optionnel : afficher un timer
            }

            override fun onFinish() {
                Toast.makeText(this@ShakeItUpActivity, "Time's up! Final shakes: $shakeCount", Toast.LENGTH_LONG).show()

                // Mise à jour des stats dans Firebase
                PlayerStatsHelper.updatePlayerStats(this@ShakeItUpActivity, database, "shakeItUp", shakeCount)

                // Enregistrer le score dans les SharedPreferences
                saveScoreToPreferences(shakeCount)

                setResult(RESULT_OK)
                finish()
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        gameTimer?.cancel()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = sqrt(
                (x * x + y * y + z * z).toDouble() /
                        (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH)
            ).toFloat()

            if (acceleration > shakeThreshold) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastShakeTime > shakeCooldown) {
                    shakeCount++
                    shakeInfoText.text = "Shake fast !!\nScore : $shakeCount"
                    progressBar.progress = shakeCount
                    if (shakeCount >= progressBar.max) {
                        gameTimer?.cancel()
                        Toast.makeText(this, "You reached ${progressBar.max} shakes!", Toast.LENGTH_SHORT).show()
                        saveScoreToPreferences(shakeCount)
                        PlayerStatsHelper.updatePlayerStats(this, database, "shakeItUp", shakeCount)
                        setResult(RESULT_OK)
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

    private fun saveScoreToPreferences(gameScore: Int) {
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val oldScore = sharedPref.getInt("cumulativeScore", 0)
        val newScore = oldScore + gameScore
        sharedPref.edit().putInt("cumulativeScore", newScore).apply()
    }
}