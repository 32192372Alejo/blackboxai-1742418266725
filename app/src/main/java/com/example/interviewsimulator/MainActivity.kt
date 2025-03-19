package com.example.interviewsimulator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.FrameLayout
import android.media.MediaRecorder
import java.io.File
import java.io.IOException

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var questionTextView: TextView
    private lateinit var responseEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    
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

        // Initialize SurfaceView
        surfaceView = SurfaceView(this)
        val cameraPreview = findViewById<FrameLayout>(R.id.camera_preview)
        cameraPreview.addView(surfaceView)
        
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)

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

        if (checkCameraPermission()) {
            setupCamera()
        } else {
            requestCameraPermission()
        }

        loadQuestion()
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            CAMERA_PERMISSION_REQUEST
        )
    }

    private fun setupCamera() {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
            camera?.setDisplayOrientation(90)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startRecording() {
        try {
            mediaRecorder = MediaRecorder().apply {
                setCamera(camera)
                setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
                setVideoSource(MediaRecorder.VideoSource.CAMERA)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
                setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT)
                setOutputFile(getOutputFile().toString())
                setPreviewDisplay(surfaceHolder.surface)
                prepare()
            }
            mediaRecorder?.start()
            isRecording = true
            findViewById<Button>(R.id.videoResponseButton).text = "DETENER"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            reset()
            release()
        }
        mediaRecorder = null
        isRecording = false
        findViewById<Button>(R.id.videoResponseButton).text = "GRABAR"
    }

    private fun getOutputFile(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, "InterviewSimulator").apply { mkdirs() }
        }
        return File(mediaDir, "video_${System.currentTimeMillis()}.mp4")
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (surfaceHolder.surface == null) return
        
        try {
            camera?.stopPreview()
        } catch (e: Exception) { }

        try {
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera?.stopPreview()
        camera?.release()
        camera = null
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
        mediaRecorder?.release()
        camera?.release()
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
    }
}