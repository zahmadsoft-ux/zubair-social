package com.zubair.social.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.zubair.social.ui.components.PrimaryButton
import com.zubair.social.ui.components.SecondaryButton
import com.zubair.social.ui.theme.Radius
import com.zubair.social.ui.theme.Spacing
import com.zubair.social.ui.theme.ZubairSocialTheme

/** "Uploading" screen: preview, ProgressBar (0-100%), Cancel. Fail par error + Retry. */
@Composable
fun UploadingContent(
    imageUri: String?,
    upload: UploadState,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Photo being uploaded",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth(0.5f).aspectRatio(1f).clip(RoundedCornerShape(Radius.md)),
            )
        }

        when (upload) {
            UploadState.Preparing -> {
                Text("Preparing photo...", style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())   // indeterminate: percent abhi pata nahi
            }
            is UploadState.Uploading -> {
                Text("Uploading...", style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(progress = { upload.percent / 100f }, modifier = Modifier.fillMaxWidth())
                Text("${upload.percent}%", style = MaterialTheme.typography.bodyMedium)
            }
            UploadState.Saving, is UploadState.Done -> {
                Text("Sharing your post...", style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            is UploadState.Failed -> {
                Text(
                    upload.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
                PrimaryButton(text = "Retry", onClick = onRetry)
            }
            UploadState.Idle -> Unit
        }

        if (upload !is UploadState.Done) SecondaryButton(text = "Cancel", onClick = onCancel)
    }
}

@Preview(showBackground = true)
@Composable
private fun UploadingPreparingPreview() {
    ZubairSocialTheme { UploadingContent(null, UploadState.Preparing, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun UploadingProgressPreview() {
    ZubairSocialTheme { UploadingContent(null, UploadState.Uploading(42), {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun UploadingSavingPreview() {
    ZubairSocialTheme { UploadingContent(null, UploadState.Saving, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun UploadingFailedPreview() {
    ZubairSocialTheme {
        UploadingContent(null, UploadState.Failed("No internet connection. Check it and try again."), {}, {})
    }
}
