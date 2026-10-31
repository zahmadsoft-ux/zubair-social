package com.zubair.social.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zubair.social.profile.UserProfile
import com.zubair.social.ui.components.AppTextField
import com.zubair.social.ui.components.Avatar
import com.zubair.social.ui.components.AvatarSize
import com.zubair.social.ui.components.PrimaryButton
import com.zubair.social.ui.components.SecondaryButton
import com.zubair.social.ui.theme.Spacing
import com.zubair.social.ui.theme.ZubairSocialTheme

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    // Android Photo Picker: permission ki zaroorat nahi, system khud gallery dikhata hai.
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.onAvatarPicked(uri?.toString())
    }

    EditProfileContent(
        state = state,
        onBack = onBack,
        onFullNameChange = viewModel::onFullNameChange,
        onBioChange = viewModel::onBioChange,
        onChangePhoto = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onSave = viewModel::onSaveClick,
        onRetryLoad = viewModel::load,
        modifier = modifier,
    )
}

@Composable
fun EditProfileContent(
    state: EditProfileUiState,
    onBack: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onChangePhoto: () -> Unit,
    onSave: () -> Unit,
    onRetryLoad: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Edit profile", style = MaterialTheme.typography.titleMedium)
        }

        val original = state.original
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            original == null -> Box(Modifier.fillMaxSize().padding(Spacing.lg), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Text(state.loadError.orEmpty(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                    SecondaryButton(text = "Try again", onClick = onRetryLoad)
                }
            }
            else -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Avatar(
                    name = state.fullName.ifBlank { original.username },
                    imageUrl = state.pickedAvatarUri ?: original.avatarUrl,
                    size = AvatarSize.Large,
                )
                SecondaryButton(text = "Choose photo", onClick = onChangePhoto, enabled = !state.isSaving)
                if (state.pickedAvatarUri != null) {
                    Text(
                        "Preview only — photo upload isn't available yet, so it won't be saved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                AppTextField(
                    label = "Full name",
                    value = state.fullName,
                    onValueChange = onFullNameChange,
                    errorMessage = state.fullNameError,
                )
                AppTextField(
                    label = "Bio",
                    value = state.bio,
                    onValueChange = onBioChange,
                    helperText = "${state.bio.length}/$BIO_MAX_LENGTH",
                    singleLine = false,
                    minLines = 3,
                )
                state.saveError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
                PrimaryButton(
                    text = if (state.isSaving) "Saving..." else "Save",
                    onClick = onSave,
                    enabled = state.hasChanges && !state.isSaving,
                )
            }
        }
    }
}

private val previewUser = UserProfile("zubair", "Zubair Ahmad", "Learning Android", null)

@Preview(showBackground = true)
@Composable
private fun EditProfilePreview() {
    ZubairSocialTheme {
        EditProfileContent(
            EditProfileUiState(isLoading = false, original = previewUser, fullName = "Zubair Ahmad", bio = "Learning Android and Firebase"),
            {}, {}, {}, {}, {}, {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditProfileErrorPreview() {
    ZubairSocialTheme {
        EditProfileContent(
            EditProfileUiState(
                isLoading = false, original = previewUser, fullName = "Zubair", bio = "x".repeat(BIO_MAX_LENGTH),
                saveError = "Couldn't save your changes. Check your connection and try again.",
            ),
            {}, {}, {}, {}, {}, {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditProfileLoadingPreview() {
    ZubairSocialTheme { EditProfileContent(EditProfileUiState(), {}, {}, {}, {}, {}, {}) }
}
