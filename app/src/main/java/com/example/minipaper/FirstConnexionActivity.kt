package com.example.minipaper

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FirstConnexionActivity : AppCompatActivity() {

    private val prefsname = "MyPrefs"
    private val usernamekey = "username"

    private lateinit var soundHelper: SoundHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_connexion)

        soundHelper = SoundHelper(this)

        // Récupérer l'EditText et le "bouton" Confirm
        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val confirmTextView = findViewById<TextView>(R.id.textView16)

        // Charger le dernier pseudo enregistré (s'il existe) pour préremplir
        val sharedPref = getSharedPreferences(prefsname, MODE_PRIVATE)
        val savedUsername = sharedPref.getString(usernamekey, null)
        if (!savedUsername.isNullOrEmpty()) {
            // On affiche le dernier pseudo dans l'EditText
            editTextUsername.setText(savedUsername)
        }

        // Gérer le clic sur "Confirm"
        confirmTextView.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a valid username", Toast.LENGTH_SHORT).show()
            } else {
                // Enregistrer le pseudo dans les préférences
                sharedPref.edit()
                    .putString(usernamekey, username)
                    .apply()

                val intent = Intent(this, MainActivity::class.java)
                val volume = PreferenceUtils.getBruitageVolume(this)

                soundHelper.playSoundAndLaunchActivity(
                    context = this,
                    volume = volume,
                    intent = intent,
                    finishActivity = { finish() }
                )
            }
        }
    }
}
