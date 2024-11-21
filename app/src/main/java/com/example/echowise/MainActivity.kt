package com.example.echowise

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.echowise.logic.CommandProcessor
import com.example.echowise.logic.DeviceController
import java.util.Locale
import com.example.echowise.utils.AppConstants.REQUEST_PERMISSIONS_CODE
import java.util.Timer
import kotlin.concurrent.schedule

//  list of simple commands my app can handle:
//  1. Open Camera  +
//  2. Open Settings  +
//  3. "What's the time?", "What's the date?" +
//  4. Battery Level +
//  5. Turn on/off Wi-Fi/Bluetooth +
//  6. Turn on/off a Flashlight +
//  7. Open an alarm clock app +

class MainActivity : AppCompatActivity() {
    private lateinit var recordButton: ImageButton
    private lateinit var responseLog: TextView
    private lateinit var instructionTextView: TextView
    private lateinit var languageSpinner: Spinner

    private val deviceController = DeviceController(this)
    private val commandProcessor = CommandProcessor(this, deviceController)

    private var languageOptions = arrayOf<String>("English", "Українська")
    private var isSpinnerInitialized = false
    private var currentLocale: String = Locale.getDefault().language

    private val speechRecognizerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("MainActivity", "Speech recognition result received.")
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                Log.d("MainActivity", "Matches from speech recognizer: $matches")
                if (!matches.isNullOrEmpty()) {
                    val command = matches[0].lowercase(Locale.getDefault())
                    Log.d("MainActivity", "Processing command: $command")
                    responseLog.text = commandProcessor.processCommand(command)
                    Timer("Setting default text", false).schedule(3000) {
                        runOnUiThread {
                            setDefaultText()
                            Log.d("MainActivity", "Default text set after 3 seconds.")
                        }
                    }
                } else {
                    Log.w("MainActivity", "No speech recognized.")
                    Toast.makeText(this, "No speech recognized. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        isSpinnerInitialized = false
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "onCreate: UI initialized.")

        recordButton = findViewById(R.id.recordButton)
        responseLog = findViewById(R.id.responseLog)
        instructionTextView = findViewById(R.id.instruction)
        languageSpinner = findViewById(R.id.languageSpinner)

        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            languageOptions
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        languageSpinner.adapter = adapter

        val initialPosition = languageOptions.indexOf(
            if (currentLocale == "uk") "Українська" else "English"
        )
        languageSpinner.setSelection(initialPosition)

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = languageOptions[position]
                val newLocale = if (selectedLanguage == "Українська") "uk" else "en"

                if (newLocale != currentLocale) {
                    currentLocale = newLocale
                    Log.d("MainActivity", "Changing locale to $newLocale")
                    setLocale(newLocale)
                } else {
                    Log.d("MainActivity", "Locale unchanged: $newLocale")
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }

        startFadingAnimation()

        recordButton.setOnClickListener {
            Log.d("MainActivity", "Record button clicked.")
            startVoiceInput()
        }


    }

    private fun setLocale(localeCode: String) {
        Log.d("MainActivity", "Setting Locale to ${localeCode}")
        val locale = Locale(localeCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)

        recreate()
    }

    private fun startFadingAnimation() {
        val fadeAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_animation)
        instructionTextView.startAnimation(fadeAnimation)
        Log.d("MainActivity", "Fading animation applied to instructionTextView.")
    }

    private fun startVoiceInput() {
        Log.d("MainActivity", "startVoiceInput called.")
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w("MainActivity", "Record audio permission not granted. Requesting permission.")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSIONS_CODE)
        } else {
            Log.d("MainActivity", "Record audio permission granted. Launching speech recognizer.")
            val locale = Locale.getDefault()
            val language = locale.language

            val localeTag = if (language == "uk") {
                "${locale.language}-UA"
            } else {
                "${locale.language}-GB"
            }

            Log.d("MainActivity", "LocaleTag:  $localeTag")
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.listening_prompt))
            }

            try {
                speechRecognizerLauncher.launch(intent)
                Log.d("MainActivity", getString(R.string.listening_prompt))
            } catch (e: Exception) {
                Log.e("MainActivity", "Error launching speech recognizer: ${e.message}")
                Toast.makeText(this, getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setDefaultText() {
        responseLog.text = getString(R.string.waiting_for_command)
        Log.d("MainActivity", "ResponseLog reset to default text.")
    }
}