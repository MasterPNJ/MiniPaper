package com.example.minipaper

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Handler
import android.util.Log

class SoundHelper(context: Context) {

    private val soundPool: SoundPool
    private var buttonSoundId: Int = 0
    private var isLoaded = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Ajout : tester si le fichier est accessible
        try {
            buttonSoundId = soundPool.load(context, R.raw.bubble, 1)
            Log.d("SoundHelper", "Sound loading started: ID=$buttonSoundId")
        } catch (e: Exception) {
            Log.e("SoundHelper", "Exception lors du chargement du son : ${e.message}")
        }

        // Ajout d'un log si le chargement échoue
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                isLoaded = true
                Log.d("SoundHelper", "Sound loaded successfully: sampleId=$sampleId")
            } else {
                Log.e("SoundHelper", "Failed to load sound: status=$status, sampleId=$sampleId")
            }
        }
    }

    fun playButtonSound(volume: Float) {
        if (isLoaded) {
            soundPool.play(buttonSoundId, volume, volume, 1, 0, 1f)
        } else {
            Log.e("SoundHelper", "Sound not loaded yet, cannot play.")
        }
    }

    fun playSoundAndLaunchActivity(
        context: Context,
        volume: Float,
        intent: Intent,
        finishActivity: () -> Unit
    ) {
        if (isLoaded) {
            soundPool.play(buttonSoundId, volume, volume, 1, 0, 1f)
        }

        context.startActivity(intent)

        Handler(context.mainLooper).postDelayed({
            finishActivity()
        }, 300)
    }

    fun release() {
        soundPool.release()
    }
}
