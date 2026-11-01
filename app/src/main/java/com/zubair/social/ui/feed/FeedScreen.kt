package com.zubair.social.ui.feed

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zubair.social.feed.FeedPost
import com.zubair.social.ui.components.PostCard
import com.zubair.social.ui.components.SecondaryButton
import com.zubair.social.ui.theme.Spacing
import com.zubair.social.ui.theme.ZubairSocialTheme

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Home tab har baar khulne par (naya post bana ho sakta hai) chupke se reload; pehli baar init ka load chal raha hota hai.
    LaunchedEffect(Unit) {
        if (viewModel.state.value !is FeedUiState.Loading) viewModel.refresh()
    }

    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    FeedContent(
        state,
        onRetry = { viewModel.load() },
        modifier = modifier,
        onLoadMore = viewModel::loadMore,
        onLikeClick = viewModel::onLikeClick,
    )
}

@Composable
fun FeedContent(
    state: FeedUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onLoadMore: () -> Unit = {},
    onLikeClick: (postId: String) -> Unit = {},
) {
    when (state) {
        FeedUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

        FeedUiState.Empty -> Box(modifier.fillMaxSize().padding(Spacing.lg), contentAlignment = Alignment.Center) {
            Text(
                "No posts yet — share the first photo",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        is FeedUiState.Error -> Box(modifier.fillMaxSize().padding(Spacing.lg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(state.message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                SecondaryButton(text = "Try again", onClick = onRetry)
            }
        }

        is FeedUiState.Success -> {
            val listState = rememberLazyListState()
            // Aakhri 3 items ke andar pohanchte hi agla page maango. Naye items aane par dobara check hota hai.
            val nearEnd by remember(listState) {
                derivedStateOf {
                    val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    last >= listState.layoutInfo.totalItemsCount - 3
                }
            }
            LaunchedEffect(nearEnd, state.posts.size, state.endReached) {
                if (nearEnd) onLoadMore()
            }

            LazyColumn(modifier.fillMaxSize(), state = listState) {
                items(state.posts, key = { it.postId }) { post ->
                    PostCard(
                        username = post.username,
                        avatarUrl = post.avatarUrl,
                        imageUrl = post.imageUrl,
                        likeCount = post.likeCount,
                        isLiked = post.isLiked,
                        caption = post.caption,
                        commentCount = 0,           // Comments feature baad mein
                        onLikeClick = { onLikeClick(post.postId) },
                        onCommentClick = {},
                    )
                    HorizontalDivider(Modifier.padding(top = Spacing.md), color = MaterialTheme.colorScheme.surfaceVariant)
                }
                // Footer: loading ya retry; endReached par kuch nahi (loading band).
                if (state.isLoadingMore) item(key = "footer-loading") {
                    Box(Modifier.fillMaxWidth().padding(Spacing.lg), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                if (state.loadMoreFailed) item(key = "footer-error") {
                    Column(
                        Modifier.fillMaxWidth().padding(Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        Text("Couldn't load more posts.", color = MaterialTheme.colorScheme.error)
                        SecondaryButton(text = "Try again", onClick = onLoadMore)
                    }
                }
            }
        }
    }
}

private val previewPosts = listOf(
    FeedPost("p2", "u1", "zubair", null, "", "Sunset at the beach", 12, 2),
    FeedPost("p1", "u2", "ali_hassan", null, "", "", 1, 1, isLiked = true),
)

@Preview(showBackground = true)
@Composable
private fun FeedSuccessPreview() { ZubairSocialTheme { FeedContent(FeedUiState.Success(previewPosts), {}) } }

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
private fun FeedSuccessDarkPreview() { ZubairSocialTheme { FeedContent(FeedUiState.Success(previewPosts), {}) } }

@Preview(showBackground = true)
@Composable
private fun FeedLoadingMorePreview() {
    ZubairSocialTheme { FeedContent(FeedUiState.Success(previewPosts, isLoadingMore = true), {}) }
}

@Preview(showBackground = true)
@Composable
private fun FeedLoadingPreview() { ZubairSocialTheme { FeedContent(FeedUiState.Loading, {}) } }

@Preview(showBackground = true)
@Composable
private fun FeedEmptyPreview() { ZubairSocialTheme { FeedContent(FeedUiState.Empty, {}) } }

@Preview(showBackground = true)
@Composable
private fun FeedErrorPreview() {
    ZubairSocialTheme { FeedContent(FeedUiState.Error("Couldn't load your feed. Check your connection and try again."), {}) }
}
