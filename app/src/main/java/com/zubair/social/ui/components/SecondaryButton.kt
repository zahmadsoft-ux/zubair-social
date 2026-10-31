package com.zubair.social.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zubair.social.ui.theme.Radius
import com.zubair.social.ui.theme.Size
import com.zubair.social.ui.theme.ZubairSocialTheme

/** Outline button (jaise "Edit profile"). PrimaryButton ka halka bhai: fill nahi, sirf border. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(Radius.md),
        border = BorderStroke(Size.stroke, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth().height(Size.control),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryButtonPreview() {
    ZubairSocialTheme { SecondaryButton(text = "Edit profile", onClick = {}) }
}
