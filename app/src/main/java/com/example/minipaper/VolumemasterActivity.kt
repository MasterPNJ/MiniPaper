package com.example.minipaper

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.log10
import com.example.minipaper.ArcProgressBar
import android.widget.TextView
import java.io.File

class VolumeMasterActivity : AppCompatActivity() {

    private lateinit var textViewDz: TextView
    private lateinit var arcProgressBar: ArcProgressBar

    // Volume cible choisi aléatoirement au démarrage (fixe)
    private val targetDz = (50..90).random()

    // Chrono pour vérifier si le volume reste dans la zone pendant requiredTime secondes
    private var timeInRange = 0f
    private val requiredTime = 1f // 1 seconde à tenir

    // Pour mesurer le temps total écoulé depuis le début du jeu
    private var startTime: Long = 0L

    private var lastUpdateTime = 0L
    private val updateInterval = 100L // mise à jour toutes les 100 ms

    private val handler = Handler(Looper.getMainLooper())
    private var isGameOver = false

    // MediaRecorder pour mesurer le volume réel
    private var mediaRecorder: MediaRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volume_master)

        textViewDz = findViewById(R.id.textView25)
        arcProgressBar = findViewById(R.id.arcProgressBar)

        // Affiche la valeur cible fixe dès le départ
        textViewDz.text = "$targetDz Dz"
        arcProgressBar.max = 100
        arcProgressBar.progress = 0

        checkAudioPermission()
        startMediaRecorder()

        // Enregistrer l'heure de début
        startTime = System.currentTimeMillis()
        lastUpdateTime = startTime
        handler.post(updateRunnable)

        Toast.makeText(this, "Objectif : $targetDz Dz !", Toast.LENGTH_LONG).show()
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (!isGameOver) {
                val currentDz = measureVolume()
                updateUI(currentDz)
                handler.postDelayed(this, updateInterval)
            }
        }
    }

    /**
     * Démarre le MediaRecorder pour mesurer le volume depuis le microphone.
     * On enregistre dans un fichier temporaire.
     */
    private fun startMediaRecorder() {
        try {
            val outputFile = File.createTempFile("temp_record", ".3gp", cacheDir)
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erreur démarrage micro: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Arrête et libère le MediaRecorder.
     */
    private fun stopMediaRecorder() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
    }

    /**
     * Mesure le volume en utilisant getMaxAmplitude() et le convertit en "dB".
     * 20 * log10(amplitude) est une estimation (à calibrer selon vos besoins).
     */
    private fun measureVolume(): Float {
        val amplitude = mediaRecorder?.maxAmplitude ?: 0
        return if (amplitude > 0) {
            (20 * log10(amplitude.toDouble())).toFloat()
        } else {
            0f
        }
    }

    /**
     * Met à jour l'interface.
     * Le TextView affiche la cible fixe.
     * La barre se met à jour en fonction de la proximité du volume mesuré à la cible.
     * Lorsqu'on maintient la cible pendant requiredTime secondes, le score est calculé.
     */
    private fun updateUI(currentDz: Float) {
        val diff = abs(currentDz - targetDz)
        val threshold = 5f
        val now = System.currentTimeMillis()
        val dt = (now - lastUpdateTime) / 1000f
        lastUpdateTime = now

        if (diff < threshold) {
            timeInRange += dt
            arcProgressBar.setArcColor(Color.GREEN)
            if (timeInRange >= requiredTime) {
                // Calcul du temps total écoulé depuis le début du jeu
                val elapsedTime = (now - startTime) / 1000f  // en secondes
                // Calcul du score selon la formule : (100 + targetDz) / elapsedTime
                val computedScore = (100 + targetDz) / elapsedTime
                isGameOver = true
                Toast.makeText(this, "Bravo ! Vous avez maintenu $targetDz Dz pendant ${requiredTime.toInt()}s ! Score: ${computedScore.toInt()}", Toast.LENGTH_LONG).show()

                // Sauvegarder le score dans les SharedPreferences ou l'envoyer à Firebase
                saveScoreToPreferences(computedScore.toInt())

                // Après un court délai, passer à EndActivity
                handler.postDelayed({
                    startActivity(Intent(this, EndActivity::class.java))
                    finish()
                }, 1000)
            }
        } else {
            timeInRange = 0f
            arcProgressBar.setArcColor(Color.RED)
        }

        val ratio = max(0f, 1f - (diff / 50f))
        val progress = (ratio * 100f).toInt()
        arcProgressBar.progress = min(100, max(0, progress))
    }

    private fun checkAudioPermission() {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1234)
        }
    }

    /**
     * Ajoute le score local (computedScore) au score cumulé dans SharedPreferences.
     */
    private fun saveScoreToPreferences(gameScore: Int) {
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val oldScore = sharedPref.getInt("cumulativeScore", 0)
        val newScore = oldScore + gameScore
        sharedPref.edit()
            .putInt("cumulativeScore", newScore)
            .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
        stopMediaRecorder()
    }
}
