package com.example.minipaper

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
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

        // Ajoutez un listener pour savoir quand le son est chargé
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                // Son chargé
                isLoaded = true
            } else {
                Log.e("SoundHelper", "Failed to load sound: status=$status")
            }
        }
        // Chargez le son depuis res/raw/button_click.mp3
        buttonSoundId = soundPool.load(context, R.raw.button_click, 1)

    }

    /**
     * Joue le son du bouton.
     * @param volume un Float entre 0.0 et 1.0
     */
    fun playButtonSound(volume: Float) {
        if (isLoaded) {
            soundPool.play(buttonSoundId, volume, volume, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}
