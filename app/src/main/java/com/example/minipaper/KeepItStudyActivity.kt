package com.example.minipaper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

class KeepItStudyActivity : AppCompatActivity(), SensorEventListener {

    // Capteurs
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    // Vues
    private lateinit var scoreText: TextView
    private lateinit var angleText: TextView
    private lateinit var messageText: TextView

    // Firebase
    private lateinit var database: DatabaseReference

    // Référence d’orientation
    private var referencePitch = 0f
    private var referenceRoll  = 0f
    private var referenceSet   = false

    // Score total
    private var score = 0

    // Angle cible courant
    private var currentTargetAngle = 0f

    // Timer global
    private var globalTimer: CountDownTimer? = null
    private val totalDuration = 10_000L
    private val tickInterval  = 500L

    // Vibreur
    @Suppress("DEPRECATION")
    private val vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val mgr = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            mgr.defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keepitsteady)

        // 1) Lier les vues
        scoreText   = findViewById(R.id.textView30)
        angleText   = findViewById(R.id.textView32)
        messageText = findViewById(R.id.textView33)

        // 2) Initialiser Firebase
        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase
            .getInstance("https://mini-paper-db-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("leaderboard")

        // 3) Initialisation capteurs
        sensorManager   = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor  = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        // 4) Affichages de départ
        scoreText.text   = "Score : 0"
        angleText.text   = "0°"
        messageText.text = "Prêt…"

        // 5) Choisir le premier objectif
        pickNewTarget()

        // 6) Démarrer le timer global
        globalTimer = object : CountDownTimer(totalDuration, tickInterval) {
            override fun onTick(millisUntilFinished: Long) {
                // Mise à jour du compte-à-rebours + objectif
                val sec = (millisUntilFinished / 1000).toInt() + 1
                messageText.text = "${sec}s restantes\nObjectif : ${currentTargetAngle.toInt()}°"
                // Petite vibration à chaque tick
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }
            override fun onFinish() {
                // Fin du mini-jeu
                endGame()
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        globalTimer?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        globalTimer?.cancel()
        sensorManager.unregisterListener(this)
    }

    private fun pickNewTarget() {
        // Réinitialiser la référence d’orientation
        referenceSet = false
        // Choisir un nouvel angle cible (20° à 45°)
        currentTargetAngle = Random.nextInt(20, 46).toFloat()
        // Mettre à jour immédiatement l'affichage (sera affiné au prochain tick)
        messageText.text = "Objectif : ${currentTargetAngle.toInt()}°"
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        // Récupérer orientation
        val rotMat = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotMat, event.values)
        val orient = FloatArray(3)
        SensorManager.getOrientation(rotMat, orient)

        val pitch = Math.toDegrees(orient[1].toDouble()).toFloat()
        val roll  = Math.toDegrees(orient[2].toDouble()).toFloat()

        // Fixer la référence si nécessaire
        if (!referenceSet) {
            referencePitch = pitch
            referenceRoll  = roll
            referenceSet   = true
        }

        // Calculer la déviation
        val dPitch = abs(pitch - referencePitch)
        val dRoll  = abs(roll  - referenceRoll)
        val delta  = sqrt(dPitch*dPitch + dRoll*dRoll)

        angleText.text = "${delta.toInt()}°"

        // Si on atteint l’objectif en cours
        if (delta >= currentTargetAngle) {
            // +10 pts et nouvel objectif immédiat
            score += 10
            scoreText.text = "Score : $score"
            Toast.makeText(this, "+10 pts !", Toast.LENGTH_SHORT).show()
            pickNewTarget()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // non utilisé
    }

    private fun endGame() {
        // 1) Stopper capteur & timer
        globalTimer?.cancel()
        sensorManager.unregisterListener(this)

        // 2) Message final
        Toast.makeText(this, "Temps écoulé ! Score final : $score", Toast.LENGTH_LONG).show()

        // 3) Sauvegarde locale
        saveScoreToPreferences(score)

        // 4) Mise à jour Firebase
        PlayerStatsHelper.updatePlayerStats(
            context   = this,
            database  = database,
            game      = "keepItSteady",
            newScore  = score
        )

        // 5) Retour au contrôleur
        setResult(RESULT_OK)
        finish()
    }

    /** Sauvegarde locale dans SharedPreferences */
    private fun saveScoreToPreferences(gameScore: Int) {
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val oldScore   = sharedPref.getInt("cumulativeScore", 0)
        val newScore   = oldScore + gameScore
        sharedPref.edit()
            .putInt("cumulativeScore", newScore)
            .apply()
    }
}
