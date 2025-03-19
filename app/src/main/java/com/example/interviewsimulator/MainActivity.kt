package com.example.interviewsimulator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private lateinit var questionTextView: TextView
    private lateinit var responseEditText: EditText
    private lateinit var progressBar: ProgressBar
    private var currentQuestionIndex = 0
    private val questions = listOf(
        "¿Por qué quieres trabajar aquí?",
        "¿Cuáles son tus fortalezas?",
        "¿Cuáles son tus debilidades?",
        "¿Dónde te ves en 5 años?",
        "¿Por qué deberíamos contratarte?",
        "¿Cómo manejas el estrés?",
        "¿Tienes alguna pregunta para nosotros?",
        "¿Cuál es tu mayor logro?",
        "¿Cómo te describirías en tres palabras?",
        "¿Qué te motiva a trabajar?"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        questionTextView = findViewById(R.id.questionTextView)
        responseEditText = findViewById(R.id.responseEditText)
        progressBar = findViewById(R.id.progressBar)

        val textResponseButton: Button = findViewById(R.id.textResponseButton)
        val videoResponseButton: Button = findViewById(R.id.videoResponseButton)
        val continueButton: Button = findViewById(R.id.continueButton)

        textResponseButton.setOnClickListener {
            val response = responseEditText.text.toString()
            nextQuestion()
        }

        videoResponseButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            } else {
                openCamera()
            }
        }

        continueButton.setOnClickListener {
            nextQuestion()
        }

        loadQuestion()
    }

    private fun loadQuestion() {
        if (currentQuestionIndex < questions.size) {
            questionTextView.text = questions[currentQuestionIndex]
            progressBar.progress = (currentQuestionIndex + 1) * 10
        }
    }

    private fun nextQuestion() {
        currentQuestionIndex++
        if (currentQuestionIndex >= questions.size) {
            val intent = Intent(this, CompletionActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            loadQuestion()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, 101)
    }
}