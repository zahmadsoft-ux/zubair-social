package com.zubair.social.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.zubair.social.ui.theme.Spacing
import com.zubair.social.ui.theme.ZubairSocialTheme

/** Profile ka "12 / Posts" jaisa block: upar bada number, neeche chhota label. */
@Composable
fun StatBlock(count: Int, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatBlockPreview() {
    ZubairSocialTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            StatBlock(12, "Posts")
            StatBlock(340, "Followers")
            StatBlock(180, "Following")
        }
    }
}
