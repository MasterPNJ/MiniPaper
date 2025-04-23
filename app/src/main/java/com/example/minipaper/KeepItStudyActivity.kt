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
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

class KeepItStudyActivity : AppCompatActivity(), SensorEventListener {

    // Capteurs
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    // Vues
    private lateinit var scoreText: TextView      // R.id.textView30
    private lateinit var angleText: TextView      // R.id.textView32
    private lateinit var messageText: TextView    // R.id.textView33

    // Référence d’orientation
    private var referencePitch = 0f
    private var referenceRoll  = 0f
    private var referenceSet   = false

    // Gestion des rondes
    private var round = 1
    private val totalRounds = 3
    private var score = 0

    // Angle cible de la ronde courante
    private var currentTargetAngle = 0f

    // Timer de la ronde (3s)
    private var roundTimer: CountDownTimer? = null
    private val roundDuration = 3_000L

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

        // Lier les vues
        scoreText   = findViewById(R.id.textView30)
        angleText   = findViewById(R.id.textView32)
        messageText = findViewById(R.id.textView33)

        // Initialisation capteurs
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        // Affichages de départ
        scoreText.text   = "Score : 0"
        angleText.text   = "0°"
        messageText.text = "Objectif: —°"

        // On lance la première ronde
        startNextRound()
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
        roundTimer?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        roundTimer?.cancel()
        sensorManager.unregisterListener(this)
    }

    private fun startNextRound() {
        if (round > totalRounds) {
            // Toutes les rondes réussies
            Toast.makeText(this,
                "Bravo ! Vous avez réussi $totalRounds objectifs !", Toast.LENGTH_LONG
            ).show()
            setResult(RESULT_OK)
            finish()
            return
        }

        // On réinitialise la référence d’orientation
        referenceSet = false

        // Choix du nouvel angle cible (20° à 45°)
        currentTargetAngle = Random.nextInt(20, 46).toFloat()
        // Affiche uniquement l'objectif
        messageText.text = "Objectif : ${currentTargetAngle.toInt()}°"

        // Démarre le timer de 3 s
        roundTimer?.cancel()
        roundTimer = object : CountDownTimer(roundDuration, 500) {
            override fun onTick(millisUntilFinished: Long) {
                // Affiche le temps restant ET l’objectif
                val sec = (millisUntilFinished / 1000).toInt() + 1
                messageText.text = "${sec}s restantes\nObjectif : ${currentTargetAngle.toInt()}°"
                // Vibration discrète
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    vibrator.vibrate(100)
                }
            }
            override fun onFinish() {
                // Échec
                Toast.makeText(this@KeepItStudyActivity,
                    "Temps écoulé !", Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_CANCELED)
                finish()
            }
        }.start()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        // Orientation
        val rotMat = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotMat, event.values)
        val orient = FloatArray(3)
        SensorManager.getOrientation(rotMat, orient)

        val pitch = Math.toDegrees(orient[1].toDouble()).toFloat()
        val roll  = Math.toDegrees(orient[2].toDouble()).toFloat()

        // Fixer la référence
        if (!referenceSet) {
            referencePitch = pitch
            referenceRoll  = roll
            referenceSet   = true
        }

        // Calcul delta
        val dPitch = abs(pitch - referencePitch)
        val dRoll  = abs(roll  - referenceRoll)
        val delta  = sqrt(dPitch*dPitch + dRoll*dRoll)

        angleText.text = "${delta.toInt()}°"

        // Succès si on atteint l’objectif
        if (delta >= currentTargetAngle) {
            roundTimer?.cancel()
            score += 10
            scoreText.text = "Score : $score"
            Toast.makeText(this,
                "Objectif atteint ! +10 pts", Toast.LENGTH_SHORT
            ).show()
            round++
            startNextRound()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // non utilisé
    }
}
