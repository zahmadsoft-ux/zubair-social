package com.zubair.social.feed

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.zubair.social.auth.FirebaseAuthRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

/** Feed ki ek row jo PostCard banane ke liye tayyar hai. */
data class FeedPost(
    val postId: String,
    val uid: String,
    val username: String,
    val avatarUrl: String?,
    val imageUrl: String,
    val caption: String,
    val likeCount: Int,
    val createdAt: Long,
    val isLiked: Boolean = false,   // current user ne like kiya hai (/postLikes/{postId}/{uid})
)

/** Optimistic update ke liye: like/unlike ke baad ka post (count 0 se neeche nahi jata). */
fun FeedPost.withLiked(liked: Boolean): FeedPost =
    if (liked == isLiked) this
    else copy(isLiked = liked, likeCount = (likeCount + if (liked) 1 else -1).coerceAtLeast(0))

// RTDB se aaya kaccha data (assemble karne se pehle).
data class FeedEntry(val postId: String, val uid: String, val createdAt: Long)
data class RawPost(val uid: String, val imageUrl: String, val caption: String, val likeCount: Int)
data class RawUser(val username: String, val avatarUrl: String?)

/** Teen alag reads ko jod kar FeedPost list banata hai. Pure function, isliye unit test ho sakta hai. */
object FeedAssembler {
    fun assemble(
        entries: List<FeedEntry>,
        posts: Map<String, RawPost?>,
        users: Map<String, RawUser?>,
        likedPostIds: Set<String> = emptySet(),
    ): List<FeedPost> = entries
        .sortedByDescending { it.createdAt }   // newest upar
        .mapNotNull { entry ->
            val post = posts[entry.postId] ?: return@mapNotNull null   // post delete ho chuki: skip
            // /feedPosts mein koi dusre ke post ka ID apne uid se likh sakta hai (rules sirf uid check karte hain):
            // post ka asli uid feed entry se na milta ho to skip.
            if (post.uid != entry.uid) return@mapNotNull null
            val user = users[post.uid]
            FeedPost(
                postId = entry.postId,
                uid = post.uid,
                username = user?.username?.takeIf { it.isNotBlank() } ?: "unknown",
                avatarUrl = user?.avatarUrl,
                imageUrl = post.imageUrl,
                caption = post.caption,
                likeCount = post.likeCount,
                createdAt = entry.createdAt,
                isLiked = entry.postId in likedPostIds,
            )
        }

    /**
     * Pehle se loaded list mein naya page jodta hai: postId se duplicate hata kar newest-first order.
     * Refresh (naye posts upar) aur loadMore (purane posts neeche) dono ke liye.
     */
    fun merge(existing: List<FeedPost>, incoming: List<FeedPost>): List<FeedPost> =
        (incoming + existing).distinctBy { it.postId }.sortedByDescending { it.createdAt }
}

/** Pagination ka bookmark: ab tak ki sabse purani feed entry (createdAt + postId tie-break ke liye). */
data class FeedCursor(val createdAt: Long, val postId: String)

/** `endReached` true ho to is se purane posts nahi hain. `nextCursor` agle page ke liye. */
data class FeedPage(val posts: List<FeedPost>, val nextCursor: FeedCursor?, val endReached: Boolean)

interface FeedRepository {
    /** `before` null ho to sabse naye posts, warna cursor se purane. Fail par exception throw karta hai. */
    suspend fun loadPage(before: FeedCursor? = null, limit: Int = FEED_PAGE_SIZE): FeedPage

    companion object { const val FEED_PAGE_SIZE = 20 }
}

class FirebaseFeedRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(FirebaseAuthRepository.DATABASE_URL),
    private val currentUid: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
) : FeedRepository {

    override suspend fun loadPage(before: FeedCursor?, limit: Int): FeedPage {
        var cursor = before
        while (true) {
            val page = readPage(cursor, limit)
            // Poora page deleted/skip nikla aur aur purane maujood hain: loading band hone ke bajaye agla page lo.
            if (page.posts.isNotEmpty() || page.endReached || page.nextCursor == cursor) return page
            cursor = page.nextCursor
        }
    }

    private suspend fun readPage(cursor: FeedCursor?, limit: Int): FeedPage = coroutineScope {
        run {
            // 1) Cursor se pehle ki aakhri `limit` entries. endBefore(value, key) cursor wali entry ko khud
            //    exclude karta hai (same createdAt wale posts bhi key se tie-break hote hain), isliye duplicate nahi.
            var query = db.getReference("feedPosts").orderByChild("createdAt")
            cursor?.let { query = query.endBefore(it.createdAt.toDouble(), it.postId) }
            val feedSnap = query.limitToLast(limit).get().await()

            val children = feedSnap.children.toList()   // ascending: pehli entry sabse purani
            val endReached = children.size < limit      // limit se kam aaye to aage kuch nahi
            // Cursor raw entries se: deleted/skip hone wali posts bhi cursor aage barhati hain.
            val next = children.firstOrNull()?.key?.let {
                FeedCursor(children.first().child("createdAt").getValue(Long::class.java) ?: 0L, it)
            } ?: cursor

            val entries = children.mapNotNull { child ->
                val postId = child.key ?: return@mapNotNull null
                val uid = child.child("uid").getValue(String::class.java) ?: return@mapNotNull null
                FeedEntry(postId, uid, child.child("createdAt").getValue(Long::class.java) ?: 0L)
            }

            // 2) N+1 reads ek saath: har post aur har (alag) user ke liye ek async. Users feed entry ke uid se
            //    pehle hi shuru ho jate hain (posts ka intezar nahi); mismatch wali entries assemble() hata deta hai.
            val postJobs = entries.associate { it.postId to async { readPost(it.postId) } }
            val userJobs = entries.map { it.uid }.distinct().associateWith { async { readUser(it) } }
            // Har post ke liye "kya maine like kiya?" bhi usi parallel batch mein.
            val me = currentUid()
            val likedJobs = if (me == null) emptyMap() else entries.associate { it.postId to async { readLiked(it.postId, me) } }
            val posts = FeedAssembler.assemble(
                entries = entries,
                posts = postJobs.mapValues { it.value.await() },
                users = userJobs.mapValues { it.value.await() },
                likedPostIds = likedJobs.filterValues { it.await() }.keys,
            )

            FeedPage(posts, next, endReached)
        }
    }

    private suspend fun readLiked(postId: String, uid: String): Boolean =
        db.getReference("postLikes/$postId/$uid").get().await().exists()

    private suspend fun readPost(postId: String): RawPost? {
        val snap = db.getReference("posts/$postId").get().await()
        if (!snap.exists()) return null
        val imageUrl = snap.child("imageUrl").getValue(String::class.java)?.takeIf { it.isNotBlank() } ?: return null
        val uid = snap.child("uid").getValue(String::class.java) ?: return null
        return RawPost(
            uid = uid,
            imageUrl = imageUrl,
            caption = snap.child("caption").getValue(String::class.java).orEmpty(),
            likeCount = snap.child("likeCount").getValue(Int::class.java) ?: 0,
        )
    }

    private suspend fun readUser(uid: String): RawUser? {
        val snap: DataSnapshot = db.getReference("users/$uid").get().await()
        if (!snap.exists()) return null
        return RawUser(
            username = snap.child("username").getValue(String::class.java).orEmpty(),
            avatarUrl = snap.child("avatarUrl").getValue(String::class.java)?.takeIf { it.isNotBlank() },
        )
    }
}
