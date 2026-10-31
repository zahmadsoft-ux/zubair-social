package com.zubair.social.upload

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException

class UploadServiceTest {
    private lateinit var server: MockWebServer
    private lateinit var service: UploadService

    @Before fun setUp() {
        server = MockWebServer().also { it.start() }
        service = UploadService(workerUrl = server.url("/").toString())
    }

    @After fun tearDown() = server.shutdown()

    private fun workerJson() =
        """{"uploadUrl":"${server.url("/bucket/posts/u1/1.jpg?sig=abc")}","imageUrl":"https://cdn.test/posts/u1/1.jpg","contentType":"image/jpeg"}"""

    @Test fun getUploadTarget_sendsBearerToken_andParsesJson() = runBlocking {
        server.enqueue(MockResponse().setBody(workerJson()))

        val target = service.getUploadTarget("my-id-token")

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/get-upload-url", req.path)
        assertEquals("Bearer my-id-token", req.getHeader("Authorization"))
        assertEquals("https://cdn.test/posts/u1/1.jpg", target.imageUrl)
        assertEquals("image/jpeg", target.contentType)
    }

    @Test fun getUploadTarget_401_throwsUnauthorized() {
        server.enqueue(MockResponse().setResponseCode(401))
        try {
            runBlocking { service.getUploadTarget("bad") }
            fail("expected UnauthorizedException")
        } catch (_: UnauthorizedException) { }
    }

    @Test fun upload_putsBytes_withContentType_andReportsProgress0to100() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200))
        val bytes = ByteArray(100_000) { (it % 251).toByte() }
        val progress = mutableListOf<Int>()

        service.upload(UploadTarget(server.url("/put").toString(), "x", "image/jpeg"), bytes) { progress += it }

        val req = server.takeRequest()
        assertEquals("PUT", req.method)
        assertEquals("image/jpeg", req.getHeader("Content-Type"))
        assertEquals(100_000L, req.bodySize)
        assertTrue(req.body.readByteArray().contentEquals(bytes))
        assertEquals(0, progress.first())
        assertEquals(100, progress.last())
        assertEquals("progress should never go backwards", progress.sorted(), progress)
        assertEquals("no duplicate percent callbacks", progress.distinct(), progress)
    }

    @Test fun upload_serverError_throwsIOException() {
        server.enqueue(MockResponse().setResponseCode(403))
        try {
            runBlocking { service.upload(UploadTarget(server.url("/put").toString(), "x", "image/jpeg"), ByteArray(10)) {} }
            fail("expected IOException")
        } catch (e: IOException) {
            assertTrue(e.message!!.contains("403"))
        }
    }
}
