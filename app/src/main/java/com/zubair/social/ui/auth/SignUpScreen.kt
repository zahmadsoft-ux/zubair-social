package com.zubair.social.ui.auth

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun SignUpScreen(
    onSignedUp: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: SignUpViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Step 11: success par Home Feed.
    LaunchedEffect(state.isSignedUp) {
        if (state.isSignedUp) onSignedUp()
    }

    SignUpContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onFullNameChange = viewModel::onFullNameChange,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignUpClick = viewModel::onSignUpClick,
        onGoogleClick = {},
        onLoginClick = onLoginClick,
    )
}

@Composable
fun SignUpContent(
    state: SignUpUiState,
    onEmailChange: (String) -> Unit,
    onFullNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    val usernameHelper = when (state.usernameStatus) {
        UsernameStatus.Checking -> "Checking availability..."
        UsernameStatus.Available -> "Username is available"
        UsernameStatus.Taken -> "This username is already taken"
        UsernameStatus.Idle -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
    ) {
        Text("Create account", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text("Join Zubair Social", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        AppTextField("Email", state.email, onEmailChange, errorMessage = state.emailError, keyboardType = KeyboardType.Email)
        AppTextField("Full name", state.fullName, onFullNameChange, errorMessage = state.fullNameError)
        AppTextField(
            "Username", state.username, onUsernameChange,
            errorMessage = state.usernameError ?: if (state.usernameStatus == UsernameStatus.Taken) usernameHelper else null,
            helperText = if (state.usernameStatus != UsernameStatus.Taken) usernameHelper else null,
        )
        AppTextField("Password", state.password, onPasswordChange, errorMessage = state.passwordError, isPassword = true)

        if (state.formError != null) {
            Text(state.formError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        PrimaryButton(
            text = if (state.isLoading) "Creating account..." else "Sign up",
            onClick = onSignUpClick,
            enabled = !state.isLoading,
        )
        GoogleButton(onClick = onGoogleClick)

        Row(modifier = Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account?", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onLoginClick) { Text("Log in") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpPreview() {
    ZubairSocialTheme {
        SignUpContent(
            SignUpUiState(email = "zubair@example.com", fullName = "Zubair", username = "zubair_d", usernameStatus = UsernameStatus.Available),
            {}, {}, {}, {}, {}, {}, {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpErrorPreview() {
    ZubairSocialTheme {
        SignUpContent(
            SignUpUiState(email = "bad", usernameError = "Username must be 3-20 characters", formError = "An account with this email already exists. Try logging in"),
            {}, {}, {}, {}, {}, {}, {},
        )
    }
}
