package com.zubair.social.ui.auth

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zubair.social.ui.components.AppTextField
import com.zubair.social.ui.components.GoogleButton
import com.zubair.social.ui.components.PrimaryButton
import com.zubair.social.ui.theme.Spacing
import com.zubair.social.ui.theme.ZubairSocialTheme

/** Stateful wrapper: ViewModel se state leta hai aur stateless LoginContent ko deta hai. */
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onSignUpClick: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoggedIn()
    }

    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onLoginClick,
        onGoogleClick = {},   // abhi sirf UI, v1.1 mein Google Sign-In
        onSignUpClick = onSignUpClick,
    )
}

/** Stateless UI: sirf state dekh kar draw karta hai, isliye @Preview mein asani se chalta hai. */
@Composable
fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onSignUpClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
    ) {
        Text("Zubair Social", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text("Welcome back", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        AppTextField(
            label = "Email",
            value = state.email,
            onValueChange = onEmailChange,
            errorMessage = state.emailError,
            keyboardType = KeyboardType.Email,
        )
        AppTextField(
            label = "Password",
            value = state.password,
            onValueChange = onPasswordChange,
            errorMessage = state.passwordError,
            isPassword = true,
        )

        if (state.formError != null) {
            Text(state.formError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        PrimaryButton(
            text = if (state.isLoading) "Logging in..." else "Log in",
            onClick = onLoginClick,
            enabled = !state.isLoading,
        )
        GoogleButton(onClick = onGoogleClick)

        Row(modifier = Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account?", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onSignUpClick) { Text("Sign up") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    ZubairSocialTheme {
        LoginContent(LoginUiState(email = "zubair@example.com"), {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginErrorPreview() {
    ZubairSocialTheme {
        LoginContent(
            LoginUiState(email = "zubair@example.com", password = "x", formError = "Incorrect email or password"),
            {}, {}, {}, {}, {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginLoadingPreview() {
    ZubairSocialTheme {
        LoginContent(LoginUiState(email = "zubair@example.com", password = "secret123", isLoading = true), {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
private fun LoginDarkPreview() {
    ZubairSocialTheme(darkTheme = true) {
        LoginContent(LoginUiState(email = "zubair@example.com"), {}, {}, {}, {}, {})
    }
}
