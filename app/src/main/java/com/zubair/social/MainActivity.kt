package com.zubair.social

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import com.zubair.social.ui.auth.LoginScreen
import com.zubair.social.ui.auth.SignUpScreen
import com.zubair.social.ui.theme.ZubairSocialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZubairSocialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Temporary: asli Navigation Compose Prompt 5 mein aayega.
                    var loggedIn by rememberSaveable { mutableStateOf(false) }
                    var showSignUp by rememberSaveable { mutableStateOf(false) }
                    BackHandler(enabled = showSignUp) { showSignUp = false }
                    when {
                        loggedIn -> Text("Home Feed (placeholder)", modifier = Modifier.padding(innerPadding))
                        showSignUp -> SignUpScreen(
                            onSignedUp = { loggedIn = true },
                            onLoginClick = { showSignUp = false },
                        )
                        else -> LoginScreen(
                            onLoggedIn = { loggedIn = true },
                            onSignUpClick = { showSignUp = true },
                        )
                    }
                }
            }
        }
    }
}
