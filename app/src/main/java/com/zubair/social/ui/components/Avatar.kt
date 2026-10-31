package com.zubair.social.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil3.compose.SubcomposeAsyncImage
import com.zubair.social.ui.theme.Size
import com.zubair.social.ui.theme.ZubairSocialTheme

enum class AvatarSize(val dp: Dp) { Small(Size.avatarSmall), Large(Size.avatarLarge) }

/** Gol profile photo. imageUrl null/khali ho ya load fail ho to name ke initials dikhte hain. */
@Composable
fun Avatar(
    name: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.Small,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl.isNullOrBlank()) {
            AvatarInitials(name, size)
        } else {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size.dp),
                loading = { AvatarInitials(name, size) },
                error = { AvatarInitials(name, size) },
            )
        }
    }
}

@Composable
private fun AvatarInitials(name: String, size: AvatarSize) {
    val text = name.trim().split(" ").filter { it.isNotEmpty() }
        .take(2).joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }
    Text(
        text = text,
        style = if (size == AvatarSize.Large) MaterialTheme.typography.headlineMedium
        else MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

@Preview(showBackground = true)
@Composable
private fun AvatarPreview() {
    ZubairSocialTheme {
        androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(com.zubair.social.ui.theme.Spacing.md)) {
            Avatar(name = "Zubair Ahmad", imageUrl = null)
            Avatar(name = "Zubair Ahmad", imageUrl = null, size = AvatarSize.Large)
            Avatar(name = "", imageUrl = null)
        }
    }
}
