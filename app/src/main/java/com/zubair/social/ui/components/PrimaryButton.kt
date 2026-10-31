package com.zubair.social.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zubair.social.ui.theme.Radius
import com.zubair.social.ui.theme.Size
import com.zubair.social.ui.theme.ZubairSocialTheme

/** Login, Sign up, Share jaisi main actions ke liye. App mein har jagah yehi button use hoga. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(Radius.md),
        modifier = modifier.fillMaxWidth().height(Size.control),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    ZubairSocialTheme { PrimaryButton(text = "Log in", onClick = {}) }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonDisabledPreview() {
    ZubairSocialTheme { PrimaryButton(text = "Logging in...", onClick = {}, enabled = false) }
}
