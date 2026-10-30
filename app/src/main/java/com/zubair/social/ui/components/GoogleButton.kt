package com.zubair.social.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zubair.social.ui.theme.Radius
import com.zubair.social.ui.theme.Size
import com.zubair.social.ui.theme.Spacing
import com.zubair.social.ui.theme.ZubairSocialTheme

/** "Continue with Google" — outline style. Abhi "G" text hai, asli logo baad mein drawable se. */
@Composable
fun GoogleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Continue with Google",
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(Radius.md),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth().height(Size.control),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text("G", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(Spacing.sm))
            Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GoogleButtonPreview() {
    ZubairSocialTheme { GoogleButton(onClick = {}) }
}
