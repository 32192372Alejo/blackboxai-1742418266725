package com.example.interviewsimulator

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CompletionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completion)

        val completionMessage: TextView = findViewById(R.id.completionMessage)
        completionMessage.text = "Entrevista finalizada"
    }
}