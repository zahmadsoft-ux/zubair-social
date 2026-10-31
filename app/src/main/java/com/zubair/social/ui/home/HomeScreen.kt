package com.zubair.social.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zubair.social.ui.components.PrimaryButton
import com.zubair.social.ui.theme.Spacing
import com.zubair.social.ui.theme.ZubairSocialTheme

/** Placeholder: asli feed baad mein aayega. Abhi sirf Logout test karne ke liye. */
@Composable
fun HomeScreen(onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Home Feed (placeholder)", style = MaterialTheme.typography.titleMedium)
        PrimaryButton(text = "Log out", onClick = onLogout)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ZubairSocialTheme { HomeScreen(onLogout = {}) }
}
