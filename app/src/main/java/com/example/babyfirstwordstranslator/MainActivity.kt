package com.example.babyfirstwordstranslator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        setContent { TinyTalksApp()  }
    }
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
}

@Composable
fun BabyApp() {
    val context = LocalContext.current

    var isListening by remember { mutableStateOf(false) }
    var detectedWord by remember { mutableStateOf("Tap to start") }
    var confidence by remember { mutableStateOf(0) }
    var history by remember { mutableStateOf(listOf<String>()) }

    // Ask for mic permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) detectedWord = "Microphone permission needed"
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    fun startListening() {
        isListening = true
        detectedWord = "Listening to baby... 👶"
        confidence = 0

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                val text = matches?.getOrNull(0) ?: "Didn't catch that"
                val score = scores?.getOrNull(0) ?: 0f

                detectedWord = "Detected Word: ${text.replaceFirstChar { it.uppercase() }}"
                confidence = (score * 100).toInt()
                isListening = false

                if (matches != null && matches.isNotEmpty()) {
                    history = history + text.replaceFirstChar { it.uppercase() }
                }
            }

            override fun onError(error: Int) {
                detectedWord = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match, try again"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error - check mic"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mic permission missing"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    else -> "Error code: $error"
                }
                confidence = 0
                isListening = false
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        isListening = false
        detectedWord = "Tap to start"
        confidence = 0
    }

    DisposableEffect(Unit) {
        onDispose { speechRecognizer.destroy() }
    }

    Column(
        Modifier.fillMaxSize().padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Baby First Words Translator", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Text(detectedWord, fontSize = 18.sp)
        if (confidence > 0) Text("Confidence: $confidence%")
        Spacer(Modifier.height(30.dp))

        Button(onClick = {
            if (isListening) stopListening() else startListening()
        }) {
            Text(if (isListening) "Stop Listening" else "Start Listening")
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            detectedWord = if (history.isEmpty()) "History: (empty)" else "History: ${history.joinToString(", ")}"
        }) {
            Text("View History")
        }
    }
}