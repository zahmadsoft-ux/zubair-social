package com.zubair.social.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.zubair.social.ui.theme.Radius
import com.zubair.social.ui.theme.Spacing
import com.zubair.social.ui.theme.ZubairSocialTheme

/**
 * Square image preview. imageUri null ho to "Select a photo" placeholder (tap = picker),
 * warna image aur kone mein "Change" chip. Dono par onPickClick aata hai: component khud picker nahi kholta.
 */
@Composable
fun ImagePreview(
    imageUri: String?,
    onPickClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(Radius.md))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onPickClick),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUri == null) {
            Text(
                "Select a photo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            AsyncImage(
                model = imageUri,
                contentDescription = "Selected photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            Surface(
                shape = RoundedCornerShape(Radius.pill),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.sm),
            ) {
                Text(
                    "Change",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImagePreviewEmptyPreview() {
    ZubairSocialTheme { ImagePreview(imageUri = null, onPickClick = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ImagePreviewSelectedPreview() {
    // Preview mein asli image load nahi hoti; background + Change chip dikhte hain.
    ZubairSocialTheme { ImagePreview(imageUri = "content://preview/photo", onPickClick = {}) }
}
