package com.zubair.social.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zubair.social.ui.theme.ZubairSocialTheme

/** Jin tabs ka feature abhi nahi bana (Home/Search/Create/Activity) un ke liye. */
@Composable
fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$title (coming soon)", style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderScreenPreview() {
    ZubairSocialTheme { PlaceholderScreen("Search") }
}
