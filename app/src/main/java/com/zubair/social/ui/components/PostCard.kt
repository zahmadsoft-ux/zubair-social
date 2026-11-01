package com.zubair.social.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.zubair.social.ui.theme.Spacing
import com.zubair.social.ui.theme.ZubairSocialTheme

/**
 * Feed ka ek post: header (Avatar + username), square image, action row (like, comment),
 * likes count, caption, "View all N comments".
 * Stateless: saara data aur callbacks parameters mein; liked/count ka hisaab ViewModel rakhta hai.
 */
@Composable
fun PostCard(
    username: String,
    avatarUrl: String?,
    imageUrl: String?,
    likeCount: Int,
    isLiked: Boolean,
    caption: String,
    commentCount: Int,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier,
    onUserClick: () -> Unit = {},
) {
    Column(modifier.fillMaxWidth()) {
        // Header
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onUserClick).padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Avatar(name = username, imageUrl = avatarUrl, size = AvatarSize.Small)
            Text(username, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }

        // Square image (load hone tak / fail par placeholder rang)
        Box(Modifier.fillMaxWidth().aspectRatio(1f).background(MaterialTheme.colorScheme.surfaceVariant)) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Post by $username",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }

        // Actions
        Row(Modifier.padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            LikeButton(isLiked = isLiked, onClick = onLikeClick)
            IconButton(onClick = onCommentClick) {
                Icon(CommentIcon, contentDescription = "Comment", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        Column(Modifier.padding(horizontal = Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(
                text = if (likeCount == 1) "1 like" else "$likeCount likes",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (caption.isNotBlank()) {
                // "username caption": username bold, caption normal, ek hi paragraph mein.
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(username) }
                        append("  ")
                        append(caption)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (commentCount > 0) {
                Text(
                    text = if (commentCount == 1) "View 1 comment" else "View all $commentCount comments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onCommentClick),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PostCardNotLikedPreview() {
    ZubairSocialTheme {
        PostCard(
            username = "zubair", avatarUrl = null, imageUrl = null,
            likeCount = 12, isLiked = false, caption = "Sunset at the beach", commentCount = 4,
            onLikeClick = {}, onCommentClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PostCardLikedPreview() {
    ZubairSocialTheme {
        PostCard(
            username = "zubair", avatarUrl = null, imageUrl = null,
            likeCount = 13, isLiked = true, caption = "Sunset at the beach", commentCount = 4,
            onLikeClick = {}, onCommentClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PostCardNoCaptionNoCommentsPreview() {
    ZubairSocialTheme {
        PostCard(
            username = "ali_hassan", avatarUrl = null, imageUrl = null,
            likeCount = 1, isLiked = true, caption = "", commentCount = 0,
            onLikeClick = {}, onCommentClick = {},
        )
    }
}
