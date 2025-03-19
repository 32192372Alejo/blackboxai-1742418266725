package com.example.interviewsimulator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.view.View
import android.widget.FrameLayout

class MainActivity : AppCompatActivity() {
    private lateinit var questionTextView: TextView
    private lateinit var responseEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var cameraPreviewLayout: FrameLayout
    private lateinit var previewView: PreviewView
    private var currentQuestionIndex = 0
    private var isRecording = false
    
    private lateinit var cameraExecutor: ExecutorService
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

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

        // Initialize views
        questionTextView = findViewById(R.id.questionTextView)
        responseEditText = findViewById(R.id.responseEditText)
        progressBar = findViewById(R.id.progressBar)
        cameraPreviewLayout = findViewById(R.id.camera_preview)
        
        // Add PreviewView programmatically to the FrameLayout
        previewView = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        cameraPreviewLayout.addView(previewView)

        // Initialize buttons
        val textResponseButton: Button = findViewById(R.id.textResponseButton)
        val videoResponseButton: Button = findViewById(R.id.videoResponseButton)
        val continueButton: Button = findViewById(R.id.continueButton)

        textResponseButton.setOnClickListener {
            val response = responseEditText.text.toString()
            nextQuestion()
        }

        videoResponseButton.setOnClickListener {
            if (!isRecording) {
                startRecording()
            } else {
                stopRecording()
            }
        }

        continueButton.setOnClickListener {
            nextQuestion()
        }

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        loadQuestion()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    videoCapture
                )
            } catch (exc: Exception) {
                // Handle any errors
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startRecording() {
        val videoCapture = this.videoCapture ?: return
        isRecording = true

        val recording = videoCapture.output
            .prepareRecording(this, createVideoFile())
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        findViewById<Button>(R.id.videoResponseButton).text = "DETENER"
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (recordEvent.hasError()) {
                            recording?.close()
                        }
                    }
                }
            }
    }

    private fun stopRecording() {
        isRecording = false
        recording?.stop()
        findViewById<Button>(R.id.videoResponseButton).text = "GRABAR"
    }

    private fun createVideoFile(): MediaStoreOutputOptions {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        }
        return MediaStoreOutputOptions.Builder(
            contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
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

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}