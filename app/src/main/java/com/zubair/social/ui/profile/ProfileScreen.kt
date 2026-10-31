package com.zubair.social.ui.profile

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zubair.social.profile.PostThumb
import com.zubair.social.profile.UserProfile
import com.zubair.social.ui.components.Avatar
import com.zubair.social.ui.components.AvatarSize
import com.zubair.social.ui.components.FollowButton
import com.zubair.social.ui.components.PostGridItem
import com.zubair.social.ui.components.SecondaryButton
import com.zubair.social.ui.components.StatBlock
import com.zubair.social.ui.theme.Radius
import com.zubair.social.ui.theme.Size
import com.zubair.social.ui.theme.Spacing
import com.zubair.social.ui.theme.ZubairSocialTheme

/** Kisi bhi user ka profile: `uid` apna ho ya doosre ka, screen wahi hai. */
@Composable
fun ProfileScreen(
    uid: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onEditProfile: () -> Unit = {},
    /** Edit se save hoke aaye to true: data dobara padho (skeleton ke bina). */
    profileUpdated: Boolean = false,
    onUpdateHandled: () -> Unit = {},
    // key = uid: alag user ka profile khule to purana ViewModel reuse na ho.
    viewModel: ProfileViewModel = viewModel(key = uid, factory = viewModelFactory {
        initializer { ProfileViewModel(uid) }
    }),
) {
    val state by viewModel.state.collectAsState()

    // Tab par wapas aane par (naya post bana ho sakta hai) chupke se reload; pehli baar init ka load chal raha hota hai.
    LaunchedEffect(Unit) {
        if (viewModel.state.value !is ProfileUiState.Loading) viewModel.refresh()
    }

    LaunchedEffect(profileUpdated) {
        if (profileUpdated) {
            viewModel.refresh()
            onUpdateHandled()
        }
    }

    ProfileContent(state, onRetry = { viewModel.load() }, onLogout = onLogout, onEditProfile = onEditProfile, modifier = modifier)
}

@Composable
fun ProfileContent(
    state: ProfileUiState,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onEditProfile: () -> Unit = {},
) {
    when (state) {
        ProfileUiState.Loading -> ProfileSkeleton(modifier)
        is ProfileUiState.Error -> Box(modifier.fillMaxSize().padding(Spacing.lg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(state.message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                SecondaryButton(text = "Try again", onClick = onRetry)
            }
        }
        is ProfileUiState.Success -> LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { Header(state, onLogout, onEditProfile) }
            if (state.posts.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "No posts yet — share your first photo",
                        modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(state.posts, key = { it.id }) { post ->
                    PostGridItem(imageUrl = post.imageUrl, onClick = {})   // post detail baad mein
                }
            }
        }
    }
}

@Composable
private fun Header(state: ProfileUiState.Success, onLogout: () -> Unit, onEditProfile: () -> Unit) {
    val profile = state.profile
    Column(Modifier.fillMaxWidth().padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            Avatar(name = profile.fullName.ifBlank { profile.username }, imageUrl = profile.avatarUrl, size = AvatarSize.Large)
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatBlock(state.posts.size, "Posts")
                StatBlock(state.followers, "Followers")
                StatBlock(state.following, "Following")
            }
        }
        Column {
            Text("@${profile.username}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(profile.fullName, style = MaterialTheme.typography.bodyMedium)
            if (profile.bio.isNotBlank()) Text(profile.bio, style = MaterialTheme.typography.bodyMedium)
        }
        // Apna profile: Edit/Log out. Doosre ka: FollowButton. Ek hi if/else, isliye khud par Follow kabhi nahi aata.
        if (state.isOwnProfile) {
            SecondaryButton(text = "Edit profile", onClick = onEditProfile)
            SecondaryButton(text = "Log out", onClick = onLogout)
        } else {
            FollowButton(isFollowing = state.isFollowing, onClick = {})   // follow/unfollow write baad mein
        }
    }
}

/** Loading ke dauran asli layout jaisa dhancha (header + 3x3 grid), halka pulse karta hua. */
@Composable
private fun ProfileSkeleton(modifier: Modifier = Modifier) {
    val alpha by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    val color = MaterialTheme.colorScheme.surfaceVariant
    Column(modifier.fillMaxSize().alpha(alpha), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            Box(Modifier.size(Size.avatarLarge).clip(CircleShape).background(color))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                repeat(3) { Box(Modifier.size(width = 48.dp, height = 36.dp).clip(RoundedCornerShape(Radius.sm)).background(color)) }
            }
        }
        Column(Modifier.padding(horizontal = Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Box(Modifier.size(width = 120.dp, height = 16.dp).clip(RoundedCornerShape(Radius.sm)).background(color))
            Box(Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(Radius.sm)).background(color))
        }
        repeat(3) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                repeat(3) { Box(Modifier.weight(1f).aspectRatio(1f).background(color)) }
            }
        }
    }
}

private val previewProfile = UserProfile("zubair", "Zubair Ahmad", "Learning Android", null)

@Preview(showBackground = true)
@Composable
private fun ProfileLoadingPreview() {
    ZubairSocialTheme { ProfileContent(ProfileUiState.Loading, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun ProfileEmptyPreview() {
    ZubairSocialTheme {
        ProfileContent(ProfileUiState.Success(previewProfile, emptyList(), 0, 0, true, false), {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePostsPreview() {
    ZubairSocialTheme {
        val posts = List(6) { PostThumb("p$it", "", it.toLong()) }
        ProfileContent(ProfileUiState.Success(previewProfile, posts, 340, 180, true, false), {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileOtherUserPreview() {
    ZubairSocialTheme {
        ProfileContent(ProfileUiState.Success(previewProfile, emptyList(), 340, 180, false, false), {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileOtherUserFollowingPreview() {
    ZubairSocialTheme {
        ProfileContent(ProfileUiState.Success(previewProfile, emptyList(), 340, 180, false, true), {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileErrorPreview() {
    ZubairSocialTheme {
        ProfileContent(ProfileUiState.Error("Couldn't load the profile. Check your connection and try again."), {}, {})
    }
}
