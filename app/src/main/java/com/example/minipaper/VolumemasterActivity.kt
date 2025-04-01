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

    // Chrono pour vérifier si le volume reste dans la zone pendant 3s
    private var timeInRange = 0f
    private val requiredTime = 1f

    private var lastUpdateTime = 0L
    private val updateInterval = 100L // mise à jour toutes les 100 ms

    private var dzScore = 0

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

        lastUpdateTime = System.currentTimeMillis()
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
     * 20 * log10(amplitude) est une estimation.
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
     * Le TextView affiche la valeur cible fixe.
     * La barre se met à jour en fonction de la proximité du volume mesuré à la cible.
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
                isGameOver = true
                Toast.makeText(this, "Bravo ! Vous avez maintenu $targetDz Dz pendant ${requiredTime.toInt()}s !", Toast.LENGTH_LONG).show()
                // Après un délai, passer à EndActivity
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
        stopMediaRecorder()
    }
}
