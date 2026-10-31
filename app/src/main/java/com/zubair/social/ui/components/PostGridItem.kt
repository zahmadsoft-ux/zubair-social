package com.zubair.social.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.zubair.social.ui.theme.ZubairSocialTheme

/** Profile grid ka ek square thumbnail. Width parent se milti hai, height = width (1:1). */
@Composable
fun PostGridItem(
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant)   // load hone tak / fail par placeholder
            .clickable(onClick = onClick),
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Post",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 120)
@Composable
private fun PostGridItemPreview() {
    ZubairSocialTheme { PostGridItem(imageUrl = null, onClick = {}) }
}
