package com.example.babyfirstwordstranslator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Tracks which screen is currently shown
enum class Screen {
    SPLASH, HOME, CRY_DETECTION, SPEECH, PICTURE_BOOK
}

@Composable
fun TinyTalksApp() {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }

    // Show splash for 2 seconds, then move to Home
    LaunchedEffect(Unit) {
        delay(2000)
        currentScreen = Screen.HOME
    }

    when (currentScreen) {
        Screen.SPLASH -> SplashScreen()
        Screen.HOME -> HomeScreen(
            onSelectCryDetection = { currentScreen = Screen.CRY_DETECTION },
            onSelectSpeech = { currentScreen = Screen.SPEECH },
            onSelectPictureBook = { currentScreen = Screen.PICTURE_BOOK }
        )
        Screen.CRY_DETECTION -> ScreenWithBack(onBack = { currentScreen = Screen.HOME }) {
            // Replace with your teammate's actual Cry Detection composable
            Text("Cry Detection Screen (placeholder)", fontSize = 20.sp)
        }
        Screen.SPEECH -> ScreenWithBack(onBack = { currentScreen = Screen.HOME }) {
            BabyApp() // your existing speech recognition screen
        }
        Screen.PICTURE_BOOK -> ScreenWithBack(onBack = { currentScreen = Screen.HOME }) {
            PictureBookScreen() // your existing picture book screen
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "TinyTalks",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun HomeScreen(
    onSelectCryDetection: () -> Unit,
    onSelectSpeech: () -> Unit,
    onSelectPictureBook: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("TinyTalks", fontSize = 32.sp, fontWeight = FontWeight.Bold ,fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(16.dp))
        Text(" Infant Care Assistant for Deaf Parents", fontSize = 16.sp)
        Spacer(Modifier.height(120.dp))

        FeatureButton("👶 Cry Detection", onSelectCryDetection)
        Spacer(Modifier.height(32.dp))
        FeatureButton("🗣 Baby First Words", onSelectSpeech)
        Spacer(Modifier.height(32.dp))
        FeatureButton("📖 Picture Book", onSelectPictureBook)
    }
}

@Composable
fun FeatureButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(label, fontSize = 18.sp)
    }
}

@Composable
fun ScreenWithBack(onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TextButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
            Text("← Back to Home")
        }
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}


