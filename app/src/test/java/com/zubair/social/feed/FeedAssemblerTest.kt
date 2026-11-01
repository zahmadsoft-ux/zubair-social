package com.zubair.social.feed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedAssemblerTest {
    private val users = mapOf("u1" to RawUser("zubair", "https://a/1.jpg"), "u2" to RawUser("ali", null))
    private fun post(uid: String) = RawPost(uid, "https://img/$uid.jpg", "cap", 3)

    @Test fun newestFirst_andFieldsJoined() {
        val entries = listOf(FeedEntry("a", "u1", 100), FeedEntry("c", "u2", 300), FeedEntry("b", "u1", 200))
        val posts = mapOf("a" to post("u1"), "b" to post("u1"), "c" to post("u2"))

        val feed = FeedAssembler.assemble(entries, posts, users)

        assertEquals(listOf("c", "b", "a"), feed.map { it.postId })
        assertEquals("ali", feed.first().username)
        assertEquals(null, feed.first().avatarUrl)
        assertEquals("zubair", feed.last().username)
        assertEquals("https://a/1.jpg", feed.last().avatarUrl)
        assertEquals(3, feed.first().likeCount)
    }

    @Test fun deletedPost_isSkipped() {
        val entries = listOf(FeedEntry("a", "u1", 100), FeedEntry("gone", "u1", 200))
        val posts = mapOf("a" to post("u1"), "gone" to null)   // null = /posts/gone maujood nahi

        assertEquals(listOf("a"), FeedAssembler.assemble(entries, posts, users).map { it.postId })
    }

    @Test fun postWhoseUidDiffersFromFeedEntry_isSkipped() {
        // u2 ne u1 ka post apne uid se /feedPosts mein daalne ki koshish ki.
        val entries = listOf(FeedEntry("victim", "u2", 100))
        val posts = mapOf("victim" to post("u1"))

        assertTrue(FeedAssembler.assemble(entries, posts, users).isEmpty())
    }

    @Test fun missingUser_getsFallbackName_notDropped() {
        val feed = FeedAssembler.assemble(listOf(FeedEntry("a", "u9", 1)), mapOf("a" to post("u9")), emptyMap())
        assertEquals("unknown", feed.single().username)
    }

    @Test fun emptyInput_givesEmptyList() {
        assertTrue(FeedAssembler.assemble(emptyList(), emptyMap(), emptyMap()).isEmpty())
    }

    private fun fp(id: String, at: Long) = FeedPost(id, "u1", "zubair", null, "https://img/$id.jpg", "", 0, at)

    @Test fun merge_appendsOlderPage_withoutDuplicates() {
        val loaded = listOf(fp("c", 300), fp("b", 200))
        val olderPage = listOf(fp("b", 200), fp("a", 100))   // "b" dobara aa gayi (overlap)

        assertEquals(listOf("c", "b", "a"), FeedAssembler.merge(loaded, olderPage).map { it.postId })
    }

    @Test fun merge_putsNewPostsOnTop_onRefresh() {
        val loaded = listOf(fp("b", 200), fp("a", 100))
        val freshFirstPage = listOf(fp("d", 400), fp("b", 200))

        assertEquals(listOf("d", "b", "a"), FeedAssembler.merge(loaded, freshFirstPage).map { it.postId })
    }

    @Test fun merge_emptyIncoming_keepsList() {
        val loaded = listOf(fp("b", 200))
        assertEquals(loaded, FeedAssembler.merge(loaded, emptyList()))
    }

    @Test fun assemble_marksLikedPosts() {
        val entries = listOf(FeedEntry("a", "u1", 100), FeedEntry("b", "u1", 200))
        val posts = mapOf("a" to post("u1"), "b" to post("u1"))

        val feed = FeedAssembler.assemble(entries, posts, users, likedPostIds = setOf("a"))

        assertEquals(listOf(false, true), feed.map { it.isLiked })   // b (newest) unliked, a liked
    }

    @Test fun withLiked_changesCountByOne_andIsIdempotent() {
        val p = fp("a", 1).copy(likeCount = 5)
        assertEquals(6, p.withLiked(true).likeCount)
        assertTrue(p.withLiked(true).isLiked)
        assertEquals(p, p.withLiked(false))                              // pehle se unliked: koi badlav nahi
        assertEquals(5, p.withLiked(true).withLiked(false).likeCount)    // like phir unlike = wapas 5
        assertEquals(0, fp("z", 1).copy(isLiked = true, likeCount = 0).withLiked(false).likeCount)  // 0 se neeche nahi
    }
}
