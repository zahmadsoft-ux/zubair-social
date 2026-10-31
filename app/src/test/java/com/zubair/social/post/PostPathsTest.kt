package com.zubair.social.post

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PostPathsTest {
    private val ts = "SERVER_TS"
    private val updates = PostPaths.updates("p1", "u1", "https://cdn.test/posts/u1/1.jpg", "hello", ts)

    @Test fun writesExactlyThreePaths() {
        assertEquals(setOf("posts/p1", "userPosts/u1/p1", "feedPosts/p1"), updates.keys)
    }

    @Test fun postHasAllFields() {
        @Suppress("UNCHECKED_CAST")
        val post = updates["posts/p1"] as Map<String, Any>
        assertEquals("u1", post["uid"])
        assertEquals("https://cdn.test/posts/u1/1.jpg", post["imageUrl"])
        assertEquals("hello", post["caption"])
        assertEquals(ts, post["createdAt"])
        assertEquals(0, post["likeCount"])
    }

    @Test fun userPostsIsTrue_andFeedHasUidAndCreatedAt() {
        assertEquals(true, updates["userPosts/u1/p1"])
        @Suppress("UNCHECKED_CAST")
        val feed = updates["feedPosts/p1"] as Map<String, Any>
        assertEquals(mapOf("uid" to "u1", "createdAt" to ts), feed)
        assertTrue(!feed.containsKey("imageUrl"))
    }
}
