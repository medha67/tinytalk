package com.example.babyfirstwordstranslator


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
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

// -------------------- Word List (Feature 3 data) --------------------

data class PictureWord(val emoji: String, val word: String, val alternates: List<String> = emptyList())

val pictureWordList = listOf(
    PictureWord("🍎", "apple"),
    PictureWord("🐶", "dog", listOf("dawg", "doc")),
    PictureWord("⚽", "ball", listOf("bull", "y'all")),
    PictureWord("🌸", "flower", listOf("flour", "flowers")),
    PictureWord("🍌", "banana", listOf("bananas"))
)

// -------------------- Combined Screen (Feature 2 + 3) --------------------

@Composable
fun PictureBookScreen() {
    val context = LocalContext.current

    var currentIndex by remember { mutableStateOf(0) }
    var detectedText by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("Tap to Listen") }
    var learnedWords by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) }

    val currentPicture = pictureWordList[currentIndex]

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) statusText = "Microphone permission needed"
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    fun startListening() {
        statusText = "Listening..."
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf()
                val acceptedWords = listOf(currentPicture.word.lowercase()) +
                        currentPicture.alternates.map { it.lowercase() }

                val isCorrect = matches.any { raw ->
                    val cleaned = raw.lowercase().trim().replace(Regex("[^a-z\\s]"), "")
                    acceptedWords.any { accepted ->
                        cleaned.split(" ").contains(accepted) || cleaned.contains(accepted)
                    }
                }

                detectedText = matches.joinToString(" | ") { it.lowercase().trim() }
                statusText = if (isCorrect) "✔ Correct" else "✖ Try Again"

                learnedWords = learnedWords.filterNot { it.first == currentPicture.word } +
                        (currentPicture.word to isCorrect)
            }

            override fun onError(error: Int) {
                statusText = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match, try again"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected, try again"
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error - check mic"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mic permission missing"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error - check internet"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy, try again"
                    else -> "Error code: $error"
                }
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

    DisposableEffect(Unit) {
        onDispose { speechRecognizer.destroy() }
    }

    fun goToNextPicture() {
        detectedText = ""
        statusText = "Tap to Listen"
        currentIndex = (currentIndex + 1) % pictureWordList.size
    }

    // -------------------- UI --------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = currentPicture.emoji, fontSize = 80.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = currentPicture.word.uppercase(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = statusText, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { startListening() }) {
            Text("🎤 Listen")
        }

        if (detectedText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Detected: $detectedText")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { goToNextPicture() }) {
            Text("Next Picture ➡")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (learnedWords.isNotEmpty()) {
            Text(text = "Today's Learning", fontWeight = FontWeight.Bold)
            learnedWords.forEach { (word, correct) ->
                Text(text = "${if (correct) "✔" else "✖"} ${word.replaceFirstChar { it.uppercase() }}")
            }
        }
    }
}