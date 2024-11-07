package com.example.echowise

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var responseLog: TextView
    private lateinit var instructionTextView: TextView

    private var dotCount = 0
    private val dots = "..."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        responseLog = findViewById(R.id.responseLog)
        instructionTextView = findViewById(R.id.instruction)

        startTextViewAnimations()
    }

    private fun startTextViewAnimations() {
        val fadeAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.dots_animation)
        instructionTextView.startAnimation(fadeAnimation)

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            @SuppressLint("SetTextI18n")
            override fun run() {
                if (dotCount < 3) {
                    responseLog.text = "Waiting for command" + dots.substring(0, ++dotCount)
                } else {
                    dotCount = 0
                    responseLog.text = "Waiting for command"
                }
                handler.postDelayed(this, 500)
            }
        }
        handler.post(runnable)
    }
}