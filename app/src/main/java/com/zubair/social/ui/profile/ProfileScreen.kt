package com.zubair.social.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zubair.social.profile.UserProfile
import com.zubair.social.ui.components.Avatar
import com.zubair.social.ui.components.AvatarSize
import com.zubair.social.ui.components.SecondaryButton
import com.zubair.social.ui.components.StatBlock
import com.zubair.social.ui.theme.Spacing
import com.zubair.social.ui.theme.ZubairSocialTheme

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    ProfileContent(state, onRetry = viewModel::load, onLogout = onLogout, modifier = modifier)
}

@Composable
fun ProfileContent(
    state: ProfileUiState,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile
    when {
        state.isLoading -> CenterBox(modifier) { CircularProgressIndicator() }
        profile == null -> CenterBox(modifier) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(state.error.orEmpty(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                SecondaryButton(text = "Try again", onClick = onRetry)
            }
        }
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { Header(state, profile, onLogout) }
            // Posts abhi nahi bane: empty state (docs step 8). Baad mein yahan items(posts) aayenge.
            if (state.posts == 0) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "No posts yet — share your first photo",
                        modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(state: ProfileUiState, profile: UserProfile, onLogout: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            Avatar(name = profile.fullName.ifBlank { profile.username }, imageUrl = profile.avatarUrl, size = AvatarSize.Large)
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatBlock(state.posts, "Posts")
                StatBlock(state.followers, "Followers")
                StatBlock(state.following, "Following")
            }
        }
        Column {
            Text("@${profile.username}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(profile.fullName, style = MaterialTheme.typography.bodyMedium)
            if (profile.bio.isNotBlank()) Text(profile.bio, style = MaterialTheme.typography.bodyMedium)
        }
        // Edit profile baad mein (docs: Edit Profile section) — abhi sirf UI.
        SecondaryButton(text = "Edit profile", onClick = {})
        SecondaryButton(text = "Log out", onClick = onLogout)
    }
}

@Composable
private fun CenterBox(modifier: Modifier, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(modifier.fillMaxSize().padding(Spacing.lg), contentAlignment = Alignment.Center) { content() }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    ZubairSocialTheme {
        ProfileContent(
            ProfileUiState(isLoading = false, profile = UserProfile("zubair", "Zubair Ahmad", "Learning Android", null)),
            onRetry = {}, onLogout = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileErrorPreview() {
    ZubairSocialTheme {
        ProfileContent(ProfileUiState(isLoading = false, error = "Couldn't load your profile."), onRetry = {}, onLogout = {})
    }
}
