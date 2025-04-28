package com.example.minipaper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    private lateinit var soundHelper: SoundHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        soundHelper = SoundHelper(this)

        startService(Intent(this, MusicService::class.java))

        val imageViewOption = findViewById<ImageView>(R.id.imageViewOption)

        resetScore(0)

        imageViewOption.setOnClickListener {
            // Au clic, on lance l'activité OptionsActivity
            val intent = Intent(this, OptionsActivity::class.java)
            val volume = PreferenceUtils.getBruitageVolume(this)

            soundHelper.playSoundAndLaunchActivity(
                context = this,
                volume = volume,
                intent = intent,
                finishActivity = { finish() }
            )
        }

        val soloButton = findViewById<ImageView>(R.id.imageView9)
        soloButton.setOnClickListener {
            val intent = Intent(this, SoloGameControllerActivity::class.java)
            val volume = PreferenceUtils.getBruitageVolume(this)

            soundHelper.playSoundAndLaunchActivity(
                context = this,
                volume = volume,
                intent = intent,
                finishActivity = { finish() }
            )
        }

        val multiButton = findViewById<ImageView>(R.id.imageView10)
        multiButton.setOnClickListener {
            val intent = Intent(this, MultiActivity::class.java)
            val volume = PreferenceUtils.getBruitageVolume(this)

            soundHelper.playSoundAndLaunchActivity(
                context = this,
                volume = volume,
                intent = intent,
                finishActivity = { finish() }
            )
        }

        val leaderBordButton = findViewById<ImageView>(R.id.imageView13)
        leaderBordButton.setOnClickListener {
            val intent = Intent(this, LeaderbordActivity::class.java)
            val volume = PreferenceUtils.getBruitageVolume(this)

            soundHelper.playSoundAndLaunchActivity(
                context = this,
                volume = volume,
                intent = intent,
                finishActivity = { finish() }
            )
        }

    }

    private fun resetScore(gameScore: Int) {
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val oldScore = sharedPref.getInt("cumulativeScore", 0)
        val newScore = 0
        sharedPref.edit()
            .putInt("cumulativeScore", newScore)
            .apply()
    }
}
