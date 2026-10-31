package com.zubair.social.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zubair.social.ui.theme.ZubairSocialTheme

/** Follow (filled) / Following (outline). Doc ke mutabiq: already follow ho to outline, warna filled. */
@Composable
fun FollowButton(
    isFollowing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isFollowing) SecondaryButton(text = "Following", onClick = onClick, modifier = modifier)
    else PrimaryButton(text = "Follow", onClick = onClick, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun FollowButtonFollowPreview() {
    ZubairSocialTheme { FollowButton(isFollowing = false, onClick = {}) }
}

@Preview(showBackground = true)
@Composable
private fun FollowButtonFollowingPreview() {
    ZubairSocialTheme { FollowButton(isFollowing = true, onClick = {}) }
}
