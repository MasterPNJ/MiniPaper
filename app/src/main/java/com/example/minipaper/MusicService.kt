package com.example.minipaper

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class MusicService : Service() {

    companion object {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.banger)
            mediaPlayer?.isLooping = true
            val volume = PreferenceUtils.getMusicVolume(this)
            mediaPlayer?.setVolume(volume, volume)
            mediaPlayer?.start()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
