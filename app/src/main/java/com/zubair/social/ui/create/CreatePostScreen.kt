package com.zubair.social.ui.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zubair.social.ui.components.AppTextField
import com.zubair.social.ui.components.ImagePreview
import com.zubair.social.ui.components.PrimaryButton
import com.zubair.social.ui.theme.Spacing
import com.zubair.social.ui.theme.ZubairSocialTheme

@Composable
fun CreatePostScreen(
    onPostCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreatePostViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Android Photo Picker: storage permission ki zaroorat nahi, system khud gallery dikhata hai.
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.onImagePicked(uri?.toString())
    }

    // Success: form reset karo aur Home Feed par jao.
    LaunchedEffect(state.upload) {
        if (state.upload is UploadState.Done) {
            viewModel.onUploadFinished()
            onPostCreated()
        }
    }

    if (state.isUploadScreen) {
        UploadingContent(
            imageUri = state.imageUri,
            upload = state.upload,
            onCancel = viewModel::onCancelUpload,
            onRetry = viewModel::onRetryClick,
            modifier = modifier,
        )
        return
    }

    CreatePostContent(
        state = state,
        onPickImage = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onCaptionChange = viewModel::onCaptionChange,
        onShare = viewModel::onShareClick,
        modifier = modifier,
    )
}

@Composable
fun CreatePostContent(
    state: CreatePostUiState,
    onPickImage: () -> Unit,
    onCaptionChange: (String) -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text("New post", style = MaterialTheme.typography.titleLarge)
        ImagePreview(imageUri = state.imageUri, onPickClick = onPickImage)
        AppTextField(
            label = "Write a caption...",
            value = state.caption,
            onValueChange = onCaptionChange,
            helperText = "${state.caption.length}/$CAPTION_MAX_LENGTH",
            singleLine = false,
            minLines = 4,
        )
        PrimaryButton(text = "Share", onClick = onShare, enabled = state.canShare)
    }
}

@Preview(showBackground = true)
@Composable
private fun CreatePostEmptyPreview() {
    ZubairSocialTheme { CreatePostContent(CreatePostUiState(), {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun CreatePostReadyPreview() {
    ZubairSocialTheme {
        CreatePostContent(CreatePostUiState("content://preview/photo", "Sunset at the beach"), {}, {}, {})
    }
}
